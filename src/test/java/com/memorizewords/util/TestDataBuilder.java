package com.memorizewords.util;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for building test data objects.
 * Provides fluent API for creating test entities with realistic test data.
 */
public class TestDataBuilder {

    /**
     * Creates a random username for testing.
     */
    public static String createRandomUsername() {
        return "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Creates a random email for testing.
     */
    public static String createRandomEmail() {
        return "test." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /**
     * Creates a random word for testing vocabulary.
     */
    public static String createRandomWord() {
        String[] words = {"hello", "world", "spring", "boot", "test", "java", "code", "learn", "study", "memory"};
        return words[(int) (Math.random() * words.length)];
    }

    /**
     * Creates a random definition for testing.
     */
    public static String createRandomDefinition() {
        String[] definitions = {
            "A greeting or expression of goodwill",
            "The earth or globe, as a planet",
            "The season after winter and before summer",
            "To kick or strike with the foot",
            "A procedure intended to establish the quality of something",
            "A high-level programming language",
            "System of words, letters, or symbols used to represent others",
            "To acquire knowledge through study or experience",
            "The devotion of time and attention to gaining knowledge",
            "The faculty of the mind to store and recall information"
        };
        return definitions[(int) (Math.random() * definitions.length)];
    }

    /**
     * Creates a set of random words for testing collections.
     */
    public static Set<String> createRandomWordSet(int size) {
        Set<String> wordSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            wordSet.add(createRandomWord() + "_" + i);
        }
        return wordSet;
    }

    /**
     * Creates a future date for testing scheduling and deadlines.
     */
    public static LocalDateTime createFutureDate(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow);
    }

    /**
     * Creates a past date for testing historical data.
     */
    public static LocalDateTime createPastDate(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    /**
     * Creates a valid password for testing.
     */
    public static String createValidPassword() {
        return "TestPassword123!";
    }

    /**
     * Creates an invalid password for testing validation.
     */
    public static String createInvalidPassword() {
        return "weak";
    }

    /**
     * Creates a collection name for testing.
     */
    public static String createCollectionName() {
        String[] names = {
            "Basic Vocabulary",
            "Advanced Words",
            "Business English",
            "Technical Terms",
            "Daily Conversation",
            "Academic Vocabulary"
        };
        return names[(int) (Math.random() * names.length)];
    }
}