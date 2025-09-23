package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for learning velocity metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningVelocityDTO {

    private BigDecimal cardsPerHour;

    private BigDecimal cardsPerDay;

    private BigDecimal retentionImprovementRate;

    private BigDecimal accuracyTrend;

    private BigDecimal efficiencyScore;

    private String velocityCategory; // SLOW, MODERATE, FAST, VERY_FAST

    private BigDecimal predictedMonthlyProgress;

    private BigDecimal timeToMasteryDays;

    private BigDecimal consistencyScore;

    private Integer improvementStreak;
}