package com.memorizewords.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for submitting an answer during a learning session.
 */
@Data
public class SubmitAnswerRequest {

    @NotNull(message = "Word ID is required")
    private Long wordId;

    @NotNull(message = "Correct flag is required")
    private Boolean isCorrect;

    @Size(max = 500, message = "User answer must be less than 500 characters")
    private String userAnswer;

    @NotNull(message = "Time spent is required")
    @Min(value = 0, message = "Time spent cannot be negative")
    private Long timeSpent; // Time spent in seconds

    private Integer attemptNumber = 1;

    private Boolean hintUsed = false;

    private Boolean pronunciationPlayed = false;

    @Min(value = 1, message = "Confidence level must be between 1 and 5")
    private Integer confidenceLevel; // 1-5 scale

    @Min(value = 1, message = "Difficulty rating must be between 1 and 5")
    private Integer difficultyRating; // User's perception of difficulty 1-5
}