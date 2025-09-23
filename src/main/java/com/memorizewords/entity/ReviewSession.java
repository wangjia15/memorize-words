package com.memorizewords.entity;

import com.memorizewords.enums.ReviewMode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a review session for spaced repetition.
 */
@Entity
@Table(name = "review_sessions")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private ReviewMode mode;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_cards", nullable = false)
    private Integer totalCards = 0;

    @Column(name = "completed_cards", nullable = false)
    private Integer completedCards = 0;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @Column(name = "average_response_time")
    private Integer averageResponseTime;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "session_accuracy", precision = 5, scale = 2)
    private BigDecimal sessionAccuracy;

    @Column(name = "session_duration")
    private Long sessionDuration;

    @Column(name = "cards_per_minute")
    private BigDecimal cardsPerMinute;

    @Column(name = "efficiency_score", precision = 5, scale = 2)
    private BigDecimal efficiencyScore;

    @Column(name = "difficulty_level", precision = 3, scale = 2)
    private BigDecimal difficultyLevel;

    @Column(name = "focus_score", precision = 5, scale = 2)
    private BigDecimal focusScore;

    @Column(name = "learning_velocity", precision = 5, scale = 2)
    private BigDecimal learningVelocity;

    @Column(name = "retention_improvement", precision = 5, scale = 2)
    private BigDecimal retentionImprovement;

    @Column(name = "streak_days_affected")
    private Integer streakDaysAffected;

    @Column(name = "new_cards_learned")
    private Integer newCardsLearned = 0;

    @Column(name = "difficult_cards_mastered")
    private Integer difficultCardsMastered = 0;

    @Column(name = "time_efficiency_bonus", precision = 3, scale = 2)
    private BigDecimal timeEfficiencyBonus;

    @Column(name = "accuracy_bonus", precision = 3, scale = 2)
    private BigDecimal accuracyBonus;

    @Column(name = "consistency_bonus", precision = 3, scale = 2)
    private BigDecimal consistencyBonus;

    @Column(name = "total_session_score", precision = 6, scale = 2)
    private BigDecimal totalSessionScore;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewSessionCard> cards = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (totalCards == null) {
            totalCards = cards.size();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (isCompleted && endTime == null) {
            endTime = LocalDateTime.now();
            calculateFinalStatistics();
        }
    }

    public void addCard(ReviewSessionCard card) {
        cards.add(card);
        card.setSession(this);
        if (totalCards == null) {
            totalCards = cards.size();
        }
    }

    public void removeCard(ReviewSessionCard card) {
        cards.remove(card);
        card.setSession(null);
        totalCards = cards.size();
    }

    public ReviewSessionCard getCurrentCard() {
        if (cards.isEmpty() || completedCards >= cards.size()) {
            return null;
        }
        return cards.get(completedCards);
    }

    public boolean hasMoreCards() {
        return completedCards < cards.size();
    }

    public double getProgressPercentage() {
        return totalCards > 0 ? (double) completedCards / totalCards * 100 : 0.0;
    }

    public double getAccuracyPercentage() {
        return completedCards > 0 ? (double) correctAnswers / completedCards * 100 : 0.0;
    }

    public void updateStatistics(ReviewSessionCard card) {
        completedCards++;

        if (card.getOutcome() == ReviewOutcome.GOOD ||
            card.getOutcome() == ReviewOutcome.EASY) {
            correctAnswers++;
        }

        // Update average response time
        updateAverageResponseTime();

        // Update session accuracy
        sessionAccuracy = new BigDecimal(getAccuracyPercentage())
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Update cards per minute
        updateCardsPerMinute();

        // Update other metrics
        updatePerformanceMetrics();
    }

    private void updateAverageResponseTime() {
        int totalTime = cards.stream()
            .filter(c -> c.getResponseTime() != null && c.getResponseTime() > 0)
            .mapToInt(ReviewSessionCard::getResponseTime)
            .sum();

        long completedWithTime = cards.stream()
            .filter(c -> c.getResponseTime() != null && c.getResponseTime() > 0)
            .count();

        averageResponseTime = completedWithTime > 0 ? totalTime / (int) completedWithTime : 0;
    }

    private void updateCardsPerMinute() {
        if (sessionDuration != null && sessionDuration > 0) {
            double minutes = sessionDuration / 60.0;
            double cardsPerMinuteValue = completedCards / minutes;
            cardsPerMinute = new BigDecimal(cardsPerMinuteValue)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    private void updatePerformanceMetrics() {
        // Calculate efficiency score (0-100)
        double efficiency = calculateEfficiencyScore();
        efficiencyScore = new BigDecimal(efficiency)
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Calculate difficulty level
        difficultyLevel = calculateDifficultyLevel();

        // Calculate focus score
        focusScore = calculateFocusScore();

        // Calculate learning velocity
        learningVelocity = calculateLearningVelocity();

        // Calculate total session score
        totalSessionScore = calculateTotalSessionScore();
    }

    private double calculateEfficiencyScore() {
        if (completedCards == 0) return 0.0;

        double accuracy = getAccuracyPercentage();
        double speedScore = 100.0;

        if (averageResponseTime != null && averageResponseTime > 0) {
            // Optimal response time is around 3-5 seconds
            double optimalTime = 4000.0; // 4 seconds
            speedScore = Math.max(0.0, 100.0 - Math.abs(averageResponseTime - optimalTime) / optimalTime * 50.0);
        }

        return (accuracy * 0.7 + speedScore * 0.3);
    }

    private BigDecimal calculateDifficultyLevel() {
        if (cards.isEmpty()) return BigDecimal.ZERO;

        double totalDifficulty = cards.stream()
            .filter(c -> c.getCard() != null && c.getCard().getDifficultyRating() != null)
            .mapToDouble(c -> c.getCard().getDifficultyRating().doubleValue())
            .average()
            .orElse(0.0);

        return new BigDecimal(totalDifficulty)
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateFocusScore() {
        if (averageResponseTime == null) return BigDecimal.ZERO;

        // Focus score based on consistency of response times
        double variance = cards.stream()
            .filter(c -> c.getResponseTime() != null)
            .mapToInt(ReviewSessionCard::getResponseTime)
            .mapToDouble(time -> Math.pow(time - averageResponseTime, 2))
            .average()
            .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);
        double focusScore = Math.max(0.0, 100.0 - (standardDeviation / averageResponseTime) * 100.0);

        return new BigDecimal(focusScore)
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateLearningVelocity() {
        if (completedCards < 2) return BigDecimal.ZERO;

        // Calculate learning velocity based on improvement over session
        double firstHalfAccuracy = cards.stream()
            .limit(completedCards / 2)
            .filter(c -> c.getOutcome() != null)
            .mapToLong(c -> c.getOutcome() == ReviewOutcome.AGAIN ? 0 : 1)
            .average()
            .orElse(0.0) * 100.0;

        double secondHalfAccuracy = cards.stream()
            .skip(completedCards / 2)
            .limit(completedCards - completedCards / 2)
            .filter(c -> c.getOutcome() != null)
            .mapToLong(c -> c.getOutcome() == ReviewOutcome.AGAIN ? 0 : 1)
            .average()
            .orElse(0.0) * 100.0;

        double velocity = secondHalfAccuracy - firstHalfAccuracy;
        return new BigDecimal(velocity)
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateTotalSessionScore() {
        if (efficiencyScore == null) return BigDecimal.ZERO;

        double total = efficiencyScore.doubleValue();

        if (accuracyBonus != null) {
            total += accuracyBonus.doubleValue();
        }

        if (timeEfficiencyBonus != null) {
            total += timeEfficiencyBonus.doubleValue();
        }

        if (consistencyBonus != null) {
            total += consistencyBonus.doubleValue();
        }

        return new BigDecimal(Math.max(0.0, Math.min(100.0, total)))
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void calculateFinalStatistics() {
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            sessionDuration = duration.getSeconds();
        }

        updateCardsPerMinute();
        updatePerformanceMetrics();

        // Calculate session completion bonuses
        calculateCompletionBonuses();
    }

    private void calculateCompletionBonuses() {
        // Accuracy bonus
        double accuracy = getAccuracyPercentage();
        if (accuracy >= 90.0) {
            accuracyBonus = new BigDecimal("10.0");
        } else if (accuracy >= 80.0) {
            accuracyBonus = new BigDecimal("5.0");
        } else if (accuracy >= 70.0) {
            accuracyBonus = new BigDecimal("2.0");
        }

        // Time efficiency bonus
        if (cardsPerMinute != null && cardsPerMinute.doubleValue() > 15.0) {
            timeEfficiencyBonus = new BigDecimal("5.0");
        } else if (cardsPerMinute != null && cardsPerMinute.doubleValue() > 10.0) {
            timeEfficiencyBonus = new BigDecimal("2.0");
        }

        // Consistency bonus (all cards completed)
        if (completedCards.equals(totalCards)) {
            consistencyBonus = new BigDecimal("3.0");
        }

        // Recalculate total score with bonuses
        totalSessionScore = calculateTotalSessionScore();
    }

    public Integer getCurrentCardIndex() {
        return completedCards;
    }

    public Integer getRemainingCards() {
        return totalCards - completedCards;
    }

    public Double getProgressPercentage() {
        return totalCards > 0 ? (double) completedCards / totalCards * 100 : 0.0;
    }
}