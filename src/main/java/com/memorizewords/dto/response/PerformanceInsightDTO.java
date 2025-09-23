package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for performance insights.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceInsightDTO {

    private List<String> recommendations;

    private List<LocalTime> optimalStudyTimes;

    private String difficultyLevel; // TOO_EASY, JUST_RIGHT, TOO_HARD

    private List<String> weakAreas;

    private List<String> strongAreas;

    private String studyConsistency; // VERY_CONSISTENT, CONSISTENT, INCONSISTENT, VERY_INCONSISTENT

    private String retentionStatus; // EXCELLENT, GOOD, AVERAGE, POOR

    private Integer recommendedDailyReviews;

    private Integer recommendedNewCardsPerDay;

    private String focusRecommendation;

    private Double improvementPotential;
}