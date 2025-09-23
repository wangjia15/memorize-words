package com.memorizewords.dto.response;

import lombok.Data;

/**
 * DTO for importing word data.
 */
@Data
public class WordImportDto {

    private String word;

    private String language;

    private String definition;

    private String pronunciation;

    private String example;

    private String difficulty;

    private String categories;

    private String tags;

    private String isPublic;
}