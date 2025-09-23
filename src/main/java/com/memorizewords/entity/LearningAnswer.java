package com.memorizewords.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Learning answer entity representing user responses during learning sessions.
 */
@Entity
@Table(name = "learning_answers")
@Data
@EqualsAndHashCode(callSuper = true)
public class LearningAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "user_answer", length = 500)
    private String userAnswer;

    @Column(name = "correct_answer", length = 500)
    private String correctAnswer;

    @Column(name = "time_spent", nullable = false) // Time spent in seconds
    private Long timeSpent = 0L;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;

    @Column(name = "hint_used")
    private Boolean hintUsed = false;

    @Column(name = "pronunciation_played")
    private Boolean pronunciationPlayed = false;

    @Column(name = "confidence_level") // 1-5 scale
    private Integer confidenceLevel;

    @Column(name = "difficulty_rating") // User's perception of difficulty 1-5
    private Integer difficultyRating;

    @PrePersist
    public void prePersist() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
        if (correctAnswer == null && word != null) {
            correctAnswer = word.getWord();
        }
    }

    public boolean isFirstAttempt() {
        return attemptNumber == 1;
    }

    public boolean isMultipleAttempt() {
        return attemptNumber > 1;
    }

    public double getAccuracyScore() {
        if (isCorrect) {
            // Reduce score for multiple attempts or hint usage
            double score = 1.0;
            if (attemptNumber > 1) {
                score -= (attemptNumber - 1) * 0.1;
            }
            if (hintUsed) {
                score -= 0.2;
            }
            return Math.max(score, 0.1); // Minimum score of 0.1
        }
        return 0.0;
    }

    public String getPerformanceCategory() {
        if (!isCorrect) {
            return "INCORRECT";
        }
        if (isFirstAttempt() && !hintUsed && timeSpent < 10) {
            return "EXCELLENT";
        }
        if (isFirstAttempt() && !hintUsed) {
            return "GOOD";
        }
        if (isFirstAttempt()) {
            return "FAIR";
        }
        return "NEEDS_PRACTICE";
    }
}