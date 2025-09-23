package com.memorizewords.load;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Memory leak detection simulation.
 * Long-running test to detect memory leaks in the application.
 */
public class MemoryLeakDetectionTest extends BaseLoadSimulation {

    @Override
    public void before() {
        super.initializeBaseSimulation();
    }

    {
        // Memory leak test configuration - long duration with moderate load
        int concurrentUsers = Integer.parseInt(System.getProperty("load.test.concurrentUsers", "100"));
        int rampUpSeconds = Integer.parseInt(System.getProperty("load.test.rampUpSeconds", "30"));
        int durationSeconds = Integer.parseInt(System.getProperty("load.test.durationSeconds", "1800")); // 30 minutes

        // Define the memory leak detection scenario
        ScenarioBuilder scn = scenario("Memory Leak Detection Test")
            .exec(flushSession())
            .exec(session -> {
                int userId = ThreadLocalRandom.current().nextInt(1, 5000);
                return session.set("user_id", userId)
                            .set("username", "memleakuser_" + userId)
                            .set("password", "memleakpass_" + userId);
            })

            // Login attempt (some will succeed, some won't)
            .tryOn(
                exec(loginUser("#{username}", "#{password}"))
            ).exitHereIfFailed()
            .pause(createRandomPause(1000, 3000))

            // Intensive memory operations for users that logged in
            .doIf(session -> session.contains("auth_token")).then(
                // Continuous word creation (can cause memory leaks if not properly managed)
                .repeat(50).on(
                    exec(session -> {
                        String word = "memleakword_" + ThreadLocalRandom.current().nextInt(10000000);
                        String definition = "Memory leak test definition " +
                            ThreadLocalRandom.current().nextInt(10000000) + " " +
                            ThreadLocalRandom.current().nextInt(10000000);
                        return session.set("current_word", word)
                                    .set("current_definition", definition);
                    })
                    .tryOn(
                        exec(addWord("#{current_word}", "#{current_definition}", "#{auth_token}"))
                    ).exitHereIfFailed()
                    .pause(createRandomPause(500, 1500))
                )

                // Intensive search operations with varying parameters
                .repeat(100).on(
                    exec(session -> {
                        String searchTerm = "memleaksearch_" + ThreadLocalRandom.current().nextInt(100000);
                        int pageSize = ThreadLocalRandom.current().nextInt(10, 200);
                        return session.set("search_term", searchTerm)
                                    .set("page_size", pageSize);
                    })
                    .exec(searchWords("#{search_term}", "#{auth_token}")
                        .queryParam("size", "#{page_size}"))
                    .pause(createRandomPause(200, 800))
                )

                // Large vocabulary retrieval operations
                .repeat(20).on(
                    exec(session -> {
                        int pageSize = ThreadLocalRandom.current().nextInt(100, 500);
                        return session.set("large_page_size", pageSize);
                    })
                    .exec(http("Get Large Vocabulary")
                        .get("/api/vocabulary")
                        .headers(createAuthHeaders("#{auth_token}"))
                        .queryParam("page", "0")
                        .queryParam("size", "#{large_page_size}")
                        .check(status().is(200)))
                    .pause(createRandomPause(1000, 3000))
                )

                // Continuous review session operations
                .repeat(30).on(
                    exec(startReviewSession("DUE_CARDS", ThreadLocalRandom.current().nextInt(10, 50), "#{auth_token}"))
                    .pause(createRandomPause(1000, 4000))

                    .repeat(ThreadLocalRandom.current().nextInt(5, 20)).on(
                        exec(session -> {
                            long cardId = ThreadLocalRandom.current().nextLong(1, 10000);
                            String[] outcomes = {"GOOD", "EASY", "HARD", "AGAIN"};
                            String outcome = outcomes[ThreadLocalRandom.current().nextInt(outcomes.length)];
                            int responseTime = ThreadLocalRandom.current().nextInt(500, 15000);
                            return session.set("card_id", cardId)
                                        .set("outcome", outcome)
                                        .set("response_time", responseTime);
                        })
                        .exec(http("Submit Review")
                            .post("/api/reviews/#{session_id}/submit")
                            .headers(createAuthHeaders("#{auth_token}"))
                            .body(StringBody("""
                                {
                                    "cardId": #{card_id},
                                    "outcome": "#{outcome}",
                                    "responseTime": #{response_time}
                                }
                                """))
                            .check(status().is(200)))
                        .pause(createRandomPause(1500, 5000))
                    )
                )

                // Bulk operations that can stress memory management
                .repeat(5).on(
                    exec(session -> {
                        StringBuilder bulkWords = new StringBuilder();
                        bulkWords.append("{\"words\": [");
                        int wordCount = ThreadLocalRandom.current().nextInt(10, 50);
                        for (int i = 0; i < wordCount; i++) {
                            if (i > 0) bulkWords.append(",");
                            bulkWords.append(String.format("""
                                {
                                    "word": "bulk_%d_%d",
                                    "definition": "Bulk definition %d",
                                    "partOfSpeech": "noun",
                                    "difficulty": %.1f,
                                    "frequency": %d
                                }""",
                                ThreadLocalRandom.current().nextInt(1000000),
                                i, i,
                                ThreadLocalRandom.current().nextDouble() * 2.0,
                                ThreadLocalRandom.current().nextInt(1, 5000)
                            ));
                        }
                        bulkWords.append("]}");
                        return session.set("bulk_words", bulkWords.toString());
                    })
                    .exec(http("Bulk Import Words")
                        .post("/api/words/bulk-import")
                        .headers(createAuthHeaders("#{auth_token}"))
                        .body(StringBody("#{bulk_words}"))
                        .check(status().is(200)))
                    .pause(createRandomPause(3000, 8000))
                )

                // Statistics and history operations with large data sets
                .repeat(10).on(
                    exec(http("Get Review History")
                        .get("/api/reviews/history")
                        .headers(createAuthHeaders("#{auth_token}"))
                        .queryParam("page", "0")
                        .queryParam("size", "1000")
                        .check(status().is(200)))
                    .pause(createRandomPause(1000, 3000))

                    .exec(getReviewStatistics("#{auth_token}"))
                    .pause(createRandomPause(500, 1500))
                )

                // Cleanup operations
                .exec(http("Get User Progress")
                    .get("/api/users/progress")
                    .headers(createAuthHeaders("#{auth_token}"))
                    .check(status().is(200)))
                .pause(createRandomPause(2000, 5000))
            )

            // For users that failed to login, perform public operations
            .doIf(session -> !session.contains("auth_token")).then(
                // Continuous health checks (can reveal connection pool leaks)
                .repeat(200).on(
                    exec(healthCheck())
                    .pause(createRandomPause(100, 500))
                )

                // Public search operations
                .repeat(50).on(
                    exec(session -> {
                        String searchTerm = "publicsearch_" + ThreadLocalRandom.current().nextInt(10000);
                        return session.set("public_search_term", searchTerm);
                    })
                    .exec(http("Public Search")
                        .get("/api/words/search")
                        .headers(createCommonHeaders())
                        .queryParam("query", "#{public_search_term}")
                        .queryParam("page", "0")
                        .queryParam("size", "20")
                        .check(status().is(200)))
                    .pause(createRandomPause(500, 1500))
                )
            );

        // Create population for memory leak testing
        PopulationBuilder population = createPopulation(scn, concurrentUsers, rampUpSeconds, durationSeconds);

        // Set up the simulation with memory leak detection in mind
        setUp(
            population
        ).protocols(httpProtocol)
        .assertions(
            // Memory leak tests allow higher response times due to GC activity
            global().responseTime().max().lt(10000),
            global().successfulRequests().percent().gt(85.0),
            global().requestsPerSec().gt(5.0)
        );
    }
}