package com.memorizewords.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for vocabulary list data.
 */
@Data
public class VocabularyListDto {

    private Long id;

    private String name;

    private String description;

    private Long ownerId;

    private String ownerUsername;

    private Set<WordSummaryDto> words;

    private Boolean isPublic;

    private Boolean isShared;

    private Set<String> tags;

    private ListType type;

    private Integer wordCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}