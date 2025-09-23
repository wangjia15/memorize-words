package com.memorizewords.dto.request;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.WordCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * Request DTO for updating an existing word.
 */
@Data
public class UpdateWordRequest {

    @Size(max = 2000, message = "Definition must be less than 2000 characters")
    private String definition;

    @Size(max = 200, message = "Pronunciation must be less than 200 characters")
    private String pronunciation;

    @Size(max = 2000, message = "Example must be less than 2000 characters")
    private String example;

    private DifficultyLevel difficulty;

    private Boolean isPublic;

    private Set<WordCategory> categories;

    private Set<String> tags;
}