package com.memorizewords;

import com.memorizewords.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for the main application class.
 * Verifies that the application can start successfully and all components are properly configured.
 */
@SpringBootTest
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class MemorizeWordsApplicationIntegrationTest {

    @Test
    void testApplicationStartup() {
        assertDoesNotThrow(() -> {
            // Application context is loaded successfully by Spring Boot
            // This test verifies that all beans can be created without errors
        });
    }

    @Test
    void testApplicationComponents() {
        assertDoesNotThrow(() -> {
            // This test verifies that all application components are properly configured
            // The test passes if no exceptions are thrown during context loading
        });
    }
}
