package com.memorizewords.dto.response;

import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.WordType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for user review preferences information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReviewPreferencesDTO {

    private Long id;

    private Long userId;

    private Integer dailyReviewLimit;

    private Integer dailyNewCardLimit;

    private Integer sessionGoal;

    private LocalTime preferredReviewTime;

    private Boolean enableNotifications;

    private Boolean enableEmailReminders;

    private Boolean useAdvancedAlgorithm;

    private BigDecimal easeFactorBonus;

    private BigDecimal intervalModifier;

    private Integer minimumIntervalDays;

    private Integer maximumIntervalDays;

    private ReviewMode defaultReviewMode;

    private Boolean autoAdvanceCards;

    private Integer showAnswerDelay;

    private Boolean enableHints;

    private Boolean enablePronunciation;

    private Boolean enableProgressAnimation;

    private String themePreference;

    private String languagePreference;

    private String timezone;

    private String weekendReviewMode;

    private Boolean vacationMode;

    private java.time.LocalDate vacationStartDate;

    private java.time.LocalDate vacationEndDate;

    private Set<ReviewMode> preferredModes;

    private Set<WordType> includedCardTypes;

    private Set<WordType> excludedCardTypes;

    private List<LocalTime> notificationTimes;

    private List<ReviewGoalDTO> reviewGoals;

    private Boolean adaptiveDifficulty;

    private Integer focusModeDuration;

    private Integer breakDuration;

    private Boolean enableStreakProtection;

    private Integer streakProtectionTime;

    private Boolean enableLearningInsights;

    private Integer weeklyGoal;

    private Integer monthlyGoal;

    private Boolean enableAchievements;

    private Boolean showDetailedStatistics;

    private String exportFormatPreference;

    private String backupFrequency;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}