package com.memorizewords.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Memory leak detection utility for performance testing.
 * Provides mechanisms to detect and prevent memory leaks during testing.
 */
public class MemoryLeakDetector {

    private static final Logger logger = LoggerFactory.getLogger(MemoryLeakDetector.class);
    private static final long MEMORY_LEAK_THRESHOLD_MS = 60000; // 1 minute
    private static final double MEMORY_GROWTH_THRESHOLD_PERCENT = 5.0; // 5% growth
    private static final int GC_ATTEMPTS = 3;
    private static final long SLEEP_BETWEEN_GC_MS = 1000;

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    private final List<MemorySnapshot> memorySnapshots = new ArrayList<>();
    private final AtomicLong objectCreationCount = new AtomicLong(0);
    private final AtomicLong objectDestructionCount = new AtomicLong(0);

    /**
     * Take a memory snapshot.
     */
    public MemorySnapshot takeSnapshot() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        List<MemoryPoolUsage> poolUsages = new ArrayList<>();
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            if (pool.isValid()) {
                poolUsages.add(new MemoryPoolUsage(
                    pool.getName(),
                    pool.getUsage()
                ));
            }
        }

        MemorySnapshot snapshot = new MemorySnapshot(
            System.currentTimeMillis(),
            heapUsage,
            nonHeapUsage,
            poolUsages,
            objectCreationCount.get(),
            objectDestructionCount.get()
        );

        memorySnapshots.add(snapshot);
        return snapshot;
    }

    /**
     * Perform garbage collection and take a clean memory snapshot.
     */
    public MemorySnapshot takeCleanSnapshot() {
        forceGarbageCollection();
        return takeSnapshot();
    }

    /**
     * Force garbage collection with multiple attempts.
     */
    public void forceGarbageCollection() {
        for (int i = 0; i < GC_ATTEMPTS; i++) {
            System.gc();
            System.runFinalization();
            try {
                Thread.sleep(SLEEP_BETWEEN_GC_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Record object creation.
     */
    public void recordObjectCreation() {
        objectCreationCount.incrementAndGet();
    }

    /**
     * Record object destruction.
     */
    public void recordObjectDestruction() {
        objectDestructionCount.incrementAndGet();
    }

    /**
     * Run a memory leak test for the given duration.
     */
    public MemoryLeakTestResult runLeakTest(Runnable testOperation, long durationMs) {
        logger.info("Starting memory leak test for {} ms", durationMs);

        MemorySnapshot initialSnapshot = takeCleanSnapshot();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs;

        // Run the test operation in a loop
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                while (System.currentTimeMillis() < endTime) {
                    testOperation.run();
                    recordObjectCreation();
                    Thread.yield();
                }
            } catch (Exception e) {
                logger.error("Error during memory leak test operation", e);
            }
        });

        try {
            future.get(durationMs + 5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Memory leak test timed out or failed", e);
            future.cancel(true);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        MemorySnapshot finalSnapshot = takeCleanSnapshot();
        return analyzeMemoryLeak(initialSnapshot, finalSnapshot, durationMs);
    }

    /**
     * Analyze memory snapshots for potential leaks.
     */
    public MemoryLeakTestResult analyzeMemoryLeak(MemorySnapshot initial, MemorySnapshot finalSnapshot, long durationMs) {
        long initialHeapUsed = initial.getHeapUsage().getUsed();
        long finalHeapUsed = finalSnapshot.getHeapUsage().getUsed();
        long heapGrowth = finalHeapUsed - initialHeapUsed;

        double growthPercent = initialHeapUsed > 0 ?
            (double) heapGrowth / initialHeapUsed * 100 : 0;

        double growthRatePerHour = durationMs > 0 ?
            (double) heapGrowth / durationMs * 3600000 : 0;

        // Check specific memory pools for leaks
        List<MemoryPoolLeak> poolLeaks = new ArrayList<>();
        for (MemoryPoolUsage initialPool : initial.getPoolUsages()) {
            MemoryPoolUsage finalPool = finalSnapshot.getPoolUsages().stream()
                .filter(p -> p.getName().equals(initialPool.getName()))
                .findFirst()
                .orElse(null);

            if (finalPool != null) {
                long poolGrowth = finalPool.getUsage().getUsed() - initialPool.getUsage().getUsed();
                double poolGrowthPercent = initialPool.getUsage().getUsed() > 0 ?
                    (double) poolGrowth / initialPool.getUsage().getUsed() * 100 : 0;

                if (poolGrowthPercent > MEMORY_GROWTH_THRESHOLD_PERCENT) {
                    poolLeaks.add(new MemoryPoolLeak(
                        initialPool.getName(),
                        poolGrowth,
                        poolGrowthPercent
                    ));
                }
            }
        }

        // Analyze object creation/destruction patterns
        long objectsCreated = finalSnapshot.getObjectsCreated() - initialSnapshot.getObjectsCreated();
        long objectsDestroyed = finalSnapshot.getObjectsDestroyed() - initialSnapshot.getObjectsDestroyed();
        long netObjectGrowth = objectsCreated - objectsDestroyed;

        boolean isLeakDetected = growthPercent > MEMORY_GROWTH_THRESHOLD_PERCENT ||
                               !poolLeaks.isEmpty() ||
                               netObjectGrowth > 1000; // Significant object growth

        return new MemoryLeakTestResult(
            initial,
            finalSnapshot,
            heapGrowth,
            growthPercent,
            growthRatePerHour,
            objectsCreated,
            objectsDestroyed,
            netObjectGrowth,
            poolLeaks,
            isLeakDetected
        );
    }

    /**
     * Get a summary of all memory snapshots.
     */
    public String getMemorySummary() {
        if (memorySnapshots.isEmpty()) {
            return "No memory snapshots available";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Memory Leak Detection Summary\n");
        summary.append("============================\n");

        MemorySnapshot first = memorySnapshots.get(0);
        MemorySnapshot last = memorySnapshots.get(memorySnapshots.size() - 1);

        long totalDurationMs = last.getTimestamp() - first.getTimestamp();
        long totalHeapGrowth = last.getHeapUsage().getUsed() - first.getHeapUsage().getUsed();
        double avgHeapGrowthPerHour = totalDurationMs > 0 ?
            (double) totalHeapGrowth / totalDurationMs * 3600000 : 0;

        summary.append(String.format("Total snapshots: %d\n", memorySnapshots.size()));
        summary.append(String.format("Total duration: %.2f seconds\n", totalDurationMs / 1000.0));
        summary.append(String.format("Initial heap usage: %d MB\n", first.getHeapUsage().getUsed() / 1024 / 1024));
        summary.append(String.format("Final heap usage: %d MB\n", last.getHeapUsage().getUsed() / 1024 / 1024));
        summary.append(String.format("Total heap growth: %d MB\n", totalHeapGrowth / 1024 / 1024));
        summary.append(String.format("Average growth rate: %.2f MB/hour\n", avgHeapGrowthPerHour / 1024 / 1024));

        return summary.toString();
    }

    /**
     * Clear all memory snapshots.
     */
    public void clearSnapshots() {
        memorySnapshots.clear();
        objectCreationCount.set(0);
        objectDestructionCount.set(0);
    }

    /**
     * Memory snapshot data class.
     */
    public static class MemorySnapshot {
        private final long timestamp;
        private final MemoryUsage heapUsage;
        private final MemoryUsage nonHeapUsage;
        private final List<MemoryPoolUsage> poolUsages;
        private final long objectsCreated;
        private final long objectsDestroyed;

        public MemorySnapshot(long timestamp, MemoryUsage heapUsage, MemoryUsage nonHeapUsage,
                            List<MemoryPoolUsage> poolUsages, long objectsCreated, long objectsDestroyed) {
            this.timestamp = timestamp;
            this.heapUsage = heapUsage;
            this.nonHeapUsage = nonHeapUsage;
            this.poolUsages = poolUsages;
            this.objectsCreated = objectsCreated;
            this.objectsDestroyed = objectsDestroyed;
        }

        // Getters
        public long getTimestamp() { return timestamp; }
        public MemoryUsage getHeapUsage() { return heapUsage; }
        public MemoryUsage getNonHeapUsage() { return nonHeapUsage; }
        public List<MemoryPoolUsage> getPoolUsages() { return poolUsages; }
        public long getObjectsCreated() { return objectsCreated; }
        public long getObjectsDestroyed() { return objectsDestroyed; }
    }

    /**
     * Memory pool usage data class.
     */
    public static class MemoryPoolUsage {
        private final String name;
        private final MemoryUsage usage;

        public MemoryPoolUsage(String name, MemoryUsage usage) {
            this.name = name;
            this.usage = usage;
        }

        // Getters
        public String getName() { return name; }
        public MemoryUsage getUsage() { return usage; }
    }

    /**
     * Memory leak test result.
     */
    public static class MemoryLeakTestResult {
        private final MemorySnapshot initialSnapshot;
        private final MemorySnapshot finalSnapshot;
        private final long heapGrowthBytes;
        private final double heapGrowthPercent;
        private final double growthRatePerHour;
        private final long objectsCreated;
        private final long objectsDestroyed;
        private final long netObjectGrowth;
        private final List<MemoryPoolLeak> poolLeaks;
        private final boolean leakDetected;

        public MemoryLeakTestResult(MemorySnapshot initialSnapshot, MemorySnapshot finalSnapshot,
                                   long heapGrowthBytes, double heapGrowthPercent, double growthRatePerHour,
                                   long objectsCreated, long objectsDestroyed, long netObjectGrowth,
                                   List<MemoryPoolLeak> poolLeaks, boolean leakDetected) {
            this.initialSnapshot = initialSnapshot;
            this.finalSnapshot = finalSnapshot;
            this.heapGrowthBytes = heapGrowthBytes;
            this.heapGrowthPercent = heapGrowthPercent;
            this.growthRatePerHour = growthRatePerHour;
            this.objectsCreated = objectsCreated;
            this.objectsDestroyed = objectsDestroyed;
            this.netObjectGrowth = netObjectGrowth;
            this.poolLeaks = poolLeaks;
            this.leakDetected = leakDetected;
        }

        /**
         * Validate against leak thresholds.
         */
        public void validateAgainstThresholds(double maxGrowthPercent, double maxGrowthRatePerHour) {
            List<String> violations = new ArrayList<>();

            if (heapGrowthPercent > maxGrowthPercent) {
                violations.add(String.format("Heap growth %.2f%% exceeds threshold %.2f%%",
                    heapGrowthPercent, maxGrowthPercent));
            }

            if (growthRatePerHour > maxGrowthRatePerHour) {
                violations.add(String.format("Growth rate %.2f bytes/hour exceeds threshold %.2f bytes/hour",
                    growthRatePerHour, maxGrowthRatePerHour));
            }

            if (!poolLeaks.isEmpty()) {
                violations.add("Memory pool leaks detected: " +
                    poolLeaks.stream().map(MemoryPoolLeak::toString).collect(java.util.stream.Collectors.joining(", ")));
            }

            if (!violations.isEmpty()) {
                throw new MemoryLeakValidationException("Memory leak validation failed", violations);
            }
        }

        // Getters
        public MemorySnapshot getInitialSnapshot() { return initialSnapshot; }
        public MemorySnapshot getFinalSnapshot() { return finalSnapshot; }
        public long getHeapGrowthBytes() { return heapGrowthBytes; }
        public double getHeapGrowthPercent() { return heapGrowthPercent; }
        public double getGrowthRatePerHour() { return growthRatePerHour; }
        public long getObjectsCreated() { return objectsCreated; }
        public long getObjectsDestroyed() { return objectsDestroyed; }
        public long getNetObjectGrowth() { return netObjectGrowth; }
        public List<MemoryPoolLeak> getPoolLeaks() { return poolLeaks; }
        public boolean isLeakDetected() { return leakDetected; }
    }

    /**
     * Memory pool leak information.
     */
    public static class MemoryPoolLeak {
        private final String poolName;
        private final long growthBytes;
        private final double growthPercent;

        public MemoryPoolLeak(String poolName, long growthBytes, double growthPercent) {
            this.poolName = poolName;
            this.growthBytes = growthBytes;
            this.growthPercent = growthPercent;
        }

        @Override
        public String toString() {
            return String.format("%s: +%d bytes (%.2f%%)", poolName, growthBytes, growthPercent);
        }

        // Getters
        public String getPoolName() { return poolName; }
        public long getGrowthBytes() { return growthBytes; }
        public double getGrowthPercent() { return growthPercent; }
    }

    /**
     * Exception thrown when memory leak validation fails.
     */
    public static class MemoryLeakValidationException extends RuntimeException {
        private final List<String> violations;

        public MemoryLeakValidationException(String message, List<String> violations) {
            super(message);
            this.violations = violations;
        }

        public List<String> getViolations() {
            return violations;
        }
    }
}