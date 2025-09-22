package com.memorizewords.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * Request DTO for removing words from a vocabulary list.
 */
@Data
public class RemoveWordsRequest {

    @NotEmpty(message = "Word IDs cannot be empty")
    @NotNull(message = "Word IDs are required")
    private Set<Long> wordIds;
}