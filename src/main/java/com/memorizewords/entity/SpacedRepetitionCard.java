package com.memorizewords.entity;

import com.memorizewords.enums.ReviewOutcome;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a word card with spaced repetition data.
 */
@Entity
@Table(name = "spaced_repetition_cards")
@Data
@EqualsAndHashCode(callSuper = true)
public class SpacedRepetitionCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 1;

    @Column(name = "ease_factor", precision = 10, scale = 2, nullable = false)
    private BigDecimal easeFactor = new BigDecimal("2.5");

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate = LocalDateTime.now();

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Column(name = "next_review")
    private LocalDateTime nextReview;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;

    @Column(name = "correct_reviews", nullable = false)
    private Integer correctReviews = 0;

    @Column(name = "consecutive_correct", nullable = false)
    private Integer consecutiveCorrect = 0;

    @Column(name = "consecutive_incorrect", nullable = false)
    private Integer consecutiveIncorrect = 0;

    @Column(name = "difficulty_rating", precision = 3, scale = 2)
    private BigDecimal difficultyRating;

    @Column(name = "performance_index", precision = 3, scale = 2)
    private BigDecimal performanceIndex;

    @Column(name = "average_response_time")
    private Integer averageResponseTime;

    @Column(name = "stability_factor", precision = 10, scale = 2)
    private BigDecimal stabilityFactor = BigDecimal.ONE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_suspended")
    private Boolean isSuspended = false;

    @Column(name = "total_study_time")
    private Long totalStudyTime = 0L;

    @Column(name = "last_review_outcome")
    @Enumerated(EnumType.STRING)
    private ReviewOutcome lastReviewOutcome;

    @Column(name = "review_count_by_outcome_again", nullable = false)
    private Integer reviewCountAgain = 0;

    @Column(name = "review_count_by_outcome_hard", nullable = false)
    private Integer reviewCountHard = 0;

    @Column(name = "review_count_by_outcome_good", nullable = false)
    private Integer reviewCountGood = 0;

    @Column(name = "review_count_by_outcome_easy", nullable = false)
    private Integer reviewCountEasy = 0;

    @Column(name = "card_age_days")
    private Integer cardAgeDays = 0;

    @Column(name = "retention_rate", precision = 5, scale = 2)
    private BigDecimal retentionRate;

    @ElementCollection
    @CollectionTable(name = "spaced_repetition_review_history",
                    joinColumns = @JoinColumn(name = "card_id"))
    @OrderColumn(name = "review_number")
    private List<ReviewHistory> reviewHistory = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dueDate == null) {
            dueDate = LocalDateTime.now();
        }
        if (easeFactor == null) {
            easeFactor = new BigDecimal("2.5");
        }
        if (intervalDays == null) {
            intervalDays = 1;
        }
    }

    public void recordReview(ReviewOutcome outcome, int responseTime) {
        totalReviews++;

        // Update outcome counters
        switch (outcome) {
            case AGAIN:
                reviewCountAgain++;
                consecutiveCorrect = 0;
                consecutiveIncorrect++;
                break;
            case HARD:
                reviewCountHard++;
                correctReviews++;
                consecutiveCorrect++;
                consecutiveIncorrect = 0;
                break;
            case GOOD:
                reviewCountGood++;
                correctReviews++;
                consecutiveCorrect++;
                consecutiveIncorrect = 0;
                break;
            case EASY:
                reviewCountEasy++;
                correctReviews++;
                consecutiveCorrect++;
                consecutiveIncorrect = 0;
                break;
        }

        // Update response time metrics
        if (averageResponseTime == null) {
            averageResponseTime = responseTime;
        } else {
            averageResponseTime = (averageResponseTime + responseTime) / 2;
        }

        totalStudyTime += responseTime;
        lastReviewOutcome = outcome;
        lastReviewed = LocalDateTime.now();

        // Update performance metrics
        updatePerformanceMetrics();

        // Add to review history
        ReviewHistory history = new ReviewHistory();
        history.setReviewNumber(totalReviews);
        history.setOutcome(outcome);
        history.setResponseTime(responseTime);
        history.setReviewedAt(lastReviewed);
        history.setIntervalBeforeReview(intervalDays);
        history.setEaseFactorBeforeReview(easeFactor);
        reviewHistory.add(history);
    }

    public void updatePerformanceMetrics() {
        // Calculate accuracy
        double accuracy = totalReviews > 0 ? (double) correctReviews / totalReviews : 0.0;

        // Calculate difficulty rating based on accuracy and response times
        double difficultyScore = 1.0 - accuracy;
        if (averageResponseTime != null) {
            // Normalize response time (assume 10 seconds is difficult)
            double responseTimeScore = Math.min(averageResponseTime / 10000.0, 1.0);
            difficultyScore = (difficultyScore + responseTimeScore) / 2.0;
        }

        this.difficultyRating = new BigDecimal(difficultyScore)
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Calculate performance index (0-100)
        double performanceScore = accuracy * 100.0;
        if (consecutiveCorrect > 0) {
            performanceScore += consecutiveCorrect * 2.0;
        }
        if (consecutiveIncorrect > 0) {
            performanceScore -= consecutiveIncorrect * 5.0;
        }
        performanceScore = Math.max(0.0, Math.min(100.0, performanceScore));

        this.performanceIndex = new BigDecimal(performanceScore)
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Calculate retention rate
        if (totalReviews > 3) {
            // Retention rate based on recent performance
            int recentReviews = Math.min(10, totalReviews);
            double recentCorrect = reviewHistory.stream()
                .skip(Math.max(0, reviewHistory.size() - recentReviews))
                .filter(h -> h.getOutcome() != ReviewOutcome.AGAIN)
                .count();
            double retention = recentCorrect / recentReviews;
            this.retentionRate = new BigDecimal(retention)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public boolean isDue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    public boolean isDueWithin(int hours) {
        return dueDate != null &&
               dueDate.isBefore(LocalDateTime.now().plusHours(hours));
    }

    public int getDaysUntilDue() {
        if (dueDate == null) return 0;
        return (int) java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    // Additional getter methods for DTO compatibility
    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public com.memorizewords.enums.DifficultyLevel getDifficultyLevel() {
        if (difficultyRating == null) return null;

        double rating = difficultyRating.doubleValue();
        if (rating >= 0.8) return com.memorizewords.enums.DifficultyLevel.ADVANCED;
        if (rating >= 0.6) return com.memorizewords.enums.DifficultyLevel.INTERMEDIATE;
        if (rating >= 0.4) return com.memorizewords.enums.DifficultyLevel.BEGINNER;
        return com.memorizewords.enums.DifficultyLevel.BASIC;
    }

    public Double getDifficultyRating() {
        return difficultyRating != null ? difficultyRating.doubleValue() : null;
    }

    public Boolean getIsDue() {
        return isDue();
    }

    public Boolean getIsNew() {
        return totalReviews == 0;
    }

    public Boolean getIsDifficult() {
        return difficultyRating != null && difficultyRating.compareTo(new BigDecimal("0.5")) > 0;
    }

    public List<String> getReviewHistory() {
        return reviewHistory.stream()
            .map(history -> String.format("%d: %s (%dms)",
                history.getReviewNumber(),
                history.getOutcome(),
                history.getResponseTime()))
            .toList();
    }

    @Embeddable
    @Data
    public static class ReviewHistory {
        @Column(name = "review_number")
        private Integer reviewNumber;

        @Enumerated(EnumType.STRING)
        @Column(name = "outcome")
        private ReviewOutcome outcome;

        @Column(name = "response_time")
        private Integer responseTime;

        @Column(name = "reviewed_at")
        private LocalDateTime reviewedAt;

        @Column(name = "interval_before_review")
        private Integer intervalBeforeReview;

        @Column(name = "ease_factor_before_review")
        private BigDecimal easeFactorBeforeReview;
    }
}