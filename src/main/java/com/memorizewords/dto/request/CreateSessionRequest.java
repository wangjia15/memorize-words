package com.memorizewords.dto.request;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.LearningMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * Request DTO for creating a new learning session.
 */
@Data
public class CreateSessionRequest {

    @NotNull(message = "Learning mode is required")
    private LearningMode mode;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    private Long vocabularyListId;

    @Min(value = 1, message = "Word count must be at least 1")
    private Integer wordCount = 10;

    private Set<Long> specificWordIds;

    // Session settings
    private Boolean autoAdvance = true;

    private Boolean showDefinitionFirst = false;

    private Boolean enablePronunciation = true;

    private Boolean enableHints = true;

    @Min(value = 1, message = "Time limit must be at least 1 second")
    private Integer timeLimit; // Time limit per word in seconds

    private Boolean shuffleWords = true;

    private Boolean repeatIncorrect = true;
}