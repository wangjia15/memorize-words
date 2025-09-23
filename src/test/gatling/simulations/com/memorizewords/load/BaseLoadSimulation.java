package com.memorizewords.load;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Base class for all Gatling load simulations.
 * Provides common configuration and utilities.
 */
public abstract class BaseLoadSimulation extends Simulation {

    protected HttpProtocolBuilder httpProtocol;
    protected String baseUrl;
    protected int defaultTimeoutMs;
    protected boolean followRedirects;

    /**
     * Initialize the base simulation with configuration.
     */
    protected void initializeBaseSimulation() {
        // Load configuration from environment or use defaults
        this.baseUrl = System.getProperty("load.test.baseUrl", "http://localhost:8080");
        this.defaultTimeoutMs = Integer.parseInt(System.getProperty("load.test.timeout", "30000"));
        this.followRedirects = Boolean.parseBoolean(System.getProperty("load.test.followRedirects", "true"));

        // Configure HTTP protocol
        this.httpProtocol = http
            .baseUrl(baseUrl)
            .acceptHeader("application/json")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .userAgentHeader("MemorizeWords-LoadTest/1.0")
            .timeout(Duration.ofMillis(defaultTimeoutMs))
            .disableFollowRedirects(!followRedirects)
            .check(status().in(200, 201, 204, 400, 401, 404)); // Allow expected HTTP status codes
    }

    /**
     * Create a scenario with the given name and configuration.
     */
    protected ScenarioBuilder createScenario(String name, int concurrentUsers, int rampUpSeconds, int durationSeconds) {
        return scenario(name)
            .exec(flushSession())
            .exec(session -> {
                session.set("base_url", baseUrl);
                session.set("timeout_ms", defaultTimeoutMs);
                return session;
            });
    }

    /**
     * Create a population configuration for the given scenario.
     */
    protected PopulationBuilder createPopulation(ScenarioBuilder scenario, int concurrentUsers, int rampUpSeconds, int durationSeconds) {
        return scenario.injectOpen(
            rampUsersPerSec(1).to((double) concurrentUsers / rampUpSeconds).during(Duration.ofSeconds(rampUpSeconds)),
            constantUsersPerSec(concurrentUsers).during(Duration.ofSeconds(durationSeconds))
        );
    }

    /**
     * Create common headers for API requests.
     */
    protected Map<String, String> createCommonHeaders() {
        return Map.of(
            "Content-Type", "application/json",
            "Accept", "application/json",
            "X-Load-Test", "true"
        );
    }

    /**
     * Create authentication headers.
     */
    protected Map<String, String> createAuthHeaders(String token) {
        Map<String, String> headers = createCommonHeaders();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    /**
     * Create a pause between requests with randomization.
     */
    protected PauseBuilder createRandomPause(int minMs, int maxMs) {
        return pace(Duration.ofMillis(minMs), Duration.ofMillis(maxMs));
    }

    /**
     * Create a think time simulation.
     */
    protected PauseBuilder createThinkTime() {
        return createRandomPause(1000, 3000);
    }

    /**
     * Add common checks to a request.
     */
    protected ChainBuilder withCommonChecks(ChainBuilder chain) {
        return chain
            .check(status().saveAs("status"))
            .check(responseTimeInMillis().saveAs("response_time"))
            .check(jsonPath("$.error").optional().saveAs("error"));
    }

    /**
     * Create a request to register a new user.
     */
    protected HttpRequestActionBuilder registerUser(String username, String password) {
        return http("Register User")
            .post("/api/auth/register")
            .headers(createCommonHeaders())
            .body(StringBody("""
                {
                    "username": "%s",
                    "email": "%s@example.com",
                    "password": "%s"
                }
                """.formatted(username, username, password)))
            .check(status().is(201))
            .check(jsonPath("$.data.accessToken").saveAs("auth_token"))
            .check(jsonPath("$.data.user.id").saveAs("user_id"));
    }

    /**
     * Create a request to login a user.
     */
    protected HttpRequestActionBuilder loginUser(String username, String password) {
        return http("Login User")
            .post("/api/auth/login")
            .headers(createCommonHeaders())
            .body(StringBody("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password)))
            .check(status().is(200))
            .check(jsonPath("$.data.accessToken").saveAs("auth_token"))
            .check(jsonPath("$.data.user.id").saveAs("user_id"));
    }

    /**
     * Create a request to add a word to vocabulary.
     */
    protected HttpRequestActionBuilder addWord(String word, String definition, String token) {
        return http("Add Word")
            .post("/api/words")
            .headers(createAuthHeaders(token))
            .body(StringBody("""
                {
                    "word": "%s",
                    "definition": "%s",
                    "partOfSpeech": "noun",
                    "difficulty": 0.5,
                    "frequency": 1000
                }
                """.formatted(word, definition)))
            .check(status().is(201))
            .check(jsonPath("$.data.id").saveAs("word_id"));
    }

    /**
     * Create a request to search for words.
     */
    protected HttpRequestActionBuilder searchWords(String query, String token) {
        return http("Search Words")
            .get("/api/words/search")
            .headers(createAuthHeaders(token))
            .queryParam("query", query)
            .queryParam("page", "0")
            .queryParam("size", "20")
            .check(status().is(200));
    }

    /**
     * Create a request to start a review session.
     */
    protected HttpRequestActionBuilder startReviewSession(String mode, int limit, String token) {
        return http("Start Review Session")
            .post("/api/reviews/start")
            .headers(createAuthHeaders(token))
            .body(StringBody("""
                {
                    "mode": "%s",
                    "limit": %d
                }
                """.formatted(mode, limit)))
            .check(status().is(200))
            .check(jsonPath("$.data.id").saveAs("session_id"));
    }

    /**
     * Create a request to submit a review.
     */
    protected HttpRequestActionBuilder submitReview(long sessionId, long cardId, String outcome, String token) {
        return http("Submit Review")
            .post("/api/reviews/" + sessionId + "/submit")
            .headers(createAuthHeaders(token))
            .body(StringBody("""
                {
                    "cardId": %d,
                    "outcome": "%s",
                    "responseTime": 3000
                }
                """.formatted(cardId, outcome)))
            .check(status().is(200));
    }

    /**
     * Create a request to get due cards.
     */
    protected HttpRequestActionBuilder getDueCards(int limit, String token) {
        return http("Get Due Cards")
            .get("/api/reviews/due-cards")
            .headers(createAuthHeaders(token))
            .queryParam("limit", limit)
            .check(status().is(200));
    }

    /**
     * Create a request to get review statistics.
     */
    protected HttpRequestActionBuilder getReviewStatistics(String token) {
        return http("Get Review Statistics")
            .get("/api/reviews/statistics")
            .headers(createAuthHeaders(token))
            .check(status().is(200));
    }

    /**
     * Create a health check request.
     */
    protected HttpRequestActionBuilder healthCheck() {
        return http("Health Check")
            .get("/api/health")
            .check(status().is(200));
    }
}