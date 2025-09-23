package com.memorizewords.controller;

import com.memorizewords.dto.request.StartReviewSessionRequest;
import com.memorizewords.dto.request.SubmitReviewRequest;
import com.memorizewords.dto.request.UpdatePreferencesRequest;
import com.memorizewords.dto.response.ApiResponse;
import com.memorizewords.dto.response.DueCardsResponse;
import com.memorizewords.dto.response.ReviewInsightsDTO;
import com.memorizewords.dto.response.ReviewSessionDTO;
import com.memorizewords.dto.response.ReviewStatisticsDTO;
import com.memorizewords.dto.response.UserReviewPreferencesDTO;
import com.memorizewords.entity.ReviewSession;
import com.memorizewords.entity.SpacedRepetitionCard;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.ReviewOutcome;
import com.memorizewords.enums.WordType;
import com.memorizewords.repository.ReviewSessionRepository;
import com.memorizewords.repository.SpacedRepetitionCardRepository;
import com.memorizewords.repository.UserRepository;
import com.memorizewords.repository.WordRepository;
import com.memorizewords.service.SpacedRepetitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for SpacedRepetitionController demonstrating Stream B API endpoints.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
public class SpacedRepetitionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private SpacedRepetitionCardRepository cardRepository;

    @Autowired
    private ReviewSessionRepository sessionRepository;

    @Autowired
    private SpacedRepetitionService spacedRepetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private List<SpacedRepetitionCard> testCards;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create test words and cards
        List<Word> testWords = List.of(
            createWord("hello", "hola", WordType.NOUN),
            createWord("goodbye", "adiós", WordType.NOUN),
            createWord("thank you", "gracias", WordType.PHRASE)
        );
        testWords = wordRepository.saveAll(testWords);

        testCards = testWords.stream()
            .map(word -> spacedRepetitionService.createCard(testUser, word))
            .toList();
        testCards = cardRepository.saveAll(testCards);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testStartReviewSession() throws Exception {
        // Given
        StartReviewSessionRequest request = StartReviewSessionRequest.builder()
            .mode(ReviewMode.DUE_CARDS)
            .limit(5)
            .shuffleCards(true)
            .build();

        // When
        var result = mockMvc.perform(post("/api/spaced-repetition/sessions/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.mode").value("DUE_CARDS"))
            .andExpect(jsonPath("$.data.totalCards").value(5))
            .andExpect(jsonPath("$.data.isCompleted").value(false))
            .andExpect(jsonPath("$.data.completedCards").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSubmitReview() throws Exception {
        // Given - start a session first
        StartReviewSessionRequest startRequest = StartReviewSessionRequest.builder()
            .mode(ReviewMode.DUE_CARDS)
            .limit(2)
            .build();

        String startResponse = mockMvc.perform(post("/api/spaced-repetition/sessions/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(startRequest)))
            .andReturn().getResponse().getContentAsString();

        ReviewSessionDTO session = objectMapper.readValue(startResponse, ApiResponse.class)
            .getData("data", ReviewSessionDTO.class);

        SubmitReviewRequest submitRequest = SubmitReviewRequest.builder()
            .sessionId(session.getId())
            .cardId(testCards.get(0).getId())
            .outcome(ReviewOutcome.GOOD)
            .responseTime(2500)
            .build();

        // When
        var result = mockMvc.perform(post("/api/spaced-repetition/sessions/" + session.getId() + "/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(submitRequest)));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.completedCards").value(1))
            .andExpect(jsonPath("$.data.correctAnswers").value(1))
            .andExpect(jsonPath("$.data.averageResponseTime").value(2500));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCompleteSession() throws Exception {
        // Given - start and complete a session
        StartReviewSessionRequest startRequest = StartReviewSessionRequest.builder()
            .mode(ReviewMode.DUE_CARDS)
            .limit(2)
            .build();

        String startResponse = mockMvc.perform(post("/api/spaced-repetition/sessions/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(startRequest)))
            .andReturn().getResponse().getContentAsString();

        ReviewSessionDTO session = objectMapper.readValue(startResponse, ApiResponse.class)
            .getData("data", ReviewSessionDTO.class);

        // When
        var result = mockMvc.perform(post("/api/spaced-repetition/sessions/" + session.getId() + "/complete"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.isCompleted").value(true))
            .andExpect(jsonPath("$.data.endTime").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetActiveSession() throws Exception {
        // Given - start a session
        StartReviewSessionRequest request = StartReviewSessionRequest.builder()
            .mode(ReviewMode.NEW_CARDS)
            .limit(3)
            .build();

        mockMvc.perform(post("/api/spaced-repetition/sessions/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/sessions/active"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.mode").value("NEW_CARDS"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDueCards() throws Exception {
        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/cards/due")
            .param("limit", "10"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.dueCards").exists())
            .andExpect(jsonPath("$.data.totalDue").exists())
            .andExpect(jsonPath("$.data.recommendedLimit").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReviewStatistics() throws Exception {
        // Given - create some review activity
        ReviewSession session = new ReviewSession();
        session.setUser(testUser);
        session.setMode(ReviewMode.DUE_CARDS);
        session.setStartTime(java.time.LocalDateTime.now());
        session.setEndTime(java.time.LocalDateTime.now().plusMinutes(5));
        session.setTotalCards(2);
        session.setCompletedCards(2);
        session.setCorrectAnswers(2);
        session.setIsCompleted(true);
        session.setSessionAccuracy(100.0);
        session.setSessionDuration(300L);
        sessionRepository.save(session);

        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/statistics")
            .param("from", from.toString())
            .param("to", to.toString()));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
            .andExpect(jsonPath("$.data.periodStart").exists())
            .andExpect(jsonPath("$.data.periodEnd").exists())
            .andExpect(jsonPath("$.data.totalReviews").exists())
            .andExpect(jsonPath("$.data.averageAccuracy").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReviewInsights() throws Exception {
        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/analytics/insights"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.learningVelocity").exists())
            .andExpect(jsonPath("$.data.retentionTrend").exists())
            .andExpect(jsonPath("$.data.optimalStudyTimes").exists())
            .andExpect(jsonPath("$.data.recommendations").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserPreferences() throws Exception {
        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/preferences"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
            .andExpect(jsonPath("$.data.dailyReviewLimit").exists())
            .andExpect(jsonPath("$.data.sessionGoal").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateUserPreferences() throws Exception {
        // Given
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
            .dailyReviewLimit(40)
            .dailyNewCardLimit(8)
            .sessionGoal(25)
            .enableNotifications(false)
            .useAdvancedAlgorithm(true)
            .build();

        // When
        var result = mockMvc.perform(put("/api/spaced-repetition/preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.dailyReviewLimit").value(40))
            .andExpect(jsonPath("$.data.dailyNewCardLimit").value(8))
            .andExpect(jsonPath("$.data.sessionGoal").value(25))
            .andExpect(jsonPath("$.data.enableNotifications").value(false))
            .andExpect(jsonPath("$.data.useAdvancedAlgorithm").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAvailableReviewModes() throws Exception {
        // When
        var result = mockMvc.perform(get("/api/spaced-repetition/modes/available"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[*].mode").exists())
            .andExpect(jsonPath("$.data[*].name").exists())
            .andExpect(jsonPath("$.data[*].availableCards").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSuspendCard() throws Exception {
        // Given
        Long cardId = testCards.get(0).getId();

        // When
        var result = mockMvc.perform(post("/api/spaced-repetition/cards/" + cardId + "/suspend"));

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("Card suspended successfully"));

        // Verify card is suspended
        SpacedRepetitionCard suspendedCard = cardRepository.findById(cardId).orElseThrow();
        assertTrue(suspendedCard.getIsSuspended());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProgressMetrics() throws Exception {
        // When
        var streakResult = mockMvc.perform(get("/api/spaced-repetition/progress/streak"));
        streakResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());

        var retentionResult = mockMvc.perform(get("/api/spaced-repetition/progress/retention"));
        retentionResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());

        var performanceResult = mockMvc.perform(get("/api/spaced-repetition/progress/performance"));
        performanceResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "wronguser")
    void testUnauthorizedAccess() throws Exception {
        // When - try to access another user's session
        var result = mockMvc.perform(get("/api/spaced-repetition/sessions/active"));

        // Then - should fail because user doesn't exist
        result.andExpect(status().isInternalServerError());
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // When - try to access without authentication
        var result = mockMvc.perform(get("/api/spaced-repetition/sessions/active"));

        // Then - should be redirected to login
        result.andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testInvalidReviewSessionStart() throws Exception {
        // Given - invalid request with null mode
        StartReviewSessionRequest request = StartReviewSessionRequest.builder()
            .limit(5)
            .build();

        // When
        var result = mockMvc.perform(post("/api/spaced-repetition/sessions/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Then - should fail validation
        result.andExpect(status().isBadRequest());
    }

    private Word createWord(String text, String translation, WordType type) {
        Word word = new Word();
        word.setText(text);
        word.setTranslation(translation);
        word.setType(type);
        word.setDifficultyLevel(com.memorizewords.enums.DifficultyLevel.BEGINNER);
        return word;
    }
}