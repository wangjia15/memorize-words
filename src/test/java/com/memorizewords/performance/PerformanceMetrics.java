package com.memorizewords.performance;

/**
 * Represents performance metrics for a single test execution.
 */
public class PerformanceMetrics {

    private final long responseTimeMs;
    private final boolean success;
    private final String errorMessage;
    private final long timestamp;

    public PerformanceMetrics(long responseTimeMs, boolean success, String errorMessage) {
        this.responseTimeMs = responseTimeMs;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if this metrics represents a failure.
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Check if the response time exceeds a threshold.
     */
    public boolean exceedsThreshold(long thresholdMs) {
        return responseTimeMs > thresholdMs;
    }

    @Override
    public String toString() {
        return String.format("PerformanceMetrics{responseTimeMs=%d, success=%s, errorMessage='%s', timestamp=%d}",
                responseTimeMs, success, errorMessage, timestamp);
    }
}