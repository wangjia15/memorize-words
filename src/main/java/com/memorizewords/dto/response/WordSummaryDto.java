package com.memorizewords.dto.response;

import lombok.Data;

/**
 * Summary DTO for word data (used in lists).
 */
@Data
public class WordSummaryDto {

    private Long id;

    private String word;

    private String language;

    private DifficultyLevel difficulty;
}