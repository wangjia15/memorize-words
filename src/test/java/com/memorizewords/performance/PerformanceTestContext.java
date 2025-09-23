package com.memorizewords.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Context for managing performance test execution and metrics collection.
 */
public class PerformanceTestContext {

    private final ExecutorService executorService;
    private final List<PerformanceMetrics> metricsHistory;
    private final AtomicInteger successfulRequests;
    private final AtomicInteger failedRequests;
    private final AtomicLong totalResponseTime;
    private final AtomicInteger concurrentRequests;

    public PerformanceTestContext() {
        this.executorService = Executors.newCachedThreadPool();
        this.metricsHistory = new ArrayList<>();
        this.successfulRequests = new AtomicInteger(0);
        this.failedRequests = new AtomicInteger(0);
        this.totalResponseTime = new AtomicLong(0);
        this.concurrentRequests = new AtomicInteger(0);
    }

    /**
     * Initialize the test context.
     */
    public void initialize() {
        reset();
    }

    /**
     * Reset metrics between test runs.
     */
    public void reset() {
        metricsHistory.clear();
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        concurrentRequests.set(0);
    }

    /**
     * Execute a performance test with the given configuration.
     */
    public PerformanceResult executeTest(BasePerformanceTest.PerformanceTestConfig config,
                                      BasePerformanceTest.PerformanceTestRunnable testRunnable) {
        // Warmup phase
        executeWarmup(config, testRunnable);

        // Measurement phase
        return executeMeasurement(config, testRunnable);
    }

    /**
     * Execute warmup iterations.
     */
    private void executeWarmup(BasePerformanceTest.PerformanceTestConfig config,
                             BasePerformanceTest.PerformanceTestRunnable testRunnable) {
        CountDownLatch warmupLatch = new CountDownLatch(config.getWarmupIterations());

        for (int i = 0; i < config.getWarmupIterations(); i++) {
            executorService.submit(() -> {
                try {
                    testRunnable.run();
                } catch (Exception e) {
                    // Ignore warmup failures
                } finally {
                    warmupLatch.countDown();
                }
            });
        }

        try {
            warmupLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Execute measurement phase.
     */
    private PerformanceResult executeMeasurement(BasePerformanceTest.PerformanceTestConfig config,
                                               BasePerformanceTest.PerformanceTestRunnable testRunnable) {
        List<Future<PerformanceMetrics>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Execute test iterations
        for (int i = 0; i < config.getMeasurementIterations(); i++) {
            futures.add(executorService.submit(() -> executeSingleIteration(testRunnable)));
        }

        // Wait for all iterations to complete
        List<PerformanceMetrics> allMetrics = new ArrayList<>();
        for (Future<PerformanceMetrics> future : futures) {
            try {
                PerformanceMetrics metrics = future.get(30, TimeUnit.SECONDS);
                allMetrics.add(metrics);
            } catch (Exception e) {
                failedRequests.incrementAndGet();
            }
        }

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;

        return new PerformanceResult(config.getTestName(), allMetrics, durationSeconds);
    }

    /**
     * Execute a single test iteration.
     */
    private PerformanceMetrics executeSingleIteration(BasePerformanceTest.PerformanceTestRunnable testRunnable) {
        long startTime = System.nanoTime();
        boolean success = true;
        String errorMessage = null;

        try {
            testRunnable.run();
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
        }

        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);

        if (success) {
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(durationMillis);
        } else {
            failedRequests.incrementAndGet();
        }

        return new PerformanceMetrics(durationMillis, success, errorMessage);
    }

    /**
     * Get current metrics summary.
     */
    public PerformanceMetrics getCurrentMetrics() {
        long totalRequests = successfulRequests.get() + failedRequests.get();
        double averageResponseTime = totalRequests > 0 ?
            (double) totalResponseTime.get() / totalRequests : 0;
        double successRate = totalRequests > 0 ?
            (double) successfulRequests.get() / totalRequests : 0;

        return new PerformanceMetrics((long) averageResponseTime, successRate >= 1.0, null);
    }

    /**
     * Cleanup resources.
     */
    public void cleanup() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}