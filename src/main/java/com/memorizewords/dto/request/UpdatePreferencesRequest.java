package com.memorizewords.dto.request;

import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.WordType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Request DTO for updating user review preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {

    @Min(value = 1, message = "Daily review limit must be at least 1")
    @Max(value = 500, message = "Daily review limit cannot exceed 500")
    private Integer dailyReviewLimit;

    @Min(value = 0, message = "Daily new card limit cannot be negative")
    @Max(value = 50, message = "Daily new card limit cannot exceed 50")
    private Integer dailyNewCardLimit;

    @Min(value = 1, message = "Session goal must be at least 1")
    @Max(value = 100, message = "Session goal cannot exceed 100")
    private Integer sessionGoal;

    private LocalTime preferredReviewTime;

    private Boolean enableNotifications;

    private Boolean useAdvancedAlgorithm;

    @DecimalMin(value = "0.1", message = "Ease factor bonus must be at least 0.1")
    @DecimalMax(value = "2.0", message = "Ease factor bonus cannot exceed 2.0")
    private BigDecimal easeFactorBonus;

    @DecimalMin(value = "0.1", message = "Interval modifier must be at least 0.1")
    @DecimalMax(value = "3.0", message = "Interval modifier cannot exceed 3.0")
    private BigDecimal intervalModifier;

    private ReviewMode defaultReviewMode;

    private Set<ReviewMode> preferredModes;

    private Set<WordType> includedCardTypes;

    private Set<WordType> excludedCardTypes;

    private List<LocalTime> notificationTimes;

    private Boolean adaptiveDifficulty;

    private Boolean enableStreakProtection;

    private Boolean enableLearningInsights;

    private Boolean enableAchievements;

    private Boolean showDetailedStatistics;

    private String themePreference;

    private String languagePreference;
}