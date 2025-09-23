package com.memorizewords.service;

import com.memorizewords.dto.response.*;
import com.memorizewords.entity.ReviewSession;
import com.memorizewords.entity.SpacedRepetitionCard;
import com.memorizewords.entity.User;
import com.memorizewords.enums.ReviewMode;
import com.memorizewords.repository.ReviewSessionRepository;
import com.memorizewords.repository.SpacedRepetitionCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating review statistics and generating insights.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewStatisticsService {

    private final ReviewSessionRepository sessionRepository;
    private final SpacedRepetitionCardRepository cardRepository;

    public ReviewStatisticsDTO calculateReviewStatistics(User user, LocalDate from, LocalDate to) {
        log.info("Calculating review statistics for user {} from {} to {}", user.getId(), from, to);

        // Get all review activity in the period
        List<ReviewSession> sessions = sessionRepository
            .findByUserAndStartTimeBetween(
                user, from.atStartOfDay(), to.atTime(23, 59, 59));

        List<SpacedRepetitionCard> cards = cardRepository
            .findByUserAndLastReviewedBetween(
                user, from.atStartOfDay(), to.atTime(23, 59, 59));

        return ReviewStatisticsDTO.builder()
            .userId(user.getId())
            .periodStart(from)
            .periodEnd(to)
            .totalReviews(calculateTotalReviews(sessions))
            .correctReviews(calculateCorrectReviews(sessions))
            .averageAccuracy(calculateAverageAccuracy(sessions))
            .totalStudyTime(calculateTotalStudyTime(sessions))
            .streakDays(calculateStreakDays(user, to))
            .longestStreak(calculateLongestStreak(user))
            .learningVelocity(calculateLearningVelocity(cards, from, to))
            .retentionRate(calculateRetentionRate(cards))
            .dailyMetrics(calculateDailyMetrics(user, from, to))
            .achievements(calculateAchievements(sessions))
            .totalSessions((int) sessions.stream().filter(ReviewSession::getIsCompleted).count())
            .averageSessionScore(calculateAverageSessionScore(sessions))
            .bestSessionAccuracy(calculateBestSessionAccuracy(sessions))
            .newCardsLearned(calculateNewCardsLearned(sessions))
            .difficultCardsMastered(calculateDifficultCardsMastered(sessions))
            .cardsPerMinuteAverage(calculateCardsPerMinuteAverage(sessions))
            .efficiencyScore(calculateEfficiencyScore(sessions))
            .reviewTrend(calculateReviewTrend(user, from, to))
            .performanceInsight(generatePerformanceInsight(user, sessions, from, to))
            .build();
    }

    public ReviewInsightsDTO generateReviewInsights(User user) {
        log.info("Generating review insights for user {}", user.getId());

        // Get recent performance data
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        ReviewStatisticsDTO recentStats = calculateReviewStatistics(user, thirtyDaysAgo, LocalDate.now());

        // Analyze patterns and generate insights
        return ReviewInsightsDTO.builder()
            .learningVelocity(calculateLearningVelocityInsight(recentStats))
            .retentionTrend(calculateRetentionTrend(user))
            .optimalStudyTimes(findOptimalStudyTimes(user))
            .difficultyDistribution(analyzeDifficultyDistribution(user))
            .recommendations(generateRecommendations(user, recentStats))
            .performanceTrend(calculatePerformanceTrend(user))
            .bestPerformanceTime(getBestPerformanceTime(user))
            .worstPerformanceTime(getWorstPerformanceTime(user))
            .averageDailyReviews(calculateAverageDailyReviews(user))
            .averageAccuracy(recentStats.getAverageAccuracy())
            .currentStreak(recentStats.getStreakDays())
            .longestStreak(recentStats.getLongestStreak())
            .overallPerformance(calculateOverallPerformance(recentStats))
            .achievements(calculateRecentAchievements(user))
            .areasForImprovement(calculateAreasForImprovement(user))
            .nextMilestone(calculateNextMilestone(user))
            .daysToMilestone(calculateDaysToMilestone(user))
            .build();
    }

    public List<ReviewModeInfoDTO> getAvailableReviewModes(User user) {
        List<ReviewModeInfoDTO> modes = new ArrayList<>();

        // Get card counts for each mode
        long dueCardsCount = cardRepository.countDueCardsForUser(user, LocalDateTime.now());
        long newCardsCount = cardRepository.countNewCardsForUser(user);
        long difficultCardsCount = cardRepository.findDifficultCardsForUser(
            user, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        long totalCardsCount = cardRepository.countActiveCardsForUser(user);

        // Check each mode
        for (ReviewMode mode : ReviewMode.values()) {
            ReviewModeInfoDTO modeInfo = ReviewModeInfoDTO.builder()
                .mode(mode)
                .name(getModeDisplayName(mode))
                .description(getModeDescription(mode))
                .icon(getModeIcon(mode))
                .color(getModeColor(mode))
                .isNew(mode == ReviewMode.NEW_CARDS)
                .isPopular(isPopularMode(mode))
                .build();

            switch (mode) {
                case DUE_CARDS:
                    modeInfo.setAvailableCards((int) dueCardsCount);
                    modeInfo.setRecommended(dueCardsCount > 0);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(dueCardsCount));
                    break;
                case NEW_CARDS:
                    modeInfo.setAvailableCards((int) newCardsCount);
                    modeInfo.setRecommended(newCardsCount > 0 && newCardsCount < 20);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(newCardsCount));
                    break;
                case DIFFICULT_CARDS:
                    modeInfo.setAvailableCards((int) difficultCardsCount);
                    modeInfo.setRecommended(difficultCardsCount > 0);
                    modeInfo.setDifficultyLevel(0.8);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(difficultCardsCount));
                    break;
                case RANDOM_REVIEW:
                    modeInfo.setAvailableCards((int) Math.min(totalCardsCount, 50));
                    modeInfo.setRecommended(totalCardsCount > 20);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(Math.min(totalCardsCount, 50)));
                    break;
                case ALL_CARDS:
                    modeInfo.setAvailableCards((int) totalCardsCount);
                    modeInfo.setRecommended(false);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(totalCardsCount));
                    break;
                case TARGETED_REVIEW:
                    modeInfo.setAvailableCards((int) totalCardsCount);
                    modeInfo.setRecommended(false);
                    modeInfo.setEstimatedDurationMinutes(estimateDuration(totalCardsCount));
                    break;
            }

            modes.add(modeInfo);
        }

        return modes;
    }

    private Integer calculateTotalReviews(List<ReviewSession> sessions) {
        return sessions.stream()
            .mapToInt(ReviewSession::getCompletedCards)
            .sum();
    }

    private Integer calculateCorrectReviews(List<ReviewSession> sessions) {
        return sessions.stream()
            .mapToInt(ReviewSession::getCorrectAnswers)
            .sum();
    }

    private Double calculateAverageAccuracy(List<ReviewSession> sessions) {
        if (sessions.isEmpty()) return 0.0;

        int totalReviews = sessions.stream()
            .mapToInt(ReviewSession::getCompletedCards)
            .sum();

        int correctReviews = sessions.stream()
            .mapToInt(ReviewSession::getCorrectAnswers)
            .sum();

        return totalReviews > 0 ? (double) correctReviews / totalReviews * 100 : 0.0;
    }

    private Duration calculateTotalStudyTime(List<ReviewSession> sessions) {
        long totalSeconds = sessions.stream()
            .filter(session -> session.getSessionDuration() != null)
            .mapToLong(ReviewSession::getSessionDuration)
            .sum();

        return Duration.ofSeconds(totalSeconds);
    }

    private Integer calculateStreakDays(User user, LocalDate endDate) {
        LocalDate currentDate = endDate;
        int streak = 0;

        while (currentDate.isAfter(LocalDate.now().minusYears(1))) { // Check up to 1 year back
            List<ReviewSession> daySessions = sessionRepository
                .findSessionsByUserAndDate(user, currentDate.atStartOfDay());

            if (!daySessions.isEmpty() && daySessions.stream()
                .anyMatch(session -> session.getCompletedCards() > 0)) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    private Integer calculateLongestStreak(User user) {
        // This would require more complex logic to find the longest streak in history
        // For now, return current streak as a placeholder
        return calculateStreakDays(user, LocalDate.now());
    }

    private LearningVelocityDTO calculateLearningVelocity(List<SpacedRepetitionCard> cards, LocalDate from, LocalDate to) {
        if (cards.isEmpty()) {
            return LearningVelocityDTO.builder()
                .cardsPerHour(BigDecimal.ZERO)
                .cardsPerDay(BigDecimal.ZERO)
                .retentionImprovementRate(BigDecimal.ZERO)
                .accuracyTrend(BigDecimal.ZERO)
                .efficiencyScore(BigDecimal.ZERO)
                .velocityCategory("NO_DATA")
                .predictedMonthlyProgress(BigDecimal.ZERO)
                .timeToMasteryDays(BigDecimal.ZERO)
                .consistencyScore(BigDecimal.ZERO)
                .improvementStreak(0)
                .build();
        }

        long periodDays = ChronoUnit.DAYS.between(from, to) + 1;
        double cardsPerDay = (double) cards.size() / periodDays;
        double cardsPerHour = cardsPerDay / 24;

        BigDecimal avgRetention = calculateAverageRetentionRate(cards);
        BigDecimal avgAccuracy = calculateAverageAccuracyFromCards(cards);

        return LearningVelocityDTO.builder()
            .cardsPerHour(BigDecimal.valueOf(cardsPerHour).setScale(2, RoundingMode.HALF_UP))
            .cardsPerDay(BigDecimal.valueOf(cardsPerDay).setScale(2, RoundingMode.HALF_UP))
            .retentionImprovementRate(BigDecimal.ZERO) // Would need historical data
            .accuracyTrend(BigDecimal.ZERO) // Would need historical data
            .efficiencyScore(calculateEfficiencyScoreFromRetentionAndAccuracy(avgRetention, avgAccuracy))
            .velocityCategory(calculateVelocityCategory(cardsPerDay))
            .predictedMonthlyProgress(BigDecimal.valueOf(cardsPerDay * 30).setScale(0, RoundingMode.UP))
            .timeToMasteryDays(calculateEstimatedTimeToMastery(cardsPerDay, avgRetention))
            .consistencyScore(calculateConsistencyScore(cards))
            .improvementStreak(0) // Would need historical data
            .build();
    }

    private Double calculateRetentionRate(List<SpacedRepetitionCard> cards) {
        if (cards.isEmpty()) return 0.0;

        return cards.stream()
            .filter(card -> card.getRetentionRate() != null)
            .mapToDouble(SpacedRepetitionCard::getRetentionRate)
            .average()
            .orElse(0.0);
    }

    private List<DailyMetricDTO> calculateDailyMetrics(User user, LocalDate from, LocalDate to) {
        List<DailyMetricDTO> metrics = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<ReviewSession> daySessions = sessionRepository
                .findSessionsByUserAndDate(user, date.atStartOfDay());

            DailyMetricDTO metric = DailyMetricDTO.builder()
                .date(date)
                .reviewsCompleted(daySessions.stream()
                    .mapToInt(ReviewSession::getCompletedCards)
                    .sum())
                .correctAnswers(daySessions.stream()
                    .mapToInt(ReviewSession::getCorrectAnswers)
                    .sum())
                .accuracy(calculateDayAccuracy(daySessions))
                .studyTimeMinutes(daySessions.stream()
                    .filter(session -> session.getSessionDuration() != null)
                    .mapToLong(session -> session.getSessionDuration() / 60)
                    .sum())
                .newCardsLearned(daySessions.stream()
                    .mapToInt(session -> session.getNewCardsLearned() != null ? session.getNewCardsLearned() : 0)
                    .sum())
                .sessionScore(daySessions.stream()
                    .mapToDouble(session -> session.getTotalSessionScore() != null ? session.getTotalSessionScore() : 0.0)
                    .average()
                    .orElse(0.0))
                .streakDay((int) ChronoUnit.DAYS.between(from, date) + 1)
                .isActiveDay(!daySessions.isEmpty())
                .efficiencyScore(calculateDayEfficiencyScore(daySessions))
                .averageResponseTime(calculateDayAverageResponseTime(daySessions))
                .performanceRate(calculateDayPerformanceRating(daySessions))
                .build();

            metrics.add(metric);
        }

        return metrics;
    }

    private List<AchievementDTO> calculateAchievements(List<ReviewSession> sessions) {
        List<AchievementDTO> achievements = new ArrayList<>();

        // Calculate various achievement metrics
        int totalReviews = sessions.stream().mapToInt(ReviewSession::getCompletedCards).sum();
        int totalSessions = (int) sessions.stream().filter(ReviewSession::getIsCompleted).count();

        // Add milestone achievements
        if (totalReviews >= 100) {
            achievements.add(AchievementDTO.builder()
                .name("Century Club")
                .description("Complete 100 reviews")
                .category("MILESTONE")
                .icon("🎯")
                .unlockedAt(LocalDateTime.now())
                .progress(totalReviews)
                .target(100)
                .isUnlocked(true)
                .rarity("COMMON")
                .points(10)
                .badgeColor("#FFD700")
                .build());
        }

        if (totalSessions >= 10) {
            achievements.add(AchievementDTO.builder()
                .name("Dedicated Learner")
                .description("Complete 10 review sessions")
                .category("CONSISTENCY")
                .icon("📚")
                .unlockedAt(LocalDateTime.now())
                .progress(totalSessions)
                .target(10)
                .isUnlocked(true)
                .rarity("COMMON")
                .points(15)
                .badgeColor("#4CAF50")
                .build());
        }

        return achievements;
    }

    private Double calculateAverageSessionScore(List<ReviewSession> sessions) {
        return sessions.stream()
            .filter(session -> session.getTotalSessionScore() != null)
            .mapToDouble(ReviewSession::getTotalSessionScore)
            .average()
            .orElse(0.0);
    }

    private Integer calculateBestSessionAccuracy(List<ReviewSession> sessions) {
        return sessions.stream()
            .filter(session -> session.getSessionAccuracy() != null)
            .mapToInt(session -> (int) Math.round(session.getSessionAccuracy()))
            .max()
            .orElse(0);
    }

    private Integer calculateNewCardsLearned(List<ReviewSession> sessions) {
        return sessions.stream()
            .filter(session -> session.getNewCardsLearned() != null)
            .mapToInt(ReviewSession::getNewCardsLearned)
            .sum();
    }

    private Integer calculateDifficultCardsMastered(List<ReviewSession> sessions) {
        return sessions.stream()
            .filter(session -> session.getDifficultCardsMastered() != null)
            .mapToInt(ReviewSession::getDifficultCardsMastered)
            .sum();
    }

    private Integer calculateCardsPerMinuteAverage(List<ReviewSession> sessions) {
        if (sessions.isEmpty()) return 0;

        long totalCards = sessions.stream().mapToLong(ReviewSession::getCompletedCards).sum();
        long totalMinutes = sessions.stream()
            .filter(session -> session.getSessionDuration() != null)
            .mapToLong(session -> session.getSessionDuration() / 60)
            .sum();

        return totalMinutes > 0 ? (int) (totalCards / totalMinutes) : 0;
    }

    private Double calculateEfficiencyScore(List<ReviewSession> sessions) {
        if (sessions.isEmpty()) return 0.0;

        Double avgAccuracy = calculateAverageAccuracy(sessions);
        Integer cardsPerMinute = calculateCardsPerMinuteAverage(sessions);

        // Efficiency = (Accuracy * CardsPerMinute) / 100
        return (avgAccuracy * cardsPerMinute) / 100.0;
    }

    private ReviewTrendDTO calculateReviewTrend(User user, LocalDate from, LocalDate to) {
        // This would require comparing with previous periods
        // For now, return a basic trend
        return ReviewTrendDTO.builder()
            .accuracyTrend(BigDecimal.ZERO)
            .retentionTrend(BigDecimal.ZERO)
            .speedTrend(BigDecimal.ZERO)
            .consistencyTrend(BigDecimal.ZERO)
            .overallTrend("STABLE")
            .trendPeriodDays(30)
            .trendStrength(BigDecimal.ONE)
            .prediction("MAINTAIN")
            .build();
    }

    private PerformanceInsightDTO generatePerformanceInsight(User user, List<ReviewSession> sessions, LocalDate from, LocalDate to) {
        List<String> recommendations = new ArrayList<>();
        List<LocalTime> optimalTimes = findOptimalStudyTimes(user);

        // Analyze performance
        Double avgAccuracy = calculateAverageAccuracy(sessions);
        Double retentionRate = calculateRetentionRateFromSessions(sessions);

        // Generate recommendations
        if (avgAccuracy < 70) {
            recommendations.add("Consider reducing your daily new card limit to focus on retention");
        }

        if (retentionRate < 80) {
            recommendations.add("Your retention rate is below 80%. Try reviewing more frequently.");
        }

        // Calculate other metrics
        String difficultyLevel = avgAccuracy > 85 ? "TOO_EASY" :
                               avgAccuracy < 60 ? "TOO_HARD" : "JUST_RIGHT";

        return PerformanceInsightDTO.builder()
            .recommendations(recommendations)
            .optimalStudyTimes(optimalTimes)
            .difficultyLevel(difficultyLevel)
            .weakAreas(calculateWeakAreas(user))
            .strongAreas(calculateStrongAreas(user))
            .studyConsistency(calculateStudyConsistency(sessions))
            .retentionStatus(calculateRetentionStatus(retentionRate))
            .recommendedDailyReviews(calculateRecommendedDailyReviews(user))
            .recommendedNewCardsPerDay(calculateRecommendedNewCardsPerDay(user))
            .focusRecommendation(calculateFocusRecommendation(avgAccuracy))
            .improvementPotential(calculateImprovementPotential(avgAccuracy, retentionRate))
            .build();
    }

    // Helper methods for insights and calculations
    private List<LocalTime> findOptimalStudyTimes(User user) {
        List<ReviewSession> sessions = sessionRepository.findByUserOrderByStartTime(user);

        Map<Integer, Double> performanceByHour = sessions.stream()
            .collect(Collectors.groupingBy(
                session -> session.getStartTime().getHour(),
                Collectors.averagingDouble(session -> {
                    if (session.getSessionAccuracy() != null) {
                        return session.getSessionAccuracy();
                    }
                    return session.getCompletedCards() > 0 ?
                        (double) session.getCorrectAnswers() / session.getCompletedCards() * 100 : 0.0;
                })
            ));

        return performanceByHour.entrySet().stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(3)
            .map(entry -> LocalTime.of(entry.getKey(), 0))
            .collect(Collectors.toList());
    }

    private String calculateLearningVelocityInsight(ReviewStatisticsDTO recentStats) {
        double cardsPerDay = recentStats.getLearningVelocity() != null &&
                           recentStats.getLearningVelocity().getCardsPerDay() != null ?
                           recentStats.getLearningVelocity().getCardsPerDay().doubleValue() : 0.0;

        if (cardsPerDay >= 20) return "VERY_FAST";
        if (cardsPerDay >= 10) return "FAST";
        if (cardsPerDay >= 5) return "MODERATE";
        return "SLOW";
    }

    private String calculateRetentionTrend(User user) {
        // This would require historical data comparison
        return "STABLE";
    }

    private String analyzeDifficultyDistribution(User user) {
        List<SpacedRepetitionCard> cards = cardRepository.findActiveCardsForUser(user);

        long easyCards = cards.stream().filter(card -> card.getPerformanceIndex() != null && card.getPerformanceIndex() >= 80).count();
        long mediumCards = cards.stream().filter(card -> card.getPerformanceIndex() != null &&
            card.getPerformanceIndex() >= 50 && card.getPerformanceIndex() < 80).count();
        long hardCards = cards.stream().filter(card -> card.getPerformanceIndex() != null && card.getPerformanceIndex() < 50).count();

        if (hardCards > easyCards) return "HEAVILY_WEIGHTED_TOWARDS_DIFFICULT";
        if (easyCards > hardCards * 2) return "HEAVILY_WEIGHTED_TOWARDS_EASY";
        return "BALANCED";
    }

    private List<String> generateRecommendations(User user, ReviewStatisticsDTO recentStats) {
        List<String> recommendations = new ArrayList<>();

        if (recentStats.getAverageAccuracy() != null && recentStats.getAverageAccuracy() < 70) {
            recommendations.add("Consider reducing your daily new card limit to focus on retention");
        }

        if (recentStats.getRetentionRate() != null && recentStats.getRetentionRate() < 80) {
            recommendations.add("Your retention rate is below 80%. Try reviewing more frequently.");
        }

        if (recentStats.getStreakDays() != null && recentStats.getStreakDays() < 3) {
            recommendations.add("Try to maintain a consistent daily study routine for better results");
        }

        List<LocalTime> optimalTimes = findOptimalStudyTimes(user);
        if (!optimalTimes.isEmpty()) {
            recommendations.add(String.format("Your best performance is around %s. Try studying at this time.",
                optimalTimes.get(0).format(java.time.format.DateTimeFormatter.ofPattern("h a"))));
        }

        return recommendations;
    }

    private String calculatePerformanceTrend(User user) {
        // This would require historical data analysis
        return "STABLE";
    }

    private String getBestPerformanceTime(User user) {
        List<LocalTime> optimalTimes = findOptimalStudyTimes(user);
        return optimalTimes.isEmpty() ? "Not enough data" :
            optimalTimes.get(0).format(java.time.format.DateTimeFormatter.ofPattern("h a"));
    }

    private String getWorstPerformanceTime(User user) {
        List<ReviewSession> sessions = sessionRepository.findByUserOrderByStartTime(user);

        Map<Integer, Double> performanceByHour = sessions.stream()
            .collect(Collectors.groupingBy(
                session -> session.getStartTime().getHour(),
                Collectors.averagingDouble(session -> {
                    if (session.getSessionAccuracy() != null) {
                        return session.getSessionAccuracy();
                    }
                    return session.getCompletedCards() > 0 ?
                        (double) session.getCorrectAnswers() / session.getCompletedCards() * 100 : 0.0;
                })
            ));

        return performanceByHour.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(entry -> LocalTime.of(entry.getKey(), 0))
            .findFirst()
            .map(time -> time.format(java.time.format.DateTimeFormatter.ofPattern("h a")))
            .orElse("Not enough data");
    }

    private Integer calculateAverageDailyReviews(User user) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        ReviewStatisticsDTO stats = calculateReviewStatistics(user, thirtyDaysAgo, LocalDate.now());

        long periodDays = ChronoUnit.DAYS.between(thirtyDaysAgo, LocalDate.now()) + 1;
        return periodDays > 0 ? (int) (stats.getTotalReviews() / periodDays) : 0;
    }

    private String calculateOverallPerformance(ReviewStatisticsDTO recentStats) {
        if (recentStats.getAverageAccuracy() == null) return "NEEDS_IMPROVEMENT";

        double accuracy = recentStats.getAverageAccuracy();
        if (accuracy >= 90) return "EXCELLENT";
        if (accuracy >= 80) return "GOOD";
        if (accuracy >= 70) return "AVERAGE";
        return "NEEDS_IMPROVEMENT";
    }

    private List<String> calculateRecentAchievements(User user) {
        // This would query achievement data
        return Arrays.asList("Completed 100 reviews", "7-day streak");
    }

    private List<String> calculateAreasForImprovement(User user) {
        // This would analyze performance patterns
        return Arrays.asList("Improve retention rate", "Increase study consistency");
    }

    private String calculateNextMilestone(User user) {
        // This would calculate next achievement milestone
        return "Reach 500 total reviews";
    }

    private Integer calculateDaysToMilestone(User user) {
        // This would estimate days to next milestone
        return 15;
    }

    // Additional helper methods
    private String getModeDisplayName(ReviewMode mode) {
        switch (mode) {
            case DUE_CARDS: return "Due Cards";
            case DIFFICULT_CARDS: return "Difficult Cards";
            case RANDOM_REVIEW: return "Random Review";
            case NEW_CARDS: return "New Cards";
            case ALL_CARDS: return "All Cards";
            case TARGETED_REVIEW: return "Targeted Review";
            default: return mode.name();
        }
    }

    private String getModeDescription(ReviewMode mode) {
        switch (mode) {
            case DUE_CARDS: return "Review cards that are due for repetition";
            case DIFFICULT_CARDS: return "Focus on cards you find challenging";
            case RANDOM_REVIEW: return "Review a random selection of cards";
            case NEW_CARDS: return "Learn new cards you haven't studied yet";
            case ALL_CARDS: return "Review all your active cards";
            case TARGETED_REVIEW: return "Review specific categories or lists";
            default: return "Review mode: " + mode.name();
        }
    }

    private String getModeIcon(ReviewMode mode) {
        switch (mode) {
            case DUE_CARDS: return "📅";
            case DIFFICULT_CARDS: return "🔥";
            case RANDOM_REVIEW: return "🎲";
            case NEW_CARDS: return "✨";
            case ALL_CARDS: return "📚";
            case TARGETED_REVIEW: return "🎯";
            default: return "📝";
        }
    }

    private String getModeColor(ReviewMode mode) {
        switch (mode) {
            case DUE_CARDS: return "#FF6B6B";
            case DIFFICULT_CARDS: return "#FF8E53";
            case RANDOM_REVIEW: return "#4ECDC4";
            case NEW_CARDS: return "#45B7D1";
            case ALL_CARDS: return "#96CEB4";
            case TARGETED_REVIEW: return "#FECA57";
            default: return "#95A5A6";
        }
    }

    private boolean isPopularMode(ReviewMode mode) {
        return mode == ReviewMode.DUE_CARDS || mode == ReviewMode.RANDOM_REVIEW;
    }

    private int estimateDuration(long cardCount) {
        return (int) Math.max(1, cardCount * 2); // Assume 2 minutes per card
    }

    private BigDecimal calculateAverageRetentionRate(List<SpacedRepetitionCard> cards) {
        return cards.stream()
            .filter(card -> card.getRetentionRate() != null)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, cards.size())), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageAccuracyFromCards(List<SpacedRepetitionCard> cards) {
        return cards.stream()
            .filter(card -> card.getTotalReviews() > 0)
            .map(card -> BigDecimal.valueOf((double) card.getCorrectReviews() / card.getTotalReviews() * 100))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, cards.size())), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEfficiencyScoreFromRetentionAndAccuracy(BigDecimal retention, BigDecimal accuracy) {
        return retention.multiply(accuracy)
            .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
    }

    private String calculateVelocityCategory(double cardsPerDay) {
        if (cardsPerDay >= 20) return "VERY_FAST";
        if (cardsPerDay >= 10) return "FAST";
        if (cardsPerDay >= 5) return "MODERATE";
        return "SLOW";
    }

    private BigDecimal calculateEstimatedTimeToMastery(double cardsPerDay, BigDecimal avgRetention) {
        if (cardsPerDay <= 0) return BigDecimal.valueOf(999);

        // Rough estimate: need ~1000 reviews with 90%+ retention
        double reviewsNeeded = 1000;
        double daysNeeded = reviewsNeeded / cardsPerDay;

        // Adjust based on retention
        double retentionAdjustment = avgRetention.doubleValue() / 90.0;
        daysNeeded = daysNeeded / retentionAdjustment;

        return BigDecimal.valueOf(Math.round(daysNeeded));
    }

    private BigDecimal calculateConsistencyScore(List<SpacedRepetitionCard> cards) {
        if (cards.isEmpty()) return BigDecimal.ZERO;

        // Calculate variance in review intervals
        double mean = cards.stream()
            .filter(card -> card.getIntervalDays() != null)
            .mapToInt(SpacedRepetitionCard::getIntervalDays)
            .average()
            .orElse(0.0);

        double variance = cards.stream()
            .filter(card -> card.getIntervalDays() != null)
            .mapToDouble(card -> Math.pow(card.getIntervalDays() - mean, 2))
            .average()
            .orElse(0.0);

        // Lower variance = higher consistency
        double consistency = 1.0 - (variance / (mean * mean));
        return BigDecimal.valueOf(Math.max(0, Math.min(1, consistency))).multiply(BigDecimal.valueOf(100));
    }

    private double calculateDayAccuracy(List<ReviewSession> daySessions) {
        if (daySessions.isEmpty()) return 0.0;

        int totalCards = daySessions.stream().mapToInt(ReviewSession::getCompletedCards).sum();
        int correctCards = daySessions.stream().mapToInt(ReviewSession::getCorrectAnswers).sum();

        return totalCards > 0 ? (double) correctCards / totalCards * 100 : 0.0;
    }

    private Double calculateDayEfficiencyScore(List<ReviewSession> daySessions) {
        if (daySessions.isEmpty()) return 0.0;

        double accuracy = calculateDayAccuracy(daySessions);
        long totalMinutes = daySessions.stream()
            .filter(session -> session.getSessionDuration() != null)
            .mapToLong(session -> session.getSessionDuration() / 60)
            .sum();

        int totalCards = daySessions.stream().mapToInt(ReviewSession::getCompletedCards).sum();
        double cardsPerMinute = totalMinutes > 0 ? (double) totalCards / totalMinutes : 0.0;

        return (accuracy * cardsPerMinute) / 100.0;
    }

    private Integer calculateDayAverageResponseTime(List<ReviewSession> daySessions) {
        if (daySessions.isEmpty()) return 0;

        return (int) daySessions.stream()
            .filter(session -> session.getAverageResponseTime() != null)
            .mapToInt(ReviewSession::getAverageResponseTime)
            .average()
            .orElse(0.0);
    }

    private String calculateDayPerformanceRating(List<ReviewSession> daySessions) {
        double accuracy = calculateDayAccuracy(daySessions);
        if (accuracy >= 90) return "EXCELLENT";
        if (accuracy >= 80) return "GOOD";
        if (accuracy >= 70) return "FAIR";
        return "POOR";
    }

    private Double calculateRetentionRateFromSessions(List<ReviewSession> sessions) {
        // This is a simplified calculation
        return calculateAverageAccuracy(sessions) / 100.0;
    }

    private List<String> calculateWeakAreas(User user) {
        // This would analyze performance by card type, difficulty, etc.
        return Arrays.asList("Irregular verbs", "Advanced vocabulary");
    }

    private List<String> calculateStrongAreas(User user) {
        // This would analyze performance by card type, difficulty, etc.
        return Arrays.asList("Basic vocabulary", "Common phrases");
    }

    private String calculateStudyConsistency(List<ReviewSession> sessions) {
        if (sessions.isEmpty()) return "NO_DATA";

        // Calculate how consistently the user studies
        long totalDays = sessions.stream()
            .map(session -> session.getStartTime().toLocalDate())
            .distinct()
            .count();

        long periodDays = ChronoUnit.DAYS.between(
            sessions.stream().map(ReviewSession::getStartTime).min(LocalDateTime::compareTo).get(),
            sessions.stream().map(ReviewSession::getStartTime).max(LocalDateTime::compareTo).get()
        ) + 1;

        double consistency = (double) totalDays / periodDays;

        if (consistency >= 0.8) return "VERY_CONSISTENT";
        if (consistency >= 0.6) return "CONSISTENT";
        if (consistency >= 0.4) return "INCONSISTENT";
        return "VERY_INCONSISTENT";
    }

    private String calculateRetentionStatus(Double retentionRate) {
        if (retentionRate == null) return "NO_DATA";
        if (retentionRate >= 0.9) return "EXCELLENT";
        if (retentionRate >= 0.8) return "GOOD";
        if (retentionRate >= 0.7) return "AVERAGE";
        return "POOR";
    }

    private Integer calculateRecommendedDailyReviews(User user) {
        // This would be based on user's performance and goals
        return 20;
    }

    private Integer calculateRecommendedNewCardsPerDay(User user) {
        // This would be based on user's performance and retention
        return 5;
    }

    private String calculateFocusRecommendation(Double accuracy) {
        if (accuracy == null) return "FOCUS_ON_CONSISTENCY";
        if (accuracy >= 90) return "INCREASE_DIFFICULTY";
        if (accuracy >= 80) return "MAINTAIN_CURRENT_PACE";
        if (accuracy >= 70) return "FOCUS_ON_RETENTION";
        return "REDUCE_NEW_CARDS";
    }

    private Double calculateImprovementPotential(Double accuracy, Double retentionRate) {
        if (accuracy == null || retentionRate == null) return 0.5;

        // Calculate potential for improvement (0.0 to 1.0)
        double accuracyPotential = (100 - accuracy) / 100;
        double retentionPotential = (100 - retentionRate * 100) / 100;

        return (accuracyPotential + retentionPotential) / 2;
    }
}