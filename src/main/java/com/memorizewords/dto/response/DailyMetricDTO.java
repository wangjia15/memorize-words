package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for daily review metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetricDTO {

    private LocalDate date;

    private Integer reviewsCompleted;

    private Integer correctAnswers;

    private Double accuracy;

    private Integer studyTimeMinutes;

    private Integer newCardsLearned;

    private Double sessionScore;

    private Integer streakDay;

    private Boolean isActiveDay;

    private Double efficiencyScore;

    private Integer averageResponseTime;

    private String performanceRating; // POOR, FAIR, GOOD, EXCELLENT
}