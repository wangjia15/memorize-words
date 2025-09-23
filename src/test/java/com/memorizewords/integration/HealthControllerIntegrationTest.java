package com.memorizewords.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for health check endpoints.
 * Tests that the application health endpoints are working correctly.
 */
class HealthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        logTestInfo("Testing health endpoint");

        MvcResult result = performGetRequest("/actuator/health");

        // Verify response status
        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk().match(result);

        // Verify response content type
        org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("application/vnd.spring-boot.actuator.v3+json").match(result);

        logTestInfo("Health endpoint test completed successfully");
    }

    @Test
    void infoEndpointShouldReturnApplicationInfo() throws Exception {
        logTestInfo("Testing info endpoint");

        MvcResult result = performGetRequest("/actuator/info");

        // Verify response status
        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk().match(result);

        logTestInfo("Info endpoint test completed successfully");
    }

    @Test
    void metricsEndpointShouldBeAccessible() throws Exception {
        logTestInfo("Testing metrics endpoint");

        MvcResult result = performGetRequest("/actuator/metrics");

        // Verify response status
        org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk().match(result);

        logTestInfo("Metrics endpoint test completed successfully");
    }
}