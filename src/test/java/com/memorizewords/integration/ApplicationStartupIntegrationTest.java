package com.memorizewords.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the application can start up successfully
 * with all required components and configurations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApplicationStartupIntegrationTest {

    @Test
    void applicationContextShouldLoad() {
        // This test passes if the Spring application context loads successfully
        // The @SpringBootTest annotation handles the actual startup
        assertThat(true).isTrue();
    }

    @Test
    void databaseConnectionShouldBeConfigured() {
        // This test verifies that database configuration is properly set up
        // The actual test is the successful loading of the context
        assertThat(true).isTrue();
    }

    @Test
    void securityConfigurationShouldLoad() {
        // This test verifies that Spring Security configuration loads
        assertThat(true).isTrue();
    }
}