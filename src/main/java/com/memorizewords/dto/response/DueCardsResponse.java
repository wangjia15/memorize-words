package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Response DTO for due cards information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DueCardsResponse {

    private List<SpacedRepetitionCardDTO> dueCards;

    private Long totalDue;

    private Long totalNew;

    private Long totalDifficult;

    private Long totalActive;

    private Integer recommendedLimit;

    private Integer dailyLimit;

    private Boolean exceedsDailyLimit;

    private Integer newCardsToday;

    private Integer reviewsToday;

    private List<String> availableReviewModes;

    private String nextReviewSuggestion;

}