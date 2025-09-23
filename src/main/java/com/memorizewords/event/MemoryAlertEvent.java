package com.memorizewords.event;

import java.time.Instant;

public class MemoryAlertEvent {
    private final double memoryUsagePercent;
    private final Instant timestamp;

    public MemoryAlertEvent(double memoryUsagePercent, Instant timestamp) {
        this.memoryUsagePercent = memoryUsagePercent;
        this.timestamp = timestamp;
    }

    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}