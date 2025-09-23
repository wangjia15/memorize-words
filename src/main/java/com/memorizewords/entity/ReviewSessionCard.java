package com.memorizewords.entity;

import com.memorizewords.enums.ReviewOutcome;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a card within a review session.
 */
@Entity
@Table(name = "review_session_cards")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewSessionCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private SpacedRepetitionCard card;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private ReviewOutcome outcome;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "confidence_level", precision = 3, scale = 2)
    private BigDecimal confidenceLevel;

    @Column(name = "difficulty_rating", precision = 3, scale = 2)
    private BigDecimal difficultyRating;

    @Column(name = "hint_used")
    private Boolean hintUsed = false;

    @Column(name = "time_taken", precision = 10, scale = 3)
    private BigDecimal timeTaken;

    @Column(name = "accuracy_score", precision = 5, scale = 2)
    private BigDecimal accuracyScore;

    @Column(name = "performance_category")
    private String performanceCategory;

    @Column(name = "interval_before_review")
    private Integer intervalBeforeReview;

    @Column(name = "ease_factor_before_review")
    private BigDecimal easeFactorBeforeReview;

    @Column(name = "interval_after_review")
    private Integer intervalAfterReview;

    @Column(name = "ease_factor_after_review")
    private BigDecimal easeFactorAfterReview;

    @Column(name = "review_number")
    private Integer reviewNumber;

    @Column(name = "consecutive_correct_before")
    private Integer consecutiveCorrectBefore;

    @Column(name = "streak_broken")
    private Boolean streakBroken = false;

    @Column(name = "new_interval_calculated")
    private Boolean newIntervalCalculated = false;

    @Column(name = "learning_gain", precision = 5, scale = 2)
    private BigDecimal learningGain;

    @Column(name = "retention_risk")
    private BigDecimal retentionRisk;

    @PrePersist
    public void prePersist() {
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
        if (outcome != null) {
            isCorrect = outcome != ReviewOutcome.AGAIN;
            if (timeTaken != null) {
                timeTaken = new BigDecimal(responseTime).divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    public void setReviewOutcome(ReviewOutcome outcome, int responseTime) {
        this.outcome = outcome;
        this.responseTime = responseTime;
        this.reviewedAt = LocalDateTime.now();
        this.isCorrect = outcome != ReviewOutcome.AGAIN;

        // Calculate time taken in seconds
        this.timeTaken = new BigDecimal(responseTime)
            .divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP);

        // Calculate performance metrics
        calculatePerformanceMetrics();
    }

    private void calculatePerformanceMetrics() {
        if (outcome == null) return;

        // Calculate accuracy score based on outcome and response time
        double baseScore = getBaseAccuracyScore();
        double timeBonus = calculateTimeBonus();
        double totalScore = Math.min(100.0, baseScore + timeBonus);

        this.accuracyScore = new BigDecimal(totalScore)
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Determine performance category
        this.performanceCategory = determinePerformanceCategory(totalScore);

        // Calculate learning gain
        this.learningGain = calculateLearningGain();

        // Calculate retention risk
        this.retentionRisk = calculateRetentionRisk();
    }

    private double getBaseAccuracyScore() {
        switch (outcome) {
            case EASY:
                return 95.0;
            case GOOD:
                return 85.0;
            case HARD:
                return 70.0;
            case AGAIN:
                return 0.0;
            default:
                return 0.0;
        }
    }

    private double calculateTimeBonus() {
        if (responseTime == null) return 0.0;

        // Bonus for quick responses (under 3 seconds)
        if (responseTime < 3000) {
            return 5.0;
        }
        // Penalty for slow responses (over 15 seconds)
        else if (responseTime > 15000) {
            return -10.0;
        }
        return 0.0;
    }

    private String determinePerformanceCategory(double score) {
        if (score >= 90.0) {
            return "EXCELLENT";
        } else if (score >= 80.0) {
            return "GOOD";
        } else if (score >= 70.0) {
            return "FAIR";
        } else if (score >= 50.0) {
            return "POOR";
        } else {
            return "NEEDS_WORK";
        }
    }

    private BigDecimal calculateLearningGain() {
        if (easeFactorBeforeReview == null || easeFactorAfterReview == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal gain = easeFactorAfterReview.subtract(easeFactorBeforeReview);
        return gain.setScale(3, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateRetentionRisk() {
        if (outcome == null) return BigDecimal.ZERO;

        switch (outcome) {
            case AGAIN:
                return new BigDecimal("0.8"); // 80% risk of forgetting
            case HARD:
                return new BigDecimal("0.4"); // 40% risk of forgetting
            case GOOD:
                return new BigDecimal("0.1"); // 10% risk of forgetting
            case EASY:
                return new BigDecimal("0.05"); // 5% risk of forgetting
            default:
                return BigDecimal.ZERO;
        }
    }

    public boolean isHighRisk() {
        return retentionRisk != null && retentionRisk.compareTo(new BigDecimal("0.5")) > 0;
    }

    public boolean isExcellentPerformance() {
        return "EXCELLENT".equals(performanceCategory);
    }

    public boolean needsAttention() {
        return "NEEDS_WORK".equals(performanceCategory) || isHighRisk();
    }

    // Additional getter methods for DTO compatibility
    public ReviewOutcome getReviewOutcome() {
        return outcome;
    }

    public Boolean getWasCorrect() {
        return isCorrect;
    }

    public Double getScore() {
        return accuracyScore != null ? accuracyScore.doubleValue() : null;
    }

    public String getUserAnswer() {
        return null; // This would be populated from user input
    }

    public Integer getHintUsed() {
        return hintUsed != null && hintUsed ? 1 : 0;
    }

    public String getConfidenceLevel() {
        return null; // This would be populated from user input
    }

    public Boolean getMarkedAsDifficult() {
        return difficultyRating != null && difficultyRating.compareTo(new BigDecimal("0.5")) > 0;
    }

    public String getNotes() {
        return null; // This would be populated from user input
    }

    public Boolean getIsNewCard() {
        return card != null && card.getTotalReviews() == 0;
    }

    public Boolean getWasDifficult() {
        return difficultyRating != null && difficultyRating.compareTo(new BigDecimal("0.5")) > 0;
    }

    public Boolean getIsCompleted() {
        return outcome != null;
    }

    public LocalDateTime getReviewTime() {
        return reviewedAt;
    }
}