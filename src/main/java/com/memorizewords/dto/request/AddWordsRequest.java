package com.memorizewords.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * Request DTO for adding words to a vocabulary list.
 */
@Data
public class AddWordsRequest {

    @NotEmpty(message = "Word IDs cannot be empty")
    @NotNull(message = "Word IDs are required")
    private Set<Long> wordIds;
}