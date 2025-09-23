package com.memorizewords.event;

import java.time.Duration;
import java.time.Instant;

public class ApiCallEvent {
    private final String endpoint;
    private final String method;
    private final int statusCode;
    private final Duration duration;
    private final Instant timestamp;

    public ApiCallEvent(String endpoint, String method, int statusCode, Duration duration, Instant timestamp) {
        this.endpoint = endpoint;
        this.method = method;
        this.statusCode = statusCode;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getMethod() {
        return method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}