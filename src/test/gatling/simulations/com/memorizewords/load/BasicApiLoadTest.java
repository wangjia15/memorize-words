package com.memorizewords.load;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Basic API load testing simulation.
 * Tests core API endpoints under normal load conditions.
 */
public class BasicApiLoadTest extends BaseLoadSimulation {

    @Override
    public void before() {
        super.initializeBaseSimulation();
    }

    {
        // Test configuration
        int concurrentUsers = Integer.parseInt(System.getProperty("load.test.concurrentUsers", "50"));
        int rampUpSeconds = Integer.parseInt(System.getProperty("load.test.rampUpSeconds", "10"));
        int durationSeconds = Integer.parseInt(System.getProperty("load.test.durationSeconds", "60"));

        // Define the scenario
        ScenarioBuilder scn = scenario("Basic API Load Test")
            .exec(flushSession())
            .exec(session -> {
                String username = "testuser_" + ThreadLocalRandom.current().nextInt(1000000);
                String password = "password123";
                return session.set("username", username)
                            .set("password", password);
            })

            // User Registration
            .exec(registerUser("#{username}", "#{password}"))
            .pause(createThinkTime())

            // User Login
            .exec(loginUser("#{username}", "#{password}"))
            .pause(createThinkTime())

            // Health Check
            .exec(healthCheck())
            .pause(createRandomPause(500, 1500))

            // Add Words to Vocabulary
            .repeat(5).on(
                exec(session -> {
                    String word = "word_" + ThreadLocalRandom.current().nextInt(1000000);
                    String definition = "Definition of " + word;
                    return session.set("current_word", word)
                                .set("current_definition", definition);
                })
                .exec(addWord("#{current_word}", "#{current_definition}", "#{auth_token}"))
                .pause(createThinkTime())
            )

            // Search Words
            .repeat(3).on(
                exec(session -> {
                    String searchTerm = "search_" + ThreadLocalRandom.current().nextInt(1000);
                    return session.set("search_term", searchTerm);
                })
                .exec(searchWords("#{search_term}", "#{auth_token}"))
                .pause(createRandomPause(1000, 2000))
            )

            // Get Review Statistics
            .exec(getReviewStatistics("#{auth_token}"))
            .pause(createThinkTime())

            // Get Due Cards
            .exec(getDueCards(20, "#{auth_token}"))
            .pause(createThinkTime())

            // Start Review Session
            .exec(startReviewSession("DUE_CARDS", 10, "#{auth_token}"))
            .pause(createThinkTime())

            // Submit Reviews (simulate review activity)
            .repeat(3).on(
                exec(session -> {
                    long cardId = ThreadLocalRandom.current().nextLong(1, 1000);
                    String[] outcomes = {"GOOD", "EASY", "HARD", "AGAIN"};
                    String outcome = outcomes[ThreadLocalRandom.current().nextInt(outcomes.length)];
                    return session.set("card_id", cardId)
                                .set("outcome", outcome);
                })
                .exec(submitReview(#{session_id}, "#{card_id}", "#{outcome}", "#{auth_token}"))
                .pause(createRandomPause(2000, 4000))
            );

        // Set up the population
        PopulationBuilder population = createPopulation(scn, concurrentUsers, rampUpSeconds, durationSeconds);

        // Set up assertions for performance validation
        setUp(
            population
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().max().lt(2000),
            global().successfulRequests().percent().gt(95.0),
            global().requestsPerSec().gt(10.0)
        );
    }
}