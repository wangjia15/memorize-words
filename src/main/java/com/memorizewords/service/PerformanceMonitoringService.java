package com.memorizewords.service;

import com.memorizewords.event.*;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.*;
import java.util.List;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class PerformanceMonitoringService {

    private final MeterRegistry meterRegistry;
    private final ApplicationContext applicationContext;

    private final ConcurrentHashMap<String, AtomicLong> apiCallCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> apiCallDurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> slowQueryCounts = new ConcurrentHashMap<>();

    private volatile boolean alertingEnabled = true;
    private volatile double slowQueryThresholdMs = 1000.0;
    private volatile double slowApiThresholdMs = 500.0;

    public PerformanceMonitoringService(MeterRegistry meterRegistry, ApplicationContext applicationContext) {
        this.meterRegistry = meterRegistry;
        this.applicationContext = applicationContext;
        initializeMetrics();
    }

    private void initializeMetrics() {
        // Application metrics
        Gauge.builder("application.api.calls.total")
            .description("Total number of API calls")
            .register(meterRegistry, this, PerformanceMonitoringService::getTotalApiCalls);

        Gauge.builder("application.memory.used.bytes")
            .description("JVM memory used in bytes")
            .register(meterRegistry, this, PerformanceMonitoringService::getUsedMemory);

        Gauge.builder("application.memory.total.bytes")
            .description("JVM total memory in bytes")
            .register(meterRegistry, this, PerformanceMonitoringService::getTotalMemory);

        // Database metrics
        Gauge.builder("database.connections.active")
            .description("Active database connections")
            .register(meterRegistry, this, PerformanceMonitoringService::getActiveConnections);

        Gauge.builder("database.connections.idle")
            .description("Idle database connections")
            .register(meterRegistry, this, PerformanceMonitoringService::getIdleConnections);

        // Performance counters
        Counter.builder("application.errors.total")
            .description("Total application errors")
            .tag("type", "unhandled")
            .register(meterRegistry);

        Counter.builder("cache.hits.total")
            .description("Cache hit count")
            .register(meterRegistry);

        Counter.builder("cache.misses.total")
            .description("Cache miss count")
            .register(meterRegistry);

        // Review session metrics
        Counter.builder("review.sessions.started.total")
            .description("Total review sessions started")
            .register(meterRegistry);

        Counter.builder("review.sessions.completed.total")
            .description("Total review sessions completed")
            .register(meterRegistry);

        Timer.builder("review.session.duration")
            .description("Review session duration")
            .tag("status", "all")
            .register(meterRegistry);
    }

    @EventListener
    public void handleApiCallEvent(ApiCallEvent event) {
        String endpoint = event.getEndpoint();
        Duration duration = event.getDuration();

        // Update counters
        apiCallCounts.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        apiCallDurations.computeIfAbsent(endpoint, k -> new AtomicLong(0))
            .addAndGet(duration.toMillis());

        // Record metrics
        Timer.Sample sample = Timer.start();
        sample.stop(Timer.builder("api.request.duration")
            .tag("endpoint", endpoint)
            .tag("method", event.getMethod())
            .tag("status", String.valueOf(event.getStatusCode()))
            .register(meterRegistry));

        // Check for slow API calls
        if (duration.toMillis() > slowApiThresholdMs) {
            log.warn("Slow API call detected: {} {} took {}ms",
                event.getMethod(), endpoint, duration.toMillis());

            Counter.builder("api.slow.requests.total")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .increment();
        }
    }

    @EventListener
    public void handleSlowQueryEvent(SlowQueryEvent event) {
        String queryType = event.getQueryType();
        Duration duration = event.getDuration();

        // Update slow query counter
        slowQueryCounts.computeIfAbsent(queryType, k -> new AtomicLong(0)).incrementAndGet();

        // Record metrics
        Counter.builder("database.slow.queries.total")
            .tag("query.type", queryType)
            .register(meterRegistry)
            .increment();

        Timer.builder("database.query.duration")
            .tag("query.type", queryType)
            .tag("performance", "slow")
            .register(meterRegistry)
            .record(duration);

        // Log warning for very slow queries
        if (duration.toMillis() > 5000) {
            log.error("Very slow query detected: {} took {}ms",
                event.getQuery(), duration.toMillis());

            if (alertingEnabled) {
                triggerSlowQueryAlert(event);
            }
        }
    }

    @EventListener
    public void handleCacheEvent(CacheEvent event) {
        String cacheName = event.getCacheName();

        Counter.builder("cache.operations.total")
            .tag("cache.name", cacheName)
            .tag("operation", event.getOperation().toLowerCase())
            .register(meterRegistry)
            .increment();

        if ("hit".equals(event.getOperation())) {
            Counter.builder("cache.hits.total")
                .tag("cache.name", cacheName)
                .register(meterRegistry)
                .increment();
        } else if ("miss".equals(event.getOperation())) {
            Counter.builder("cache.misses.total")
                .tag("cache.name", cacheName)
                .register(meterRegistry)
                .increment();
        }
    }

    @EventListener
    public void handleReviewSessionEvent(ReviewSessionEvent event) {
        String eventType = event.getEventType();

        Counter.builder("review.sessions.events.total")
            .tag("event.type", eventType)
            .register(meterRegistry)
            .increment();

        if ("started".equals(eventType)) {
            Counter.builder("review.sessions.started.total")
                .register(meterRegistry)
                .increment();
        } else if ("completed".equals(eventType)) {
            Counter.builder("review.sessions.completed.total")
                .register(meterRegistry)
                .increment();

            // Record session duration
            if (event.getDuration() != null) {
                Timer.builder("review.session.duration")
                    .tag("status", "completed")
                    .register(meterRegistry)
                    .record(event.getDuration());
            }
        }
    }

    @EventListener
    public void handleMemoryAlertEvent(MemoryAlertEvent event) {
        double memoryUsagePercent = event.getMemoryUsagePercent();

        Gauge.builder("application.memory.usage.percent")
            .description("Memory usage percentage")
            .register(meterRegistry)
            .set(memoryUsagePercent);

        log.warn("Memory alert triggered: {}% memory usage", memoryUsagePercent);

        if (memoryUsagePercent > 90 && alertingEnabled) {
            triggerHighMemoryAlert(event);
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void collectSystemMetrics() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

            // Update memory metrics
            Gauge.builder("jvm.memory.used")
                .description("JVM used memory in bytes")
                .register(meterRegistry)
                .set(usedMemory);

            Gauge.builder("jvm.memory.free")
                .description("JVM free memory in bytes")
                .register(meterRegistry)
                .set(freeMemory);

            Gauge.builder("jvm.memory.total")
                .description("JVM total memory in bytes")
                .register(meterRegistry)
                .set(totalMemory);

            Gauge.builder("jvm.memory.max")
                .description("JVM max memory in bytes")
                .register(meterRegistry)
                .set(runtime.maxMemory());

            // Memory usage percentage
            Gauge.builder("jvm.memory.usage.percent")
                .description("JVM memory usage percentage")
                .register(meterRegistry)
                .set(memoryUsagePercent);

            // Thread metrics
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            Gauge.builder("jvm.threads.live")
                .description("Number of live threads")
                .register(meterRegistry)
                .set(threadBean.getThreadCount());

            Gauge.builder("jvm.threads.daemon")
                .description("Number of daemon threads")
                .register(meterRegistry)
                .set(threadBean.getDaemonThreadCount());

            // Garbage collection metrics
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                String name = gcBean.getName().replace(" ", "_").toLowerCase();

                Gauge.builder("jvm.gc.collections.total")
                    .tag("name", name)
                    .description("GC collection count")
                    .register(meterRegistry)
                    .set(gcBean.getCollectionCount());

                Gauge.builder("jvm.gc.collection.time.total")
                    .tag("name", name)
                    .description("GC collection time in milliseconds")
                    .register(meterRegistry)
                    .set(gcBean.getCollectionTime());
            }

            // Database connection pool metrics
            try {
                HikariDataSource dataSource = applicationContext.getBean(HikariDataSource.class);
                if (dataSource.getHikariPoolMXBean() != null) {
                    Gauge.builder("database.connections.active")
                        .description("Active database connections")
                        .register(meterRegistry)
                        .set(dataSource.getHikariPoolMXBean().getActiveConnections());

                    Gauge.builder("database.connections.idle")
                        .description("Idle database connections")
                        .register(meterRegistry)
                        .set(dataSource.getHikariPoolMXBean().getIdleConnections());

                    Gauge.builder("database.connections.total")
                        .description("Total database connections")
                        .register(meterRegistry)
                        .set(dataSource.getHikariPoolMXBean().getTotalConnections());

                    Gauge.builder("database.connections.threads.awaiting")
                        .description("Threads awaiting connections")
                        .register(meterRegistry)
                        .set(dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
                }
            } catch (Exception e) {
                log.debug("Could not collect database pool metrics: {}", e.getMessage());
            }

            // Check for high memory usage
            if (memoryUsagePercent > 85) {
                applicationContext.publishEvent(
                    new MemoryAlertEvent(memoryUsagePercent, Instant.now()));
            }

        } catch (Exception e) {
            log.error("Error collecting system metrics", e);
        }
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void generatePerformanceReport() {
        try {
            log.info("=== Performance Report ===");

            // API metrics
            log.info("API Calls: {}", apiCallCounts.entrySet().stream()
                .mapToLong(e -> e.getValue().get()).sum());

            // Slow queries
            log.info("Slow Queries: {}", slowQueryCounts.entrySet().stream()
                .mapToLong(e -> e.getValue().get()).sum());

            // Memory usage
            Runtime runtime = Runtime.getRuntime();
            double memoryUsagePercent = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100;
            log.info("Memory Usage: {:.2f}%", memoryUsagePercent);

            log.info("========================");

        } catch (Exception e) {
            log.error("Error generating performance report", e);
        }
    }

    public void recordApiCall(String endpoint, String method, int statusCode, Duration duration) {
        applicationContext.publishEvent(
            new ApiCallEvent(endpoint, method, statusCode, duration, Instant.now()));
    }

    public void recordCacheOperation(String cacheName, String operation) {
        applicationContext.publishEvent(
            new CacheEvent(cacheName, operation, Instant.now()));
    }

    public void recordSlowQuery(String queryType, String query, Duration duration) {
        applicationContext.publishEvent(
            new SlowQueryEvent(queryType, query, duration, Instant.now()));
    }

    public void recordReviewSessionEvent(String eventType, Duration duration) {
        applicationContext.publishEvent(
            new ReviewSessionEvent(eventType, duration, Instant.now()));
    }

    private void triggerSlowQueryAlert(SlowQueryEvent event) {
        log.error("ALERT: Slow query detected - Type: {}, Duration: {}ms, Query: {}",
            event.getQueryType(), event.getDuration().toMillis(), event.getQuery());
    }

    private void triggerHighMemoryAlert(MemoryAlertEvent event) {
        log.error("ALERT: High memory usage detected - {}% memory usage",
            event.getMemoryUsagePercent());
    }

    // Helper methods for metrics
    private double getTotalApiCalls() {
        return apiCallCounts.values().stream().mapToLong(AtomicLong::get).sum();
    }

    private double getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private double getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    private double getActiveConnections() {
        try {
            HikariDataSource dataSource = applicationContext.getBean(HikariDataSource.class);
            return dataSource.getHikariPoolMXBean() != null ?
                dataSource.getHikariPoolMXBean().getActiveConnections() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double getIdleConnections() {
        try {
            HikariDataSource dataSource = applicationContext.getBean(HikariDataSource.class);
            return dataSource.getHikariPoolMXBean() != null ?
                dataSource.getHikariPoolMXBean().getIdleConnections() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Configuration methods
    public void setAlertingEnabled(boolean alertingEnabled) {
        this.alertingEnabled = alertingEnabled;
    }

    public void setSlowQueryThresholdMs(double slowQueryThresholdMs) {
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    public void setSlowApiThresholdMs(double slowApiThresholdMs) {
        this.slowApiThresholdMs = slowApiThresholdMs;
    }

    public boolean isAlertingEnabled() {
        return alertingEnabled;
    }

    public double getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public double getSlowApiThresholdMs() {
        return slowApiThresholdMs;
    }
}