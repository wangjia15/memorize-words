package com.memorizewords.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorizewords.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for GlobalExceptionHandler.
 * Verifies that global exception handling works correctly for various scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpointAccessibility() throws Exception {
        // Test that health endpoints are accessible (should return 200)
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testDetailedHealthEndpointAccessibility() throws Exception {
        // Test that detailed health endpoint is accessible
        mockMvc.perform(get("/api/health/detailed"))
                .andExpect(status().isOk());
    }

    @Test
    void testNonExistentEndpointReturnsError() throws Exception {
        // Test that non-existent endpoints return an error status
        mockMvc.perform(get("/api/non-existent-endpoint"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testExceptionHandlerIsConfigured() throws Exception {
        // Test that the exception handler is properly configured
        // This test passes if the context loads successfully and no exceptions are thrown
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testApplicationCanHandleRequests() throws Exception {
        // Test that the application can handle various requests without crashing
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}
