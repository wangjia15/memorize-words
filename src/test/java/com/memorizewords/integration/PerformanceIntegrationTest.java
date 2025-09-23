package com.memorizewords.integration;

import com.memorizewords.performance.*;
import com.memorizewords.service.ReviewSessionService;
import com.memorizewords.service.SpacedRepetitionService;
import com.memorizewords.service.WordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance integration tests for the memorize-words application.
 * Tests performance of core operations under load.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PerformanceIntegrationTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceIntegrationTest.class);
    private static final int PERFORMANCE_TIMEOUT_SECONDS = 30;

    @Autowired
    private WordService wordService;

    @Autowired
    private ReviewSessionService reviewSessionService;

    @Autowired
    private SpacedRepetitionService spacedRepetitionService;

    @Autowired
    private MemoryLeakDetector memoryLeakDetector;

    @BeforeEach
    void setUp() {
        // Clear any existing test data
        memoryLeakDetector.clearSnapshots();
    }

    @Test
    @Timeout(value = PERFORMANCE_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testVocabularyLoadPerformance() {
        logger.info("Testing vocabulary load performance...");

        // Create test user
        User user = createTestUser();
        userRepository.save(user);

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Vocabulary Load Performance",
            5,  // warmup iterations
            50, // measurement iterations
            10, // concurrent users
            25  // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            // Add a word to vocabulary
            wordService.addWordToUserVocabulary(user.getId(), "performance_test_word", "test definition");
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            2000,  // max response time ms
            20.0,  // min throughput per second
            0.05   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Vocabulary load test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = PERFORMANCE_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testConcurrentReviewSessions() {
        logger.info("Testing concurrent review sessions...");

        // Create test user with vocabulary
        User user = createTestUser();
        userRepository.save(user);

        // Create test vocabulary
        IntStream.range(0, 100).forEach(i -> {
            wordService.addWordToUserVocabulary(user.getId(), "testword_" + i, "Definition " + i);
        });

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Concurrent Review Sessions",
            3,   // warmup iterations
            20,  // measurement iterations
            50,  // concurrent users
            100  // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            reviewSessionService.startReviewSession(user.getId(), "DUE_CARDS", 10);
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            3000,  // max response time ms
            15.0,  // min throughput per second
            0.1    // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Concurrent review sessions test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = PERFORMANCE_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testSpacedRepetitionPerformance() {
        logger.info("Testing spaced repetition performance...");

        // Create test user
        User user = createTestUser();
        userRepository.save(user);

        // Create test vocabulary
        IntStream.range(0, 200).forEach(i -> {
            wordService.addWordToUserVocabulary(user.getId(), "spacedword_" + i, "Spaced definition " + i);
        });

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Spaced Repetition Performance",
            5,   // warmup iterations
            100, // measurement iterations
            20,  // concurrent users
            50   // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            spacedRepetitionService.getDueCards(user.getId(), 50);
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            1500,  // max response time ms
            30.0,  // min throughput per second
            0.02   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Spaced repetition performance test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = PERFORMANCE_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testDatabaseConnectionPoolPerformance() {
        logger.info("Testing database connection pool performance...");

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Database Connection Pool Performance",
            10,  // warmup iterations
            100, // measurement iterations
            20,  // concurrent users
            100  // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            // Create and retrieve users to test connection pool
            User user = createTestUser();
            userRepository.save(user);
            userRepository.findById(user.getId()).orElseThrow();
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            1000,  // max response time ms
            50.0,  // min throughput per second
            0.01   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Database connection pool test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testMemoryLeakDetection() {
        logger.info("Testing memory leak detection...");

        // Take initial memory snapshot
        MemoryLeakDetector.MemorySnapshot initialSnapshot = memoryLeakDetector.takeCleanSnapshot();

        // Run memory-intensive operations
        Runnable memoryIntensiveOperation = () -> {
            User user = createTestUser();
            userRepository.save(user);

            // Create many words
            IntStream.range(0, 50).forEach(i -> {
                wordService.addWordToUserVocabulary(user.getId(), "memleakword_" + i, "Memory leak definition " + i);
            });

            // Perform spaced repetition operations
            spacedRepetitionService.getDueCards(user.getId(), 100);
        };

        // Run leak detection test
        MemoryLeakDetector.MemoryLeakTestResult leakResult = memoryLeakDetector.runLeakTest(
            memoryIntensiveOperation, 30000 // 30 seconds
        );

        // Validate against memory leak thresholds
        leakResult.validateAgainstThresholds(10.0, 10485760); // 10% growth, 10MB/hour

        logger.info("Memory leak detection test passed:");
        logger.info("Heap growth: {} bytes ({:.2f}%)",
            leakResult.getHeapGrowthBytes(), leakResult.getHeapGrowthPercent());
        logger.info("Growth rate: {:.2f} bytes/hour", leakResult.getGrowthRatePerHour());
        logger.info("Objects created: {}, destroyed: {}, net growth: {}",
            leakResult.getObjectsCreated(), leakResult.getObjectsDestroyed(), leakResult.getNetObjectGrowth());

        assertFalse(leakResult.isLeakDetected(), "Memory leak detected!");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testBulkOperationsPerformance() {
        logger.info("Testing bulk operations performance...");

        // Create test user
        User user = createTestUser();
        userRepository.save(user);

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Bulk Operations Performance",
            3,   // warmup iterations
            10,  // measurement iterations
            5,   // concurrent users
            10   // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            // Simulate bulk word addition
            List<String> words = IntStream.range(0, 100)
                .mapToObj(i -> "bulkword_" + i)
                .toList();

            for (String word : words) {
                wordService.addWordToUserVocabulary(user.getId(), word, "Bulk definition for " + word);
            }
        });

        // Validate performance thresholds (bulk operations expected to be slower)
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            10000, // max response time ms (10 seconds for bulk operations)
            1.0,   // min throughput per second
            0.01   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Bulk operations performance test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = PERFORMANCE_TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    void testSearchPerformanceUnderLoad() {
        logger.info("Testing search performance under load...");

        // Create test user with large vocabulary
        User user = createTestUser();
        userRepository.save(user);

        // Create large vocabulary for search testing
        IntStream.range(0, 500).forEach(i -> {
            wordService.addWordToUserVocabulary(user.getId(), "searchword_" + i, "Search definition " + i);
        });

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Search Performance Under Load",
            5,   // warmup iterations
            100, // measurement iterations
            30,  // concurrent users
            75   // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            // Simulate search operations
            wordService.searchWords("search", 0, 20);
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            2000,  // max response time ms
            40.0,  // min throughput per second
            0.03   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Search performance test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testConcurrentUserRegistration() {
        logger.info("Testing concurrent user registration...");

        // Performance test configuration
        BasePerformanceTest.PerformanceTestConfig config = new BasePerformanceTest.PerformanceTestConfig(
            "Concurrent User Registration",
            5,   // warmup iterations
            50,  // measurement iterations
            20,  // concurrent users
            50   // requests per second
        );

        // Execute performance test
        PerformanceResult result = executePerformanceTest(config, () -> {
            User user = createTestUser();
            userRepository.save(user);
        });

        // Validate performance thresholds
        BasePerformanceTest.PerformanceThresholds thresholds = new BasePerformanceTest.PerformanceThresholds(
            1500,  // max response time ms
            25.0,  // min throughput per second
            0.02   // max error rate
        );

        validatePerformanceThresholds(result, thresholds);
        logger.info("Concurrent user registration test passed: {}", result.generateReport());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testPerformanceRegressionTest() {
        logger.info("Running comprehensive performance regression test...");

        // Run all performance tests in sequence
        assertDoesNotThrow(() -> testVocabularyLoadPerformance());
        assertDoesNotThrow(() -> testConcurrentReviewSessions());
        assertDoesNotThrow(() -> testSpacedRepetitionPerformance());
        assertDoesNotThrow(() -> testDatabaseConnectionPoolPerformance());
        assertDoesNotThrow(() -> testMemoryLeakDetection());
        assertDoesNotThrow(() -> testBulkOperationsPerformance());
        assertDoesNotThrow(() -> testSearchPerformanceUnderLoad());
        assertDoesNotThrow(() -> testConcurrentUserRegistration());

        logger.info("All performance regression tests passed successfully!");
    }
}