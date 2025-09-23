package com.memorizewords.event;

import java.time.Duration;
import java.time.Instant;

public class ReviewSessionEvent {
    private final String eventType;
    private final Duration duration;
    private final Instant timestamp;

    public ReviewSessionEvent(String eventType, Duration duration, Instant timestamp) {
        this.eventType = eventType;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}