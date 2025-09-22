package com.memorizewords.entity;

import com.memorizewords.enum.LearningStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entity tracking user progress for individual words.
 */
@Entity
@Table(name = "user_word_progress")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordProgress extends BaseEntity {

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
    @Column(nullable = false)
    private LearningStatus status = LearningStatus.NEW;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "streak_count")
    private Integer streakCount = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "confidence_level")
    private Integer confidenceLevel = 0;

    @Column(name = "times_seen")
    private Integer timesSeen = 0;

    @Column(name = "total_time_spent")
    private Long totalTimeSpent = 0L;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Column(name = "difficulty_rating")
    private Integer difficultyRating = 0;

    @Column(name = "notes")
    private String notes;
}