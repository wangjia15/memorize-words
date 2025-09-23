package com.memorizewords.unit;

import com.memorizewords.MemorizeWordsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the main MemorizeWordsApplication class.
 * This test verifies that the Spring Boot application can start successfully.
 */
@SpringBootTest(classes = MemorizeWordsApplication.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MemorizeWordsApplicationTest {

    @Test
    void contextLoads() {
        // Test passes if Spring context loads successfully
        assertThat(true).isTrue();
    }

    @Test
    void applicationShouldStartWithoutErrors() {
        // This test verifies that the application can start without throwing exceptions
        // The @SpringBootTest annotation will handle the actual startup
        assertThat(MemorizeWordsApplication.class).isNotNull();
    }
}