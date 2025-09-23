package com.memorizewords.dto.response;

import com.memorizewords.enums.DifficultyLevel;
import lombok.Data;

/**
 * Response DTO for word data in learning sessions.
 */
@Data
public class LearningWordDto {

    private Long id;

    private String word;

    private String definition;

    private String pronunciation;

    private String example;

    private DifficultyLevel difficulty;

    private Integer attempts;

    private Integer correctAttempts;

    private Boolean isCompleted;

    private Long timeSpent;

    private String performanceCategory;

    private Double accuracyScore;
}