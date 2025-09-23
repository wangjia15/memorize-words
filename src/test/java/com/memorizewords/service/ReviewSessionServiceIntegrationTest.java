package com.memorizewords.service;

import com.memorizewords.dto.request.StartReviewSessionRequest;
import com.memorizewords.dto.request.SubmitReviewRequest;
import com.memorizewords.dto.request.UpdatePreferencesRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ReviewSessionService demonstrating Stream B functionality.
 */
@SpringBootTest
@Transactional
public class ReviewSessionServiceIntegrationTest {

    @Autowired
    private ReviewSessionService reviewSessionService;

    @Autowired
    private UserReviewPreferencesService preferencesService;

    @Autowired
    private ReviewStatisticsService statisticsService;

    @Autowired
    private SpacedRepetitionService spacedRepetitionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private SpacedRepetitionCardRepository cardRepository;

    @Autowired
    private ReviewSessionRepository sessionRepository;

    private User testUser;
    private List<Word> testWords;
    private List<SpacedRepetitionCard> testCards;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create test words
        testWords = List.of(
            createWord("hello", "hola", WordType.NOUN),
            createWord("goodbye", "adiós", WordType.NOUN),
            createWord("thank you", "gracias", WordType.PHRASE),
            createWord("please", "por favor", WordType.ADVERB),
            createWord("water", "agua", WordType.NOUN)
        );
        testWords = wordRepository.saveAll(testWords);

        // Create test cards
        testCards = testWords.stream()
            .map(word -> spacedRepetitionService.createCard(testUser, word))
            .toList();
        testCards = cardRepository.saveAll(testCards);
    }

    @Test
    void testStartReviewSessionWithDueCards() {
        // Given
        ReviewMode mode = ReviewMode.DUE_CARDS;
        int limit = 10;

        // When
        ReviewSession session = reviewSessionService.startReviewSession(testUser, mode, limit);

        // Then
        assertNotNull(session);
        assertEquals(testUser, session.getUser());
        assertEquals(mode, session.getMode());
        assertTrue(session.getTotalCards() > 0);
        assertNotNull(session.getStartTime());
        assertFalse(session.getIsCompleted());
        assertEquals(0, session.getCompletedCards());
        assertEquals(0, session.getCorrectAnswers());
    }

    @Test
    void testStartReviewSessionWithRequestDTO() {
        // Given
        StartReviewSessionRequest request = StartReviewSessionRequest.builder()
            .mode(ReviewMode.NEW_CARDS)
            .limit(5)
            .shuffleCards(true)
            .build();

        // When
        ReviewSession session = reviewSessionService.startReviewSession(testUser, request);

        // Then
        assertNotNull(session);
        assertEquals(ReviewMode.NEW_CARDS, session.getMode());
        assertEquals(5, session.getTotalCards());
        assertFalse(session.getIsCompleted());
    }

    @Test
    void testSubmitReviewUpdatesSessionAndCard() {
        // Given
        ReviewSession session = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 3);
        SpacedRepetitionCard card = testCards.get(0);
        ReviewOutcome outcome = ReviewOutcome.GOOD;
        int responseTime = 2500;

        // When
        SubmitReviewRequest request = SubmitReviewRequest.builder()
            .sessionId(session.getId())
            .cardId(card.getId())
            .outcome(outcome)
            .responseTime(responseTime)
            .build();

        ReviewSession updatedSession = reviewSessionService.submitReview(request, testUser);

        // Then
        assertEquals(1, updatedSession.getCompletedCards());
        assertEquals(1, updatedSession.getCorrectAnswers());
        assertEquals(responseTime, updatedSession.getAverageResponseTime());

        // Verify card was updated by spaced repetition algorithm
        SpacedRepetitionCard updatedCard = cardRepository.findById(card.getId()).orElseThrow();
        assertTrue(updatedCard.getTotalReviews() > 0);
        assertTrue(updatedCard.getLastReviewed() != null);
        assertEquals(outcome, updatedCard.getLastReviewOutcome());
    }

    @Test
    void testCompleteSession() {
        // Given
        ReviewSession session = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 2);

        // Submit reviews for all cards
        for (SpacedRepetitionCard card : testCards.subList(0, 2)) {
            SubmitReviewRequest request = SubmitReviewRequest.builder()
                .sessionId(session.getId())
                .cardId(card.getId())
                .outcome(ReviewOutcome.GOOD)
                .responseTime(2000)
                .build();
            reviewSessionService.submitReview(request, testUser);
        }

        // When
        ReviewSession completedSession = reviewSessionService.completeSession(session.getId(), testUser);

        // Then
        assertTrue(completedSession.getIsCompleted());
        assertNotNull(completedSession.getEndTime());
        assertEquals(2, completedSession.getCompletedCards());
        assertEquals(2, completedSession.getCorrectAnswers());
        assertNotNull(completedSession.getSessionAccuracy());
        assertNotNull(completedSession.getSessionDuration());
    }

    @Test
    void testUserReviewPreferencesManagement() {
        // Given
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
            .dailyReviewLimit(30)
            .dailyNewCardLimit(5)
            .sessionGoal(15)
            .enableNotifications(true)
            .useAdvancedAlgorithm(true)
            .build();

        // When
        UserReviewPreferencesDTO updated = preferencesService.updatePreferences(testUser, request);

        // Then
        assertNotNull(updated);
        assertEquals(30, updated.getDailyReviewLimit());
        assertEquals(5, updated.getDailyNewCardLimit());
        assertEquals(15, updated.getSessionGoal());
        assertTrue(updated.getEnableNotifications());
        assertTrue(updated.getUseAdvancedAlgorithm());
    }

    @Test
    void testReviewStatisticsCalculation() {
        // Given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        // Create some completed sessions
        ReviewSession session1 = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 2);
        reviewSessionService.completeSession(session1.getId(), testUser);

        ReviewSession session2 = reviewSessionService.startReviewSession(
            testUser, ReviewMode.NEW_CARDS, 2);
        reviewSessionService.completeSession(session2.getId(), testUser);

        // When
        ReviewStatisticsDTO statistics = statisticsService.calculateReviewStatistics(testUser, from, to);

        // Then
        assertNotNull(statistics);
        assertEquals(testUser.getId(), statistics.getUserId());
        assertEquals(from, statistics.getPeriodStart());
        assertEquals(to, statistics.getPeriodEnd());
        assertNotNull(statistics.getAverageAccuracy());
        assertNotNull(statistics.getLearningVelocity());
        assertNotNull(statistics.getRetentionRate());
        assertNotNull(statistics.getDailyMetrics());
        assertEquals(2, statistics.getTotalSessions());
    }

    @Test
    void testAvailableReviewModes() {
        // When
        List<ReviewMode> availableModes = reviewSessionService.getAvailableReviewModes(testUser);

        // Then
        assertNotNull(availableModes);
        assertTrue(availableModes.contains(ReviewMode.DUE_CARDS));
        assertTrue(availableModes.contains(ReviewMode.NEW_CARDS));
        assertTrue(availableModes.contains(ReviewMode.RANDOM_REVIEW));
        assertTrue(availableModes.contains(ReviewMode.ALL_CARDS));
    }

    @Test
    void testGetDueCards() {
        // When
        List<SpacedRepetitionCard> dueCards = reviewSessionService.getDueCards(testUser, 10);

        // Then
        assertNotNull(dueCards);
        assertTrue(dueCards.size() <= 10);
        assertTrue(dueCards.stream().allMatch(card -> card.getUser().equals(testUser)));
    }

    @Test
    void testGetActiveSession() {
        // Given
        ReviewSession session = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 3);

        // When
        ReviewSession activeSession = reviewSessionService.getActiveSession(testUser);

        // Then
        assertNotNull(activeSession);
        assertEquals(session.getId(), activeSession.getId());
        assertFalse(activeSession.getIsCompleted());
    }

    @Test
    void testSessionTimeoutExpiredSessions() {
        // Given
        ReviewSession session = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 3);

        // When
        reviewSessionService.timeoutExpiredSessions();

        // Then
        // Session should be completed after timeout
        ReviewSession timedOutSession = sessionRepository.findById(session.getId()).orElseThrow();
        assertTrue(timedOutSession.getIsCompleted());
        assertNotNull(timedOutSession.getEndTime());
    }

    @Test
    void testReviewModesWithDifferentCardSelections() {
        // Test DUE_CARDS mode
        ReviewSession dueSession = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 2);
        assertNotNull(dueSession);
        assertEquals(ReviewMode.DUE_CARDS, dueSession.getMode());

        // Test NEW_CARDS mode
        ReviewSession newSession = reviewSessionService.startReviewSession(
            testUser, ReviewMode.NEW_CARDS, 2);
        assertNotNull(newSession);
        assertEquals(ReviewMode.NEW_CARDS, newSession.getMode());

        // Test RANDOM_REVIEW mode
        ReviewSession randomSession = reviewSessionService.startReviewSession(
            testUser, ReviewMode.RANDOM_REVIEW, 2);
        assertNotNull(randomSession);
        assertEquals(ReviewMode.RANDOM_REVIEW, randomSession.getMode());

        // Test ALL_CARDS mode
        ReviewSession allSession = reviewSessionService.startReviewSession(
            testUser, ReviewMode.ALL_CARDS, 2);
        assertNotNull(allSession);
        assertEquals(ReviewMode.ALL_CARDS, allSession.getMode());
    }

    @Test
    void testStatisticsServiceInsights() {
        // Given - create some review activity
        ReviewSession session = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 2);
        reviewSessionService.completeSession(session.getId(), testUser);

        // When
        var insights = statisticsService.generateReviewInsights(testUser);

        // Then
        assertNotNull(insights);
        assertNotNull(insights.getLearningVelocity());
        assertNotNull(insights.getRetentionTrend());
        assertNotNull(insights.getOptimalStudyTimes());
        assertNotNull(insights.getDifficultyDistribution());
        assertNotNull(insights.getRecommendations());
        assertNotNull(insights.getPerformanceTrend());
        assertNotNull(insights.getOverallPerformance());
    }

    @Test
    void testUserPreferencesEffectiveLimits() {
        // Given
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
            .dailyReviewLimit(25)
            .dailyNewCardLimit(3)
            .build();
        preferencesService.updatePreferences(testUser, request);

        // When
        int effectiveLimit = preferencesService.getEffectiveDailyLimit(testUser);
        int newCardLimit = preferencesService.getEffectiveNewCardLimit(testUser);

        // Then
        assertEquals(25, effectiveLimit);
        assertEquals(3, newCardLimit);
    }

    @Test
    void testConcurrentSessionPrevention() {
        // Given - start first session
        ReviewSession firstSession = reviewSessionService.startReviewSession(
            testUser, ReviewMode.DUE_CARDS, 3);

        // When & Then - try to start second session
        assertThrows(IllegalStateException.class, () -> {
            reviewSessionService.startReviewSession(testUser, ReviewMode.NEW_CARDS, 3);
        });

        // Clean up - complete first session
        reviewSessionService.completeSession(firstSession.getId(), testUser);
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