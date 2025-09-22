package com.memorizewords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Memorize Words application.
 *
 * This is a language learning application with spaced repetition algorithm
 * for effective vocabulary memorization.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@SpringBootApplication
@EnableJpaAuditing
public class MemorizeWordsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemorizeWordsApplication.class, args);
    }
}