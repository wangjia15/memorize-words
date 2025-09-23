package com.memorizewords.event;

import java.time.Duration;
import java.time.Instant;

public class SlowQueryEvent {
    private final String queryType;
    private final String query;
    private final Duration duration;
    private final Instant timestamp;

    public SlowQueryEvent(String queryType, String query, Duration duration, Instant timestamp) {
        this.queryType = queryType;
        this.query = query;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getQuery() {
        return query;
    }

    public Duration getDuration() {
        return duration;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}