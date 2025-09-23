package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response DTO for review goal information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewGoalDTO {

    private String period; // DAILY, WEEKLY, MONTHLY

    private Integer target;

    private Integer current;

    private Double progressPercentage;

    private Boolean isAchieved;

    private Integer remaining;

    private String status; // ON_TRACK, AHEAD, BEHIND
}