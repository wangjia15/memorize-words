package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User statistics entity for tracking user learning metrics.
 *
 * Aggregates various user statistics including learning progress,
 * study session data, and achievement tracking.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "user_statistics")
@EntityListeners(AuditingEntityListener.class)
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Min(value = 0, message = "Total words learned cannot be negative")
    @Column(name = "total_words_learned")
    private Integer totalWordsLearned = 0;

    @Min(value = 0, message = "Total study sessions cannot be negative")
    @Column(name = "total_study_sessions")
    private Integer totalStudySessions = 0;

    @Min(value = 0, message = "Total study time cannot be negative")
    @Column(name = "total_study_time")
    private Integer totalStudyTime = 0; // total time in seconds

    @DecimalMin(value = "0.0", message = "Average accuracy cannot be negative")
    @Column(name = "average_accuracy")
    private Double averageAccuracy = 0.0;

    @Min(value = 0, message = "Current streak cannot be negative")
    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Min(value = 0, message = "Longest streak cannot be negative")
    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UserStatistics() {
    }

    public UserStatistics(User user) {
        this.user = user;
    }

    // Helper methods
    public void recordStudySession(int correctAnswers, int totalWords, int duration) {
        this.totalStudySessions++;
        this.totalStudyTime += duration;
        this.totalWordsLearned += correctAnswers;

        // Update average accuracy
        if (totalWords > 0) {
            double sessionAccuracy = (double) correctAnswers / totalWords;
            this.averageAccuracy = ((this.averageAccuracy * (this.totalStudySessions - 1)) + sessionAccuracy) / this.totalStudySessions;
        }

        // Update streak
        updateStreak();
    }

    public void updateStreak() {
        LocalDate today = LocalDate.now();

        if (lastActivityDate == null) {
            // First activity
            currentStreak = 1;
            longestStreak = 1;
        } else if (lastActivityDate.equals(today.minusDays(1))) {
            // Consecutive day
            currentStreak++;
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
        } else if (lastActivityDate.isBefore(today.minusDays(1))) {
            // Streak broken
            currentStreak = 1;
        }
        // If last activity was today, streak remains unchanged

        lastActivityDate = today;
    }

    public long getStudyTimeInMinutes() {
        return totalStudyTime / 60L;
    }

    public double getStudyTimeInHours() {
        return totalStudyTime / 3600.0;
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

    public Integer getTotalWordsLearned() {
        return totalWordsLearned;
    }

    public void setTotalWordsLearned(Integer totalWordsLearned) {
        this.totalWordsLearned = totalWordsLearned;
    }

    public Integer getTotalStudySessions() {
        return totalStudySessions;
    }

    public void setTotalStudySessions(Integer totalStudySessions) {
        this.totalStudySessions = totalStudySessions;
    }

    public Integer getTotalStudyTime() {
        return totalStudyTime;
    }

    public void setTotalStudyTime(Integer totalStudyTime) {
        this.totalStudyTime = totalStudyTime;
    }

    public Double getAverageAccuracy() {
        return averageAccuracy;
    }

    public void setAverageAccuracy(Double averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
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
        return "UserStatistics{" +
                "id=" + id +
                ", totalWordsLearned=" + totalWordsLearned +
                ", totalStudySessions=" + totalStudySessions +
                ", totalStudyTime=" + totalStudyTime +
                ", averageAccuracy=" + averageAccuracy +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                ", lastActivityDate=" + lastActivityDate +
                '}';
    }
}