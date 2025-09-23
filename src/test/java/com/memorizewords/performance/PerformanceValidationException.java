package com.memorizewords.performance;

import java.util.List;

/**
 * Exception thrown when performance thresholds are violated.
 */
public class PerformanceValidationException extends RuntimeException {

    private final List<String> violations;

    public PerformanceValidationException(String message, List<String> violations) {
        super(message);
        this.violations = violations;
    }

    /**
     * Get the list of violations that caused the exception.
     */
    public List<String> getViolations() {
        return violations;
    }

    /**
     * Get a formatted summary of violations.
     */
    public String getViolationSummary() {
        return String.join("\n", violations);
    }
}