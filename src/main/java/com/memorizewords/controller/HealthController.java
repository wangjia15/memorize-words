package com.memorizewords.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 * Provides basic health information about the application.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Basic health check endpoint.
     * Returns application status and timestamp.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "Memorize Words");
        health.put("version", "1.0.0-SNAPSHOT");

        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check endpoint.
     * Returns additional system information.
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "Memorize Words");
        health.put("version", "1.0.0-SNAPSHOT");
        health.put("javaVersion", System.getProperty("java.version"));
        health.put("javaVendor", System.getProperty("java.vendor"));
        health.put("osName", System.getProperty("os.name"));
        health.put("osVersion", System.getProperty("os.version"));
        health.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        health.put("freeMemory", Runtime.getRuntime().freeMemory());
        health.put("totalMemory", Runtime.getRuntime().totalMemory());
        health.put("maxMemory", Runtime.getRuntime().maxMemory());

        return ResponseEntity.ok(health);
    }
}