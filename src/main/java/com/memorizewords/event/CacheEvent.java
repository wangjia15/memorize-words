package com.memorizewords.event;

import java.time.Instant;

public class CacheEvent {
    private final String cacheName;
    private final String operation;
    private final Instant timestamp;

    public CacheEvent(String cacheName, String operation, Instant timestamp) {
        this.cacheName = cacheName;
        this.operation = operation;
        this.timestamp = timestamp;
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getOperation() {
        return operation;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}