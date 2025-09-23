package com.memorizewords.dto.request;

import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.WordType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for starting a review session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartReviewSessionRequest {

    @NotNull(message = "Review mode is required")
    private ReviewMode mode;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    @Builder.Default
    private Integer limit = 20;

    private List<Long> includeWordListIds;

    private List<Long> excludeWordListIds;

    private List<WordType> includeWordTypes;

    private List<WordType> excludeWordTypes;

    private Boolean shuffleCards = true;

    private Boolean prioritizeDueCards = true;

    private Integer maxNewCards;

    private Boolean useUserPreferences = true;
}