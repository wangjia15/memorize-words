package com.memorizewords.entity;

import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.WordType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing user preferences for review sessions.
 */
@Entity
@Table(name = "user_review_preferences")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserReviewPreferences extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "daily_review_limit")
    private Integer dailyReviewLimit = 50;

    @Column(name = "daily_new_card_limit")
    private Integer dailyNewCardLimit = 10;

    @Column(name = "session_goal")
    private Integer sessionGoal = 20;

    @Column(name = "preferred_review_time")
    private LocalTime preferredReviewTime = LocalTime.of(20, 0); // 8:00 PM

    @Column(name = "enable_notifications")
    private Boolean enableNotifications = true;

    @Column(name = "enable_email_reminders")
    private Boolean enableEmailReminders = true;

    @Column(name = "use_advanced_algorithm")
    private Boolean useAdvancedAlgorithm = true;

    @Column(name = "ease_factor_bonus", precision = 5, scale = 2)
    private BigDecimal easeFactorBonus = BigDecimal.ZERO;

    @Column(name = "interval_modifier", precision = 5, scale = 2)
    private BigDecimal intervalModifier = new BigDecimal("1.0");

    @Column(name = "minimum_interval_days")
    private Integer minimumIntervalDays = 1;

    @Column(name = "maximum_interval_days")
    private Integer maximumIntervalDays = 365;

    @Column(name = "default_review_mode")
    @Enumerated(EnumType.STRING)
    private ReviewMode defaultReviewMode = ReviewMode.DUE_CARDS;

    @Column(name = "auto_advance_cards")
    private Boolean autoAdvanceCards = true;

    @Column(name = "show_answer_delay")
    private Integer showAnswerDelay = 2000; // 2 seconds

    @Column(name = "enable_hints")
    private Boolean enableHints = true;

    @Column(name = "enable_pronunciation")
    private Boolean enablePronunciation = true;

    @Column(name = "enable_progress_animation")
    private Boolean enableProgressAnimation = true;

    @Column(name = "theme_preference")
    private String themePreference = "LIGHT";

    @Column(name = "language_preference")
    private String languagePreference = "en";

    @Column(name = "timezone")
    private String timezone = "UTC";

    @Column(name = "weekend_review_mode")
    private String weekendReviewMode = "NORMAL";

    @Column(name = "vacation_mode")
    private Boolean vacationMode = false;

    @Column(name = "vacation_start_date")
    private java.time.LocalDate vacationStartDate;

    @Column(name = "vacation_end_date")
    private java.time.LocalDate vacationEndDate;

    @ElementCollection
    @CollectionTable(name = "user_preferred_review_modes",
                    joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "review_mode")
    @Enumerated(EnumType.STRING)
    private Set<ReviewMode> preferredModes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_included_card_types",
                    joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    private Set<WordType> includedCardTypes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_excluded_card_types",
                    joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    private Set<WordType> excludedCardTypes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_notification_times",
                    joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "notification_time")
    private List<LocalTime> notificationTimes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_review_goals",
                    joinColumns = @JoinColumn(name = "preferences_id"))
    private List<ReviewGoal> reviewGoals = new ArrayList<>();

    @Column(name = "adaptive_difficulty")
    private Boolean adaptiveDifficulty = true;

    @Column(name = "focus_mode_duration")
    private Integer focusModeDuration = 25; // 25 minutes (Pomodoro)

    @Column(name = "break_duration")
    private Integer breakDuration = 5; // 5 minutes

    @Column(name = "enable_streak_protection")
    private Boolean enableStreakProtection = true;

    @Column(name = "streak_protection_time")
    private Integer streakProtectionTime = 2; // 2 hours

    @Column(name = "enable_learning_insights")
    private Boolean enableLearningInsights = true;

    @Column(name = "weekly_goal")
    private Integer weeklyGoal = 350; // 50 cards per day * 7 days

    @Column(name = "monthly_goal")
    private Integer monthlyGoal = 1500; // ~50 cards per day * 30 days

    @Column(name = "enable_achievements")
    private Boolean enableAchievements = true;

    @Column(name = "show_detailed_statistics")
    private Boolean showDetailedStatistics = true;

    @Column(name = "export_format_preference")
    private String exportFormatPreference = "JSON";

    @Column(name = "backup_frequency")
    private String backupFrequency = "WEEKLY";

    @PrePersist
    public void prePersist() {
        if (preferredModes.isEmpty()) {
            preferredModes.add(ReviewMode.DUE_CARDS);
            preferredModes.add(ReviewMode.NEW_CARDS);
        }

        if (notificationTimes.isEmpty()) {
            notificationTimes.add(LocalTime.of(9, 0));  // 9:00 AM
            notificationTimes.add(LocalTime.of(20, 0)); // 8:00 PM
        }

        if (reviewGoals.isEmpty()) {
            reviewGoals.add(new ReviewGoal("DAILY", dailyReviewLimit));
            reviewGoals.add(new ReviewGoal("WEEKLY", weeklyGoal));
        }

        if (includedCardTypes.isEmpty()) {
            // Include all word types by default
            for (WordType type : WordType.values()) {
                includedCardTypes.add(type);
            }
        }
    }

    public void addPreferredMode(ReviewMode mode) {
        preferredModes.add(mode);
    }

    public void removePreferredMode(ReviewMode mode) {
        preferredModes.remove(mode);
    }

    public void addIncludedCardType(WordType type) {
        includedCardTypes.add(type);
        excludedCardTypes.remove(type);
    }

    public void addExcludedCardType(WordType type) {
        excludedCardTypes.add(type);
        includedCardTypes.remove(type);
    }

    public boolean isCardTypeIncluded(WordType type) {
        return includedCardTypes.contains(type) && !excludedCardTypes.contains(type);
    }

    public void addNotificationTime(LocalTime time) {
        if (!notificationTimes.contains(time)) {
            notificationTimes.add(time);
        }
    }

    public void removeNotificationTime(LocalTime time) {
        notificationTimes.remove(time);
    }

    public void setReviewGoal(String period, Integer target) {
        reviewGoals.removeIf(goal -> goal.getPeriod().equals(period));
        reviewGoals.add(new ReviewGoal(period, target));
    }

    public Integer getReviewGoal(String period) {
        return reviewGoals.stream()
            .filter(goal -> goal.getPeriod().equals(period))
            .findFirst()
            .map(ReviewGoal::getTarget)
            .orElse(0);
    }

    public boolean isInVacationMode() {
        if (!vacationMode) return false;

        java.time.LocalDate today = java.time.LocalDate.now();
        if (vacationStartDate != null && vacationEndDate != null) {
            return !today.isBefore(vacationStartDate) && !today.isAfter(vacationEndDate);
        }
        return vacationMode;
    }

    public boolean shouldShowNotification(LocalTime currentTime) {
        if (!enableNotifications || isInVacationMode()) {
            return false;
        }

        // Check if current time is within any notification time window (±30 minutes)
        return notificationTimes.stream()
            .anyMatch(notificationTime -> {
                LocalTime windowStart = notificationTime.minusMinutes(30);
                LocalTime windowEnd = notificationTime.plusMinutes(30);
                return !currentTime.isBefore(windowStart) && !currentTime.isAfter(windowEnd);
            });
    }

    public boolean isWeekendReviewRestricted() {
        return "RESTRICTED".equals(weekendReviewMode) ||
               "MINIMAL".equals(weekendReviewMode);
    }

    public int getWeekendReviewLimit() {
        if ("RESTRICTED".equals(weekendReviewMode)) {
            return dailyReviewLimit / 2;
        } else if ("MINIMAL".equals(weekendReviewMode)) {
            return Math.max(10, dailyReviewLimit / 3);
        }
        return dailyReviewLimit;
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewGoal {
        @Column(name = "period")
        private String period; // DAILY, WEEKLY, MONTHLY

        @Column(name = "target")
        private Integer target;
    }
}