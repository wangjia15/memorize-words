package com.memorizewords.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for learning answer data.
 */
@Data
public class LearningAnswerDto {

    private Long id;

    private Long sessionId;

    private Long wordId;

    private String wordText;

    private Boolean isCorrect;

    private String userAnswer;

    private String correctAnswer;

    private Long timeSpent;

    private LocalDateTime answeredAt;

    private Integer attemptNumber;

    private Boolean hintUsed;

    private Boolean pronunciationPlayed;

    private Integer confidenceLevel;

    private Integer difficultyRating;

    private Double accuracyScore;

    private String performanceCategory;
}