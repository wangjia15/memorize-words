package com.memorizewords.load;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * High load stress testing simulation.
 * Tests the system under heavy load (1000+ concurrent users).
 */
public class HighLoadStressTest extends BaseLoadSimulation {

    @Override
    public void before() {
        super.initializeBaseSimulation();
    }

    {
        // High load configuration
        int concurrentUsers = Integer.parseInt(System.getProperty("load.test.concurrentUsers", "1000"));
        int rampUpSeconds = Integer.parseInt(System.getProperty("load.test.rampUpSeconds", "60"));
        int durationSeconds = Integer.parseInt(System.getProperty("load.test.durationSeconds", "300"));

        // Define the high load scenario
        ScenarioBuilder scn = scenario("High Load Stress Test")
            .exec(flushSession())
            .exec(session -> {
                // Use a pool of test users to simulate realistic load
                int userId = ThreadLocalRandom.current().nextInt(1, 1000);
                return session.set("user_id", userId)
                            .set("username", "stressuser_" + userId)
                            .set("password", "stresspass_" + userId);
            })

            // Fast health check - many users will do this
            .exec(healthCheck())
            .pause(createRandomPause(100, 500))

            // Attempt login (most will fail in stress test, but some will succeed)
            .tryOn(
                exec(loginUser("#{username}", "#{password}"))
            ).exitHereIfFailed()
            .pause(createRandomPause(500, 1500))

            // For users that successfully logged in, perform various operations
            .doIf(session -> session.contains("auth_token")).then(
                // Search operations (high frequency)
                .repeat(10).on(
                    exec(session -> {
                        String searchTerm = "search_" + ThreadLocalRandom.current().nextInt(10000);
                        return session.set("search_term", searchTerm);
                    })
                    .exec(searchWords("#{search_term}", "#{auth_token}"))
                    .pause(createRandomPause(200, 800))
                )

                // Vocabulary operations
                .repeat(5).on(
                    exec(session -> {
                        String word = "stressword_" + ThreadLocalRandom.current().nextInt(1000000);
                        String definition = "Stress definition " + ThreadLocalRandom.current().nextInt(1000000);
                        return session.set("current_word", word)
                                    .set("current_definition", definition);
                    })
                    .tryOn(
                        exec(addWord("#{current_word}", "#{current_definition}", "#{auth_token}"))
                    ).exitHereIfFailed()
                    .pause(createRandomPause(300, 1200))
                )

                // Review operations
                .exec(getReviewStatistics("#{auth_token}"))
                .pause(createRandomPause(500, 1500))

                .exec(getDueCards(50, "#{auth_token}"))
                .pause(createRandomPause(300, 1000))

                // Start and complete review sessions
                .repeat(3).on(
                    exec(startReviewSession("DUE_CARDS", 20, "#{auth_token}"))
                    .pause(createRandomPause(500, 2000))

                    .repeat(5).on(
                        exec(session -> {
                            long cardId = ThreadLocalRandom.current().nextLong(1, 5000);
                            String[] outcomes = {"GOOD", "EASY", "HARD", "AGAIN"};
                            String outcome = outcomes[ThreadLocalRandom.current().nextInt(outcomes.length)];
                            return session.set("card_id", cardId)
                                        .set("outcome", outcome);
                        })
                        .tryOn(
                            exec(submitReview(#{session_id}, "#{card_id}", "#{outcome}", "#{auth_token}"))
                        ).exitHereIfFailed()
                        .pause(createRandomPause(1000, 3000))
                    )
                )
            )

            // For users that failed to login, continue with public endpoints
            .doIf(session -> !session.contains("auth_token")).then(
                // Health checks for unauthenticated users
                .repeat(20).on(
                    exec(healthCheck())
                    .pause(createRandomPause(100, 400))
                )
            );

        // Create population with high load
        PopulationBuilder population = createPopulation(scn, concurrentUsers, rampUpSeconds, durationSeconds);

        // Set up the simulation with relaxed assertions for stress testing
        setUp(
            population
        ).protocols(httpProtocol)
        .assertions(
            // Stress test allows higher response times and lower success rates
            global().responseTime().max().lt(5000),
            global().successfulRequests().percent().gt(90.0),
            global().requestsPerSec().gt(50.0)
        );
    }
}