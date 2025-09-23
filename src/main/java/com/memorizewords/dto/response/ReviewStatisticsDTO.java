package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for review statistics information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDTO {

    private Long userId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Integer totalReviews;

    private Integer correctReviews;

    private Double averageAccuracy;

    private Duration totalStudyTime;

    private Integer streakDays;

    private Integer longestStreak;

    private LearningVelocityDTO learningVelocity;

    private Double retentionRate;

    private List<DailyMetricDTO> dailyMetrics;

    private List<AchievementDTO> achievements;

    private Integer totalSessions;

    private Double averageSessionScore;

    private Integer bestSessionAccuracy;

    private Integer newCardsLearned;

    private Integer difficultCardsMastered;

    private Integer cardsPerMinuteAverage;

    private Double efficiencyScore;

    private ReviewTrendDTO reviewTrend;

    private PerformanceInsightDTO performanceInsight;
}