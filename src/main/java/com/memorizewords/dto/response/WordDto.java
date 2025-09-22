package com.memorizewords.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for word data.
 */
@Data
public class WordDto {

    private Long id;

    private String word;

    private String language;

    private String definition;

    private String pronunciation;

    private String example;

    private DifficultyLevel difficulty;

    private Set<WordCategory> categories;

    private Set<String> tags;

    private Boolean isPublic;

    private Long createdByUserId;

    private String createdByUsername;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}