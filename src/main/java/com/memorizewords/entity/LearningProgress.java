package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Learning progress entity for tracking user word mastery.
 *
 * Implements spaced repetition algorithm to track user progress
 * in learning words, including mastery levels and review scheduling.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "learning_progress")
@EntityListeners(AuditingEntityListener.class)
public class LearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false)
    private MasteryLevel masteryLevel = MasteryLevel.NEW;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "total_attempts")
    private Integer totalAttempts = 0;

    @Column(name = "consecutive_correct")
    private Integer consecutiveCorrect = 0;

    @NotNull
    @DecimalMin(value = "1.3")
    @DecimalMax(value = "2.5")
    @Column(name = "ease_factor", nullable = false)
    private Double easeFactor = 2.5;

    @Column(name = "interval_days")
    private Integer intervalDays = 0;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MasteryLevel {
        NEW, LEARNING, FAMILIAR, MASTERED
    }

    // Constructors
    public LearningProgress() {
    }

    public LearningProgress(User user, Word word) {
        this.user = user;
        this.word = word;
    }

    // Helper methods for spaced repetition
    public void recordAnswer(boolean isCorrect) {
        this.totalAttempts++;
        if (isCorrect) {
            this.correctAnswers++;
            this.consecutiveCorrect++;
        } else {
            this.consecutiveCorrect = 0;
        }
        this.lastReviewed = LocalDateTime.now();
    }

    public double getAccuracy() {
        if (totalAttempts == 0) return 0.0;
        return (double) correctAnswers / totalAttempts;
    }

    public void updateMasteryLevel() {
        double accuracy = getAccuracy();
        if (accuracy >= 0.9 && consecutiveCorrect >= 3) {
            masteryLevel = MasteryLevel.MASTERED;
        } else if (accuracy >= 0.7 && consecutiveCorrect >= 2) {
            masteryLevel = MasteryLevel.FAMILIAR;
        } else if (accuracy >= 0.5 || consecutiveCorrect >= 1) {
            masteryLevel = MasteryLevel.LEARNING;
        } else {
            masteryLevel = MasteryLevel.NEW;
        }
    }

    public boolean isDueForReview() {
        return nextReviewDate == null || nextReviewDate.isBefore(LocalDateTime.now());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public MasteryLevel getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(MasteryLevel masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Integer getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public void setConsecutiveCorrect(Integer consecutiveCorrect) {
        this.consecutiveCorrect = consecutiveCorrect;
    }

    public Double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(Double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }

    public LocalDateTime getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDateTime nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public LocalDateTime getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(LocalDateTime lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "LearningProgress{" +
                "id=" + id +
                ", masteryLevel=" + masteryLevel +
                ", correctAnswers=" + correctAnswers +
                ", totalAttempts=" + totalAttempts +
                ", consecutiveCorrect=" + consecutiveCorrect +
                ", nextReviewDate=" + nextReviewDate +
                '}';
    }
}