package com.memorizewords.service;

import com.memorizewords.dto.request.StartReviewSessionRequest;
import com.memorizewords.dto.request.SubmitReviewRequest;
import com.memorizewords.entity.ReviewSession;
import com.memorizewords.entity.ReviewSessionCard;
import com.memorizewords.entity.SpacedRepetitionCard;
import com.memorizewords.entity.User;
import com.memorizewords.entity.UserReviewPreferences;
import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.ReviewOutcome;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.ReviewSessionRepository;
import com.memorizewords.repository.SpacedRepetitionCardRepository;
import com.memorizewords.repository.UserRepository;
import com.memorizewords.repository.UserReviewPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing review sessions with spaced repetition.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewSessionService {

    private final SpacedRepetitionCardRepository cardRepository;
    private final ReviewSessionRepository sessionRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final ReviewStatisticsService statisticsService;
    private final UserReviewPreferencesService preferencesService;
    private final UserReviewPreferencesRepository userReviewPreferencesRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_SESSION_LIMIT = 20;
    private static final int MAX_SESSION_LIMIT = 100;
    private static final int SESSION_TIMEOUT_HOURS = 24;

    public ReviewSession startReviewSession(User user, ReviewMode mode, int limit) {
        log.info("Starting review session for user {} with mode {} and limit {}", user.getId(), mode, limit);

        // Validate user can start a session
        validateUserCanStartSession(user);

        // Apply user preferences and limits
        int actualLimit = Math.min(limit, getSessionLimit(user));
        actualLimit = Math.min(actualLimit, MAX_SESSION_LIMIT);

        // Get cards for review based on mode
        List<SpacedRepetitionCard> cards = selectCardsForReview(user, mode, actualLimit);

        if (cards.isEmpty()) {
            throw new IllegalStateException("No cards available for review in mode: " + mode);
        }

        // Create and initialize session
        ReviewSession session = createReviewSession(user, mode, cards);

        // Log session start for analytics
        logSessionStart(session);

        ReviewSession savedSession = sessionRepository.save(session);
        log.info("Successfully created review session {} with {} cards", savedSession.getId(), cards.size());

        return savedSession;
    }

    public ReviewSession startReviewSession(User user, StartReviewSessionRequest request) {
        return startReviewSession(user, request.getMode(), request.getLimit());
    }

    public ReviewSession submitReview(ReviewSession session, SpacedRepetitionCard card,
                                     ReviewOutcome outcome, int responseTime) {
        log.debug("Submitting review for session {} card {} with outcome {}", session.getId(), card.getId(), outcome);

        // Validate session and card
        validateSessionAndCard(session, card);

        // Update spaced repetition algorithm
        SpacedRepetitionCard updatedCard = spacedRepetitionService.updateCardWithOutcome(card, outcome, responseTime);

        // Record the review in session
        ReviewSessionCard sessionCard = recordReviewInSession(session, updatedCard, outcome, responseTime);

        // Update session statistics
        updateSessionStatistics(session, sessionCard);

        // Check if session is complete
        checkSessionCompletion(session);

        // Log review for analytics
        logReviewOutcome(session, updatedCard, outcome, responseTime);

        ReviewSession savedSession = sessionRepository.save(session);
        log.debug("Successfully submitted review for session {}", session.getId());

        return savedSession;
    }

    public ReviewSession submitReview(SubmitReviewRequest request, User user) {
        ReviewSession session = getSession(request.getSessionId());
        SpacedRepetitionCard card = spacedRepetitionService.getCard(request.getCardId());

        return submitReview(session, card, request.getOutcome(), request.getResponseTime());
    }

    public ReviewSession completeSession(ReviewSession session) {
        log.info("Completing review session {}", session.getId());

        // Validate session can be completed
        validateSessionCompletion(session);

        // Update session completion data
        session.setEndTime(LocalDateTime.now());
        session.setIsCompleted(true);

        // Calculate final statistics
        calculateFinalSessionStatistics(session);

        // Update user achievements
        updateUserAchievements(session.getUser(), session);

        // Log session completion for analytics
        logSessionCompletion(session);

        ReviewSession savedSession = sessionRepository.save(session);
        log.info("Successfully completed session {}", savedSession.getId());

        return savedSession;
    }

    public ReviewSession completeSession(Long sessionId, User user) {
        ReviewSession session = getSession(sessionId);
        return completeSession(session);
    }

    public ReviewSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("ReviewSession", "id", sessionId));
    }

    public ReviewSession getActiveSession(User user) {
        List<ReviewSession> activeSessions = sessionRepository.findActiveSessionsByUser(user);
        if (activeSessions.isEmpty()) {
            return null;
        }

        // Return the most recent active session
        return activeSessions.get(0);
    }

    public List<ReviewSession> getUserSessions(User user) {
        return sessionRepository.findByUserOrderByStartTimeDesc(user);
    }

    public Page<ReviewSession> getUserSessions(User user, int page, int size) {
        return sessionRepository.findByUserOrderByStartTimeDesc(user, PageRequest.of(page, size));
    }

    public List<ReviewSession> getCompletedSessions(User user) {
        return sessionRepository.findCompletedSessionsByUser(user);
    }

    public List<ReviewSession> getCompletedSessions(User user, int page, int size) {
        return sessionRepository.findCompletedSessionsByUser(user, PageRequest.of(page, size)).getContent();
    }

    public List<SpacedRepetitionCard> getDueCards(User user, int limit) {
        return spacedRepetitionService.getDueCards(user, limit);
    }

    public List<SpacedRepetitionCard> getNewCards(User user, int limit) {
        return spacedRepetitionService.getNewCards(user, limit);
    }

    public List<SpacedRepetitionCard> getDifficultCards(User user, int limit) {
        return spacedRepetitionService.getDifficultCards(user, limit);
    }

    public List<SpacedRepetitionCard> getRandomCards(User user, int limit) {
        return spacedRepetitionService.getRandomCards(user, limit);
    }

    public long getTotalDueCards(User user) {
        return spacedRepetitionService.countDueCards(user);
    }

    public long getTotalNewCards(User user) {
        return spacedRepetitionService.countNewCards(user);
    }

    public long getTotalActiveCards(User user) {
        return spacedRepetitionService.countActiveCards(user);
    }

    public int getRecommendedDailyLimit(User user) {
        UserReviewPreferences preferences = preferencesService.getPreferences(user);
        return preferences != null ? preferences.getDailyReviewLimit() : DEFAULT_SESSION_LIMIT;
    }

    public List<ReviewMode> getAvailableReviewModes(User user) {
        List<ReviewMode> availableModes = new ArrayList<>();

        // Check if user has due cards
        if (getTotalDueCards(user) > 0) {
            availableModes.add(ReviewMode.DUE_CARDS);
        }

        // Check if user has new cards
        if (getTotalNewCards(user) > 0) {
            availableModes.add(ReviewMode.NEW_CARDS);
        }

        // Check if user has difficult cards
        if (!spacedRepetitionService.getDifficultCards(user, 1).isEmpty()) {
            availableModes.add(ReviewMode.DIFFICULT_CARDS);
        }

        // Always available modes
        availableModes.add(ReviewMode.RANDOM_REVIEW);
        availableModes.add(ReviewMode.ALL_CARDS);

        return availableModes;
    }

    public void timeoutExpiredSessions() {
        LocalDateTime timeoutDate = LocalDateTime.now().minusHours(SESSION_TIMEOUT_HOURS);
        List<User> users = userRepository.findAll(); // You'll need to inject UserRepository

        for (User user : users) {
            List<ReviewSession> expiredSessions = sessionRepository.findExpiredSessions(user, timeoutDate);
            for (ReviewSession session : expiredSessions) {
                completeSession(session);
            }
        }
    }

    private List<SpacedRepetitionCard> selectCardsForReview(User user, ReviewMode mode, int limit) {
        UserReviewPreferences preferences = preferencesService.getPreferences(user);

        switch (mode) {
            case DUE_CARDS:
                return selectDueCards(user, limit, preferences);
            case DIFFICULT_CARDS:
                return selectDifficultCards(user, limit, preferences);
            case RANDOM_REVIEW:
                return selectRandomCards(user, limit, preferences);
            case NEW_CARDS:
                return selectNewCards(user, limit, preferences);
            case ALL_CARDS:
                return selectAllCards(user, limit, preferences);
            default:
                throw new IllegalArgumentException("Unsupported review mode: " + mode);
        }
    }

    private List<SpacedRepetitionCard> selectDueCards(User user, int limit, UserReviewPreferences preferences) {
        LocalDateTime now = LocalDateTime.now();

        // Get due cards, prioritized by due date
        List<SpacedRepetitionCard> dueCards = cardRepository
            .findDueCardsForUser(user, now, PageRequest.of(0, limit * 2));

        // Apply user preferences for daily limits
        int dailyLimit = preferences != null ? preferences.getDailyReviewLimit() : DEFAULT_SESSION_LIMIT;
        if (dailyLimit > 0) {
            dueCards = dueCards.stream()
                .limit(dailyLimit)
                .toList();
        }

        // Apply card type filters if specified
        if (preferences != null && !preferences.getIncludedCardTypes().isEmpty()) {
            dueCards = dueCards.stream()
                .filter(card -> preferences.isCardTypeIncluded(card.getWord().getType()))
                .toList();
        }

        return dueCards.stream()
            .limit(limit)
            .toList();
    }

    private List<SpacedRepetitionCard> selectDifficultCards(User user, int limit, UserReviewPreferences preferences) {
        // Find cards with low performance or high difficulty
        List<SpacedRepetitionCard> difficultCards = cardRepository
            .findDifficultCardsForUser(user, PageRequest.of(0, limit * 2))
            .getContent();

        // Sort by priority (difficulty rating, then performance index)
        difficultCards.sort((a, b) -> {
            int difficultyCompare = b.getDifficultyRating().compareTo(a.getDifficultyRating());
            if (difficultyCompare != 0) return difficultyCompare;

            return Double.compare(a.getPerformanceIndex().doubleValue(), b.getPerformanceIndex().doubleValue());
        });

        return difficultCards.stream()
            .limit(limit)
            .toList();
    }

    private List<SpacedRepetitionCard> selectRandomCards(User user, int limit, UserReviewPreferences preferences) {
        return spacedRepetitionService.getRandomCards(user, limit);
    }

    private List<SpacedRepetitionCard> selectNewCards(User user, int limit, UserReviewPreferences preferences) {
        // Apply daily new card limit
        int newCardLimit = preferences != null ? preferences.getDailyNewCardLimit() : 10;
        int actualLimit = Math.min(limit, newCardLimit);

        return spacedRepetitionService.getNewCards(user, actualLimit);
    }

    private List<SpacedRepetitionCard> selectAllCards(User user, int limit, UserReviewPreferences preferences) {
        // Get a mix of cards from different categories
        List<SpacedRepetitionCard> allCards = new ArrayList<>();

        // Add some due cards
        allCards.addAll(getDueCards(user, limit / 3));

        // Add some new cards
        allCards.addAll(getNewCards(user, limit / 3));

        // Add some random cards
        allCards.addAll(getRandomCards(user, limit / 3));

        return allCards.stream()
            .limit(limit)
            .toList();
    }

    private List<SpacedRepetitionCard> selectTargetedCards(User user, int limit, UserReviewPreferences preferences) {
        // For now, fall back to due cards with user preferences applied
        List<SpacedRepetitionCard> cards = selectDueCards(user, limit, preferences);

        // If no due cards, get new cards
        if (cards.isEmpty()) {
            cards = selectNewCards(user, limit, preferences);
        }

        // If still no cards, get random cards
        if (cards.isEmpty()) {
            cards = selectRandomCards(user, limit, preferences);
        }

        return cards.stream()
            .limit(limit)
            .toList();
    }

    private ReviewSession createReviewSession(User user, ReviewMode mode, List<SpacedRepetitionCard> cards) {
        ReviewSession session = new ReviewSession();
        session.setUser(user);
        session.setMode(mode);
        session.setStartTime(LocalDateTime.now());
        session.setTotalCards(cards.size());
        session.setCompletedCards(0);
        session.setCorrectAnswers(0);
        session.setAverageResponseTime(0);
        session.setIsCompleted(false);

        // Create session cards for tracking
        List<ReviewSessionCard> sessionCards = cards.stream()
            .map(card -> createSessionCard(session, card))
            .toList();

        session.setCards(sessionCards);

        return session;
    }

    private ReviewSessionCard createSessionCard(ReviewSession session, SpacedRepetitionCard card) {
        ReviewSessionCard sessionCard = new ReviewSessionCard();
        sessionCard.setSession(session);
        sessionCard.setCard(card);
        sessionCard.setIntervalBeforeReview(card.getIntervalDays());
        sessionCard.setEaseFactorBeforeReview(card.getEaseFactor());
        sessionCard.setReviewNumber(card.getTotalReviews() + 1);
        sessionCard.setConsecutiveCorrectBefore(card.getConsecutiveCorrect());

        return sessionCard;
    }

    private ReviewSessionCard recordReviewInSession(ReviewSession session, SpacedRepetitionCard card,
                                                  ReviewOutcome outcome, int responseTime) {
        ReviewSessionCard sessionCard = session.getCards().stream()
            .filter(sc -> sc.getCard().getId().equals(card.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("ReviewSessionCard", "cardId", card.getId()));

        sessionCard.setReviewOutcome(outcome, responseTime);
        sessionCard.setIntervalAfterReview(card.getIntervalDays());
        sessionCard.setEaseFactorAfterReview(card.getEaseFactor());

        return sessionCard;
    }

    private void updateSessionStatistics(ReviewSession session, ReviewSessionCard sessionCard) {
        session.updateStatistics(sessionCard);
    }

    private void checkSessionCompletion(ReviewSession session) {
        if (session.getCompletedCards() >= session.getTotalCards()) {
            completeSession(session);
        }
    }

    private void calculateFinalSessionStatistics(ReviewSession session) {
        // Calculate session accuracy
        double accuracy = session.getCompletedCards() > 0 ?
            (double) session.getCorrectAnswers() / session.getCompletedCards() * 100 : 0.0;

        // Calculate session duration
        if (session.getStartTime() != null && session.getEndTime() != null) {
            long duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
            session.setSessionDuration(duration);
        }
    }

    private void validateUserCanStartSession(User user) {
        ReviewSession activeSession = getActiveSession(user);
        if (activeSession != null && !activeSession.getIsCompleted()) {
            throw new IllegalStateException("User already has an active review session");
        }
    }

    private void validateSessionAndCard(ReviewSession session, SpacedRepetitionCard card) {
        if (session.getIsCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }

        if (!session.getUser().getId().equals(card.getUser().getId())) {
            throw new IllegalStateException("Card does not belong to session user");
        }

        if (session.getCompletedCards() >= session.getTotalCards()) {
            throw new IllegalStateException("All cards in session have been reviewed");
        }
    }

    private void validateSessionCompletion(ReviewSession session) {
        if (session.getIsCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }
    }

    private int getSessionLimit(User user) {
        UserReviewPreferences preferences = preferencesService.getPreferences(user);
        return preferences != null ? preferences.getSessionGoal() : DEFAULT_SESSION_LIMIT;
    }

    private void updateUserAchievements(User user, ReviewSession session) {
        // This will be implemented later when achievements are added
        log.debug("Updating achievements for user {} based on session {}", user.getId(), session.getId());
    }

    private void logSessionStart(ReviewSession session) {
        log.info("Review session started: user={}, mode={}, cards={}",
            session.getUser().getId(), session.getMode(), session.getTotalCards());
    }

    private void logReviewOutcome(ReviewSession session, SpacedRepetitionCard card,
                                ReviewOutcome outcome, int responseTime) {
        log.debug("Review outcome: session={}, card={}, outcome={}, responseTime={}",
            session.getId(), card.getId(), outcome, responseTime);
    }

    private void logSessionCompletion(ReviewSession session) {
        log.info("Review session completed: user={}, mode={}, cards={}, accuracy={}",
            session.getUser().getId(), session.getMode(), session.getTotalCards(),
            session.getAccuracyPercentage());
    }
}