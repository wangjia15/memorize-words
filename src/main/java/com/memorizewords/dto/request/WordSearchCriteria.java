package com.memorizewords.dto.request;

import lombok.Data;

/**
 * Criteria for searching words with filters.
 */
@Data
public class WordSearchCriteria {

    private String word;

    private String language;

    private DifficultyLevel difficulty;

    private String category;

    private String tag;

    private Boolean isPublic;

    private String createdBy;
}