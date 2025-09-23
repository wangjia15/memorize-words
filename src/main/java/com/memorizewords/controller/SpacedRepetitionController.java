package com.memorizewords.controller;

import com.memorizewords.dto.request.StartReviewSessionRequest;
import com.memorizewords.dto.request.SubmitReviewRequest;
import com.memorizewords.dto.request.UpdatePreferencesRequest;
import com.memorizewords.dto.response.*;
import com.memorizewords.entity.ReviewSession;
import com.memorizewords.entity.User;
import com.memorizewords.enums.ReviewMode;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for spaced repetition and review session management.
 */
@RestController
@RequestMapping("/api/spaced-repetition")
@RequiredArgsConstructor
@Slf4j
public class SpacedRepetitionController {

    private final ReviewSessionService sessionService;
    private final ReviewStatisticsService statisticsService;
    private final UserReviewPreferencesService preferencesService;
    private final SpacedRepetitionService spacedRepetitionService;

    // Review Session Management

    @PostMapping("/sessions/start")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> startReviewSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody StartReviewSessionRequest request) {

        log.info("Starting review session for user {} with mode {}", user.getId(), request.getMode());

        ReviewSession session = sessionService.startReviewSession(user, request);
        ReviewSessionDTO sessionDTO = convertToReviewSessionDTO(session);

        return ResponseEntity.ok(ApiResponse.success(sessionDTO));
    }

    @PostMapping("/sessions/{sessionId}/submit")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> submitReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitReviewRequest request) {

        log.info("Submitting review for session {} card {}", sessionId, request.getCardId());

        // Validate session belongs to user
        ReviewSession session = sessionService.getSession(sessionId);
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("ReviewSession", "id", sessionId);
        }

        ReviewSession updatedSession = sessionService.submitReview(request, user);
        ReviewSessionDTO sessionDTO = convertToReviewSessionDTO(updatedSession);

        return ResponseEntity.ok(ApiResponse.success(sessionDTO));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> completeSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {

        log.info("Completing review session {}", sessionId);

        ReviewSession session = sessionService.getSession(sessionId);
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("ReviewSession", "id", sessionId);
        }

        ReviewSession completedSession = sessionService.completeSession(sessionId, user);
        ReviewSessionDTO sessionDTO = convertToReviewSessionDTO(completedSession);

        return ResponseEntity.ok(ApiResponse.success(sessionDTO));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> getSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {

        ReviewSession session = sessionService.getSession(sessionId);
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("ReviewSession", "id", sessionId);
        }

        ReviewSessionDTO sessionDTO = convertToReviewSessionDTO(session);
        return ResponseEntity.ok(ApiResponse.success(sessionDTO));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<ReviewSessionDTO>> getActiveSession(
            @AuthenticationPrincipal User user) {

        ReviewSession session = sessionService.getActiveSession(user);

        if (session == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }

        ReviewSessionDTO sessionDTO = convertToReviewSessionDTO(session);
        return ResponseEntity.ok(ApiResponse.success(sessionDTO));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ReviewSessionDTO>>> getUserSessions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ReviewSession> sessions = sessionService.getUserSessions(user, page, size);
        List<ReviewSessionDTO> sessionDTOs = sessions.stream()
            .map(this::convertToReviewSessionDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(sessionDTOs));
    }

    @GetMapping("/sessions/completed")
    public ResponseEntity<ApiResponse<List<ReviewSessionDTO>>> getCompletedSessions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ReviewSession> sessions = sessionService.getCompletedSessions(user, page, size);
        List<ReviewSessionDTO> sessionDTOs = sessions.stream()
            .map(this::convertToReviewSessionDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(sessionDTOs));
    }

    // Card Management

    @GetMapping("/cards/due")
    public ResponseEntity<ApiResponse<DueCardsResponse>> getDueCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {

        List<SpacedRepetitionCard> dueCards = sessionService.getDueCards(user, limit);
        List<SpacedRepetitionCardDTO> dueCardDTOs = dueCards.stream()
            .map(this::convertToSpacedRepetitionCardDTO)
            .toList();

        DueCardsResponse response = DueCardsResponse.builder()
            .dueCards(dueCardDTOs)
            .totalDue(sessionService.getTotalDueCards(user))
            .totalNew(sessionService.getTotalNewCards(user))
            .totalDifficult(spacedRepetitionService.getDifficultCards(user, 1).size())
            .totalActive(sessionService.getTotalActiveCards(user))
            .recommendedLimit(sessionService.getRecommendedDailyLimit(user))
            .dailyLimit(preferencesService.getEffectiveDailyLimit(user))
            .exceedsDailyLimit(dueCards.size() > preferencesService.getEffectiveDailyLimit(user))
            .newCardsToday((int) sessionService.getTotalNewCards(user))
            .reviewsToday((int) sessionService.getTotalDueCards(user))
            .availableReviewModes(sessionService.getAvailableReviewModes(user).stream()
                .map(Enum::name)
                .toList())
            .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cards/new")
    public ResponseEntity<ApiResponse<List<SpacedRepetitionCardDTO>>> getNewCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {

        List<SpacedRepetitionCard> newCards = sessionService.getNewCards(user, limit);
        List<SpacedRepetitionCardDTO> cardDTOs = newCards.stream()
            .map(this::convertToSpacedRepetitionCardDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(cardDTOs));
    }

    @GetMapping("/cards/difficult")
    public ResponseEntity<ApiResponse<List<SpacedRepetitionCardDTO>>> getDifficultCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {

        List<SpacedRepetitionCard> difficultCards = sessionService.getDifficultCards(user, limit);
        List<SpacedRepetitionCardDTO> cardDTOs = difficultCards.stream()
            .map(this::convertToSpacedRepetitionCardDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(cardDTOs));
    }

    @GetMapping("/cards/random")
    public ResponseEntity<ApiResponse<List<SpacedRepetitionCardDTO>>> getRandomCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit) {

        List<SpacedRepetitionCard> randomCards = sessionService.getRandomCards(user, limit);
        List<SpacedRepetitionCardDTO> cardDTOs = randomCards.stream()
            .map(this::convertToSpacedRepetitionCardDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(cardDTOs));
    }

    @GetMapping("/cards/active")
    public ResponseEntity<ApiResponse<List<SpacedRepetitionCardDTO>>> getActiveCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "50") int limit) {

        List<SpacedRepetitionCard> activeCards = spacedRepetitionService.getActiveCards(user);
        List<SpacedRepetitionCardDTO> cardDTOs = activeCards.stream()
            .limit(limit)
            .map(this::convertToSpacedRepetitionCardDTO)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(cardDTOs));
    }

    // Statistics and Analytics

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getReviewStatistics(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ReviewStatistics statistics = statisticsService.calculateReviewStatistics(user, from, to);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/statistics/overview")
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getCurrentMonthStatistics(
            @AuthenticationPrincipal User user) {

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        ReviewStatistics statistics = statisticsService.calculateReviewStatistics(user, startOfMonth, now);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/analytics/insights")
    public ResponseEntity<ApiResponse<ReviewInsightsDTO>> getReviewInsights(
            @AuthenticationPrincipal User user) {

        ReviewInsightsDTO insights = statisticsService.generateReviewInsights(user);
        return ResponseEntity.ok(ApiResponse.success(insights));
    }

    @GetMapping("/analytics/performance")
    public ResponseEntity<ApiResponse<PerformanceInsightDTO>> getPerformanceInsights(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ReviewStatisticsDTO statistics = statisticsService.calculateReviewStatistics(user, from, to);
        PerformanceInsightDTO insight = statistics.getPerformanceInsight();
        return ResponseEntity.ok(ApiResponse.success(insight));
    }

    // User Preferences

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserReviewPreferencesDTO>> getPreferences(
            @AuthenticationPrincipal User user) {

        UserReviewPreferences preferences = preferencesService.getPreferences(user);
        UserReviewPreferencesDTO preferencesDTO = convertToUserReviewPreferencesDTO(preferences);
        return ResponseEntity.ok(ApiResponse.success(preferencesDTO));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserReviewPreferencesDTO>> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePreferencesRequest request) {

        UserReviewPreferences updated = preferencesService.updatePreferences(user, request);
        UserReviewPreferencesDTO preferencesDTO = convertToUserReviewPreferencesDTO(updated);
        return ResponseEntity.ok(ApiResponse.success(preferencesDTO));
    }

    @PostMapping("/preferences/reset")
    public ResponseEntity<ApiResponse<UserReviewPreferencesDTO>> resetPreferences(
            @AuthenticationPrincipal User user) {

        preferencesService.resetToDefaults(user);
        UserReviewPreferences preferences = preferencesService.getPreferences(user);
        UserReviewPreferencesDTO preferencesDTO = convertToUserReviewPreferencesDTO(preferences);
        return ResponseEntity.ok(ApiResponse.success(preferencesDTO));
    }

    // Review Modes

    @GetMapping("/modes/available")
    public ResponseEntity<ApiResponse<List<ReviewModeInfoDTO>>> getAvailableReviewModes(
            @AuthenticationPrincipal User user) {

        List<ReviewModeInfoDTO> modes = statisticsService.getAvailableReviewModes(user);
        return ResponseEntity.ok(ApiResponse.success(modes));
    }

    @GetMapping("/modes/recommended")
    public ResponseEntity<ApiResponse<ReviewModeInfoDTO>> getRecommendedReviewMode(
            @AuthenticationPrincipal User user) {

        List<ReviewModeInfoDTO> availableModes = statisticsService.getAvailableReviewModes(user);
        ReviewModeInfoDTO recommended = availableModes.stream()
            .filter(ReviewModeInfoDTO::getIsRecommended)
            .findFirst()
            .orElse(availableModes.get(0));

        return ResponseEntity.ok(ApiResponse.success(recommended));
    }

    // Card Operations

    @PostMapping("/cards/{cardId}/suspend")
    public ResponseEntity<ApiResponse<String>> suspendCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        spacedRepetitionService.suspendCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card suspended successfully"));
    }

    @PostMapping("/cards/{cardId}/unsuspend")
    public ResponseEntity<ApiResponse<String>> unsuspendCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        spacedRepetitionService.unsuspendCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card unsuspended successfully"));
    }

    @PostMapping("/cards/{cardId}/reset")
    public ResponseEntity<ApiResponse<String>> resetCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        spacedRepetitionService.resetCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card reset successfully"));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<String>> deleteCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        spacedRepetitionService.deleteCard(cardId);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully"));
    }

    // Analytics and Progress Tracking

    @GetMapping("/progress/streak")
    public ResponseEntity<ApiResponse<Integer>> getCurrentStreak(
            @AuthenticationPrincipal User user) {

        LocalDate now = LocalDate.now();
        Integer streak = statisticsService.calculateReviewStatistics(user, now.minusDays(30), now).getStreakDays();
        return ResponseEntity.ok(ApiResponse.success(streak));
    }

    @GetMapping("/progress/retention")
    public ResponseEntity<ApiResponse<Double>> getRetentionRate(
            @AuthenticationPrincipal User user) {

        Double retentionRate = spacedRepetitionService.getUserAverageRetentionRate(user).doubleValue();
        return ResponseEntity.ok(ApiResponse.success(retentionRate));
    }

    @GetMapping("/progress/performance")
    public ResponseEntity<ApiResponse<Double>> getPerformanceIndex(
            @AuthenticationPrincipal User user) {

        Double performanceIndex = spacedRepetitionService.getUserAveragePerformanceIndex(user).doubleValue();
        return ResponseEntity.ok(ApiResponse.success(performanceIndex));
    }

    @GetMapping("/progress/study-time")
    public ResponseEntity<ApiResponse<Long>> getTotalStudyTime(
            @AuthenticationPrincipal User user) {

        Long studyTime = spacedRepetitionService.getUserTotalStudyTime(user);
        return ResponseEntity.ok(ApiResponse.success(studyTime));
    }

    // Utility Methods

    private ReviewSessionDTO convertToReviewSessionDTO(ReviewSession session) {
        if (session == null) return null;

        return ReviewSessionDTO.builder()
            .id(session.getId())
            .userId(session.getUser().getId())
            .mode(session.getMode())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .totalCards(session.getTotalCards())
            .completedCards(session.getCompletedCards())
            .correctAnswers(session.getCorrectAnswers())
            .averageResponseTime(session.getAverageResponseTime())
            .isCompleted(session.getIsCompleted())
            .sessionAccuracy(session.getSessionAccuracy())
            .sessionDuration(session.getSessionDuration() != null ?
                Duration.ofSeconds(session.getSessionDuration()) : null)
            .cardsPerMinute(session.getCardsPerMinute())
            .totalSessionScore(session.getTotalSessionScore())
            .efficiencyScore(session.getEfficiencyScore())
            .newCardsLearned(session.getNewCardsLearned())
            .difficultCardsMastered(session.getDifficultCardsMastered())
            .learningVelocity(session.getLearningVelocity())
            .cards(session.getCards() != null ? session.getCards().stream()
                .map(this::convertToReviewSessionCardDTO)
                .toList() : null)
            .currentCardIndex(session.getCurrentCardIndex())
            .remainingCards(session.getRemainingCards())
            .progressPercentage(session.getProgressPercentage())
            .build();
    }

    private ReviewSessionCardDTO convertToReviewSessionCardDTO(ReviewSessionCard sessionCard) {
        if (sessionCard == null) return null;

        return ReviewSessionCardDTO.builder()
            .id(sessionCard.getId())
            .sessionId(sessionCard.getSession() != null ? sessionCard.getSession().getId() : null)
            .card(convertToSpacedRepetitionCardDTO(sessionCard.getCard()))
            .reviewOutcome(sessionCard.getReviewOutcome())
            .responseTime(sessionCard.getResponseTime())
            .reviewTime(sessionCard.getReviewTime())
            .intervalBeforeReview(sessionCard.getIntervalBeforeReview())
            .intervalAfterReview(sessionCard.getIntervalAfterReview())
            .easeFactorBeforeReview(sessionCard.getEaseFactorBeforeReview())
            .easeFactorAfterReview(sessionCard.getEaseFactorAfterReview())
            .reviewNumber(sessionCard.getReviewNumber())
            .consecutiveCorrectBefore(sessionCard.getConsecutiveCorrectBefore())
            .wasCorrect(sessionCard.getWasCorrect())
            .score(sessionCard.getScore())
            .userAnswer(sessionCard.getUserAnswer())
            .hintUsed(sessionCard.getHintUsed())
            .confidenceLevel(sessionCard.getConfidenceLevel())
            .markedAsDifficult(sessionCard.getMarkedAsDifficult())
            .notes(sessionCard.getNotes())
            .isNewCard(sessionCard.getIsNewCard())
            .wasDifficult(sessionCard.getWasDifficult())
            .isCompleted(sessionCard.getIsCompleted())
            .build();
    }

    private SpacedRepetitionCardDTO convertToSpacedRepetitionCardDTO(SpacedRepetitionCard card) {
        if (card == null) return null;

        return SpacedRepetitionCardDTO.builder()
            .id(card.getId())
            .userId(card.getUser().getId())
            .word(convertToWordDTO(card.getWord()))
            .intervalDays(card.getIntervalDays())
            .easeFactor(card.getEaseFactor())
            .dueDate(card.getDueDate())
            .nextReview(card.getNextReview())
            .lastReviewed(card.getLastReviewed())
            .totalReviews(card.getTotalReviews())
            .correctReviews(card.getCorrectReviews())
            .consecutiveCorrect(card.getConsecutiveCorrect())
            .consecutiveIncorrect(card.getConsecutiveIncorrect())
            .difficultyLevel(card.getDifficultyLevel())
            .performanceIndex(card.getPerformanceIndex())
            .averageResponseTime(card.getAverageResponseTime())
            .isActive(card.getIsActive())
            .isSuspended(card.getIsSuspended())
            .stabilityFactor(card.getStabilityFactor())
            .totalStudyTime(card.getTotalStudyTime())
            .lastReviewOutcome(card.getLastReviewOutcome())
            .reviewCountAgain(card.getReviewCountAgain())
            .reviewCountHard(card.getReviewCountHard())
            .reviewCountGood(card.getReviewCountGood())
            .reviewCountEasy(card.getReviewCountEasy())
            .cardAgeDays(card.getCardAgeDays())
            .retentionRate(card.getRetentionRate())
            .isDue(card.getDueDate() != null && card.getDueDate().isBefore(LocalDateTime.now()))
            .isNew(card.getTotalReviews() == 0)
            .isDifficult(card.getDifficultyRating() != null && card.getDifficultyRating() > 0.5)
            .difficultyRating(card.getDifficultyRating())
            .reviewHistory(card.getReviewHistory())
            .build();
    }

    private WordDto convertToWordDTO(Word word) {
        if (word == null) return null;

        return WordDto.builder()
            .id(word.getId())
            .text(word.getText())
            .translation(word.getTranslation())
            .pronunciation(word.getPronunciation())
            .type(word.getType())
            .category(word.getCategory())
            .difficultyLevel(word.getDifficultyLevel())
            .exampleSentence(word.getExampleSentence())
            .exampleTranslation(word.getExampleTranslation())
            .build();
    }

    private UserReviewPreferencesDTO convertToUserReviewPreferencesDTO(UserReviewPreferences preferences) {
        if (preferences == null) return null;

        return UserReviewPreferencesDTO.builder()
            .id(preferences.getId())
            .userId(preferences.getUser().getId())
            .dailyReviewLimit(preferences.getDailyReviewLimit())
            .dailyNewCardLimit(preferences.getDailyNewCardLimit())
            .sessionGoal(preferences.getSessionGoal())
            .preferredReviewTime(preferences.getPreferredReviewTime())
            .enableNotifications(preferences.getEnableNotifications())
            .enableEmailReminders(preferences.getEnableEmailReminders())
            .useAdvancedAlgorithm(preferences.getUseAdvancedAlgorithm())
            .easeFactorBonus(preferences.getEaseFactorBonus())
            .intervalModifier(preferences.getIntervalModifier())
            .minimumIntervalDays(preferences.getMinimumIntervalDays())
            .maximumIntervalDays(preferences.getMaximumIntervalDays())
            .defaultReviewMode(preferences.getDefaultReviewMode())
            .autoAdvanceCards(preferences.getAutoAdvanceCards())
            .showAnswerDelay(preferences.getShowAnswerDelay())
            .enableHints(preferences.getEnableHints())
            .enablePronunciation(preferences.getEnablePronunciation())
            .enableProgressAnimation(preferences.getEnableProgressAnimation())
            .themePreference(preferences.getThemePreference())
            .languagePreference(preferences.getLanguagePreference())
            .timezone(preferences.getTimezone())
            .weekendReviewMode(preferences.getWeekendReviewMode())
            .vacationMode(preferences.getVacationMode())
            .vacationStartDate(preferences.getVacationStartDate())
            .vacationEndDate(preferences.getVacationEndDate())
            .preferredModes(preferences.getPreferredModes())
            .includedCardTypes(preferences.getIncludedCardTypes())
            .excludedCardTypes(preferences.getExcludedCardTypes())
            .notificationTimes(preferences.getNotificationTimes())
            .reviewGoals(preferences.getReviewGoals().stream()
                .map(goal -> ReviewGoalDTO.builder()
                    .period(goal.getPeriod())
                    .target(goal.getTarget())
                    .build())
                .toList())
            .adaptiveDifficulty(preferences.getAdaptiveDifficulty())
            .focusModeDuration(preferences.getFocusModeDuration())
            .breakDuration(preferences.getBreakDuration())
            .enableStreakProtection(preferences.getEnableStreakProtection())
            .streakProtectionTime(preferences.getStreakProtectionTime())
            .enableLearningInsights(preferences.getEnableLearningInsights())
            .weeklyGoal(preferences.getWeeklyGoal())
            .monthlyGoal(preferences.getMonthlyGoal())
            .enableAchievements(preferences.getEnableAchievements())
            .showDetailedStatistics(preferences.getShowDetailedStatistics())
            .exportFormatPreference(preferences.getExportFormatPreference())
            .backupFrequency(preferences.getBackupFrequency())
            .createdAt(preferences.getCreatedAt())
            .updatedAt(preferences.getUpdatedAt())
            .build();
    }
}