package com.memorizewords.dto.response;

import com.memorizewords.enums.ReviewMode;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response DTO for review mode information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModeInfoDTO {

    private ReviewMode mode;

    private String name;

    private String description;

    private Integer availableCards;

    private Boolean isRecommended;

    private String icon;

    private String color;

    private Integer estimatedDurationMinutes;

    private Double difficultyLevel;

    private Boolean isNew;

    private Boolean isPopular;
}