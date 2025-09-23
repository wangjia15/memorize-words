package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for review insights.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInsightsDTO {

    private String learningVelocity;

    private String retentionTrend;

    private List<LocalTime> optimalStudyTimes;

    private String difficultyDistribution;

    private List<String> recommendations;

    private String performanceTrend;

    private String bestPerformanceTime;

    private String worstPerformanceTime;

    private Integer averageDailyReviews;

    private Double averageAccuracy;

    private Integer currentStreak;

    private Integer longestStreak;

    private String overallPerformance; // EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT

    private List<String> achievements;

    private List<String> areasForImprovement;

    private String nextMilestone;

    private Integer daysToMilestone;
}