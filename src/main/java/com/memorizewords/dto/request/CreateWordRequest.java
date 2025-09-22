package com.memorizewords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new word.
 */
@Data
public class CreateWordRequest {

    @NotBlank(message = "Word is required")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String word;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must be less than 50 characters")
    private String language;

    @Size(max = 2000, message = "Definition must be less than 2000 characters")
    private String definition;

    @Size(max = 200, message = "Pronunciation must be less than 200 characters")
    private String pronunciation;

    @Size(max = 2000, message = "Example must be less than 2000 characters")
    private String example;

    private DifficultyLevel difficulty = DifficultyLevel.BEGINNER;

    private Boolean isPublic = false;
}