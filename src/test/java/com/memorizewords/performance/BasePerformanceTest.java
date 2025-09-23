package com.memorizewords.performance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for all performance tests.
 * Provides common setup and teardown functionality for performance testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePerformanceTest {

    protected PerformanceTestContext testContext;

    @BeforeAll
    public void setUpBasePerformanceTest() {
        testContext = new PerformanceTestContext();
        testContext.initialize();
    }

    @BeforeEach
    public void beforeEachPerformanceTest() {
        testContext.reset();
    }

    /**
     * Execute a performance test with the given parameters.
     */
    protected PerformanceResult executePerformanceTest(PerformanceTestConfig config, PerformanceTestRunnable testRunnable) {
        return testContext.executeTest(config, testRunnable);
    }

    /**
     * Validate that performance meets the expected thresholds.
     */
    protected void validatePerformanceThresholds(PerformanceResult result, PerformanceThresholds thresholds) {
        result.validateAgainstThresholds(thresholds);
    }

    /**
     * Configuration for a performance test run.
     */
    protected static class PerformanceTestConfig {
        private final String testName;
        private final int warmupIterations;
        private final int measurementIterations;
        private final int concurrentUsers;
        private final int requestsPerSecond;

        public PerformanceTestConfig(String testName, int warmupIterations, int measurementIterations,
                                    int concurrentUsers, int requestsPerSecond) {
            this.testName = testName;
            this.warmupIterations = warmupIterations;
            this.measurementIterations = measurementIterations;
            this.concurrentUsers = concurrentUsers;
            this.requestsPerSecond = requestsPerSecond;
        }

        // Getters
        public String getTestName() { return testName; }
        public int getWarmupIterations() { return warmupIterations; }
        public int getMeasurementIterations() { return measurementIterations; }
        public int getConcurrentUsers() { return concurrentUsers; }
        public int getRequestsPerSecond() { return requestsPerSecond; }
    }

    /**
     * Interface for performance test implementations.
     */
    @FunctionalInterface
    protected interface PerformanceTestRunnable {
        void run() throws Exception;
    }

    /**
     * Performance thresholds for validation.
     */
    protected static class PerformanceThresholds {
        private final long maxResponseTimeMs;
        private final double minThroughputPerSecond;
        private final double maxErrorRate;

        public PerformanceThresholds(long maxResponseTimeMs, double minThroughputPerSecond, double maxErrorRate) {
            this.maxResponseTimeMs = maxResponseTimeMs;
            this.minThroughputPerSecond = minThroughputPerSecond;
            this.maxErrorRate = maxErrorRate;
        }

        // Getters
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public double getMinThroughputPerSecond() { return minThroughputPerSecond; }
        public double getMaxErrorRate() { return maxErrorRate; }
    }
}