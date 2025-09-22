package com.memorizewords.integration;

import com.memorizewords.config.TestConfig;
import com.memorizewords.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Base class for integration tests.
 * Provides common setup and utilities for integration testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Setup method that runs before each test.
     * Override in subclasses to provide test-specific setup.
     */
    @BeforeEach
    void setUp() {
        // Common setup for all integration tests
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up test-specific data.
     */
    protected void setupTestData() {
        // Default implementation does nothing
        // Subclasses can override to provide their own test data setup
    }

    /**
     * Helper method to log test information for debugging.
     */
    protected void logTestInfo(String message) {
        System.out.println("[TEST INFO] " + message + " - " + this.getClass().getSimpleName());
    }

    /**
     * Helper method to log test warnings.
     */
    protected void logTestWarning(String message) {
        System.out.println("[TEST WARNING] " + message + " - " + this.getClass().getSimpleName());
    }

    /**
     * Helper method to perform GET requests.
     */
    protected MvcResult performGetRequest(String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn();
    }
}
