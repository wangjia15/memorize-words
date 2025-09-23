package com.memorizewords.dto.response;

import com.memorizewords.enums.LearningMode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for session statistics.
 */
@Data
public class SessionStatsDto {

    private Long totalSessions;

    private Long totalCompletedSessions;

    private Long totalActiveSessions;

    private Long totalPausedSessions;

    private Long totalTime; // Total time in seconds

    private Double averageAccuracy;

    private Long totalCorrectAnswers;

    private Long totalWordsStudied;

    private Double averageSessionDuration;

    private Map<LearningMode, Long> sessionsByMode;

    private Long currentStreak;

    private Long longestStreak;

    private LocalDateTime lastSessionDate;

    private LocalDateTime firstSessionDate;

    private Integer totalDaysActive;

    // Performance metrics
    private Double improvementRate;

    private String performanceTrend; // IMPROVING, STABLE, DECLINING

    private Integer weakWords; // Words with low accuracy

    private Integer masteredWords; // Words with high accuracy
}