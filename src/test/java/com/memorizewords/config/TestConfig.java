package com.memorizewords.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test configuration class that provides beans needed for testing.
 * This configuration is automatically picked up by Spring Boot test context.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a password encoder for testing purposes.
     * Using a fixed strength for predictable test results.
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    /**
     * Test configuration properties for database and other test-specific settings.
     */
    @Bean
    @Primary
    public TestProperties testProperties() {
        return new TestProperties();
    }

    /**
     * Inner class to hold test-specific properties.
     */
    public static class TestProperties {
        private String testDatabaseUrl = "jdbc:h2:mem:testdb";
        private String testDatabaseUsername = "sa";
        private String testDatabasePassword = "password";

        public String getTestDatabaseUrl() {
            return testDatabaseUrl;
        }

        public void setTestDatabaseUrl(String testDatabaseUrl) {
            this.testDatabaseUrl = testDatabaseUrl;
        }

        public String getTestDatabaseUsername() {
            return testDatabaseUsername;
        }

        public void setTestDatabaseUsername(String testDatabaseUsername) {
            this.testDatabaseUsername = testDatabaseUsername;
        }

        public String getTestDatabasePassword() {
            return testDatabasePassword;
        }

        public void setTestDatabasePassword(String testDatabasePassword) {
            this.testDatabasePassword = testDatabasePassword;
        }
    }
}