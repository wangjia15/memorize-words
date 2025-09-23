package com.memorizewords.performance;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the complete result of a performance test execution.
 */
public class PerformanceResult {

    private final String testName;
    private final List<PerformanceMetrics> metrics;
    private final double durationSeconds;
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final double averageResponseTime;
    private final double minResponseTime;
    private final double maxResponseTime;
    private final double percentile95;
    private final double percentile99;
    private final double throughputPerSecond;
    private final double errorRate;

    public PerformanceResult(String testName, List<PerformanceMetrics> metrics, double durationSeconds) {
        this.testName = testName;
        this.metrics = metrics;
        this.durationSeconds = durationSeconds;

        // Calculate statistics
        this.totalRequests = metrics.size();
        this.successfulRequests = metrics.stream().filter(PerformanceMetrics::isSuccess).count();
        this.failedRequests = totalRequests - successfulRequests;

        List<Long> responseTimes = metrics.stream()
            .map(PerformanceMetrics::getResponseTimeMs)
            .collect(Collectors.toList());

        this.averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        this.minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        this.maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        this.percentile95 = calculatePercentile(responseTimes, 95);
        this.percentile99 = calculatePercentile(responseTimes, 99);

        this.throughputPerSecond = durationSeconds > 0 ? totalRequests / durationSeconds : 0;
        this.errorRate = totalRequests > 0 ? (double) failedRequests / totalRequests : 0;
    }

    /**
     * Calculate percentile for response times.
     */
    private double calculatePercentile(List<Long> values, double percentile) {
        if (values.isEmpty()) return 0;

        values.sort(Long::compareTo);
        int index = (int) Math.ceil(percentile / 100 * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));

        return values.get(index);
    }

    /**
     * Validate the result against performance thresholds.
     */
    public void validateAgainstThresholds(BasePerformanceTest.PerformanceThresholds thresholds) {
        List<String> violations = new java.util.ArrayList<>();

        if (maxResponseTime > thresholds.getMaxResponseTimeMs()) {
            violations.add(String.format("Max response time %dms exceeds threshold %dms",
                (long) maxResponseTime, thresholds.getMaxResponseTimeMs()));
        }

        if (throughputPerSecond < thresholds.getMinThroughputPerSecond()) {
            violations.add(String.format("Throughput %.2f req/s below threshold %.2f req/s",
                throughputPerSecond, thresholds.getMinThroughputPerSecond()));
        }

        if (errorRate > thresholds.getMaxErrorRate()) {
            violations.add(String.format("Error rate %.2f%% exceeds threshold %.2f%%",
                errorRate * 100, thresholds.getMaxErrorRate() * 100));
        }

        if (!violations.isEmpty()) {
            String message = String.format("Performance test '%s' failed validation:%n%s",
                testName, String.join("\n", violations));
            throw new PerformanceValidationException(message, violations);
        }
    }

    /**
     * Generate a detailed report of the performance test results.
     */
    public String generateReport() {
        return String.format(
            "Performance Test Report: %s%n" +
            "=====================================%n" +
            "Total Requests: %d%n" +
            "Successful Requests: %d%n" +
            "Failed Requests: %d%n" +
            "Duration: %.2f seconds%n" +
            "Throughput: %.2f requests/second%n" +
            "Error Rate: %.2f%%%n" +
            "Response Times:%n" +
            "  Average: %.2f ms%n" +
            "  Minimum: %.0f ms%n" +
            "  Maximum: %.0f ms%n" +
            "  95th percentile: %.0f ms%n" +
            "  99th percentile: %.0f ms%n",
            testName, totalRequests, successfulRequests, failedRequests, durationSeconds,
            throughputPerSecond, errorRate * 100, averageResponseTime, minResponseTime,
            maxResponseTime, percentile95, percentile99);
    }

    // Getters
    public String getTestName() { return testName; }
    public List<PerformanceMetrics> getMetrics() { return metrics; }
    public double getDurationSeconds() { return durationSeconds; }
    public long getTotalRequests() { return totalRequests; }
    public long getSuccessfulRequests() { return successfulRequests; }
    public long getFailedRequests() { return failedRequests; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getMinResponseTime() { return minResponseTime; }
    public double getMaxResponseTime() { return maxResponseTime; }
    public double getPercentile95() { return percentile95; }
    public double getPercentile99() { return percentile99; }
    public double getThroughputPerSecond() { return throughputPerSecond; }
    public double getErrorRate() { return errorRate; }
}