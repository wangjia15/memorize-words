package com.memorizewords.service;

import com.memorizewords.dto.request.UpdatePreferencesRequest;
import com.memorizewords.dto.response.UserReviewPreferencesDTO;
import com.memorizewords.entity.User;
import com.memorizewords.entity.UserReviewPreferences;
import com.memorizewords.enums.ReviewMode;
import com.memorizewords.enums.WordType;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.UserRepository;
import com.memorizewords.repository.UserReviewPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user review preferences.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserReviewPreferencesService {

    private final UserReviewPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_DAILY_REVIEW_LIMIT = 50;
    private static final int DEFAULT_DAILY_NEW_CARD_LIMIT = 10;
    private static final int DEFAULT_SESSION_GOAL = 20;
    private static final LocalTime DEFAULT_PREFERRED_REVIEW_TIME = LocalTime.of(20, 0);

    public UserReviewPreferences getPreferences(User user) {
        return preferencesRepository.findByUser(user)
            .orElseGet(() -> createDefaultPreferences(user));
    }

    public UserReviewPreferences getPreferences(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return getPreferences(user);
    }

    public UserReviewPreferences updatePreferences(User user, UpdatePreferencesRequest request) {
        log.info("Updating preferences for user {}", user.getId());

        UserReviewPreferences preferences = getPreferences(user);

        // Update basic preferences
        if (request.getDailyReviewLimit() != null) {
            preferences.setDailyReviewLimit(request.getDailyReviewLimit());
        }

        if (request.getDailyNewCardLimit() != null) {
            preferences.setDailyNewCardLimit(request.getDailyNewCardLimit());
        }

        if (request.getSessionGoal() != null) {
            preferences.setSessionGoal(request.getSessionGoal());
        }

        if (request.getPreferredReviewTime() != null) {
            preferences.setPreferredReviewTime(request.getPreferredReviewTime());
        }

        if (request.getEnableNotifications() != null) {
            preferences.setEnableNotifications(request.getEnableNotifications());
        }

        if (request.getUseAdvancedAlgorithm() != null) {
            preferences.setUseAdvancedAlgorithm(request.getUseAdvancedAlgorithm());
        }

        if (request.getEaseFactorBonus() != null) {
            preferences.setEaseFactorBonus(request.getEaseFactorBonus());
        }

        if (request.getIntervalModifier() != null) {
            preferences.setIntervalModifier(request.getIntervalModifier());
        }

        if (request.getDefaultReviewMode() != null) {
            preferences.setDefaultReviewMode(request.getDefaultReviewMode());
        }

        if (request.getPreferredModes() != null) {
            preferences.getPreferredModes().clear();
            preferences.getPreferredModes().addAll(request.getPreferredModes());
        }

        if (request.getIncludedCardTypes() != null) {
            preferences.getIncludedCardTypes().clear();
            preferences.getIncludedCardTypes().addAll(request.getIncludedCardTypes());
        }

        if (request.getExcludedCardTypes() != null) {
            preferences.getExcludedCardTypes().clear();
            preferences.getExcludedCardTypes().addAll(request.getExcludedCardTypes());
        }

        if (request.getNotificationTimes() != null) {
            preferences.getNotificationTimes().clear();
            preferences.getNotificationTimes().addAll(request.getNotificationTimes());
        }

        if (request.getAdaptiveDifficulty() != null) {
            preferences.setAdaptiveDifficulty(request.getAdaptiveDifficulty());
        }

        if (request.getEnableStreakProtection() != null) {
            preferences.setEnableStreakProtection(request.getEnableStreakProtection());
        }

        if (request.getEnableLearningInsights() != null) {
            preferences.setEnableLearningInsights(request.getEnableLearningInsights());
        }

        if (request.getEnableAchievements() != null) {
            preferences.setEnableAchievements(request.getEnableAchievements());
        }

        if (request.getShowDetailedStatistics() != null) {
            preferences.setShowDetailedStatistics(request.getShowDetailedStatistics());
        }

        if (request.getThemePreference() != null) {
            preferences.setThemePreference(request.getThemePreference());
        }

        if (request.getLanguagePreference() != null) {
            preferences.setLanguagePreference(request.getLanguagePreference());
        }

        UserReviewPreferences saved = preferencesRepository.save(preferences);
        log.info("Successfully updated preferences for user {}", user.getId());

        return saved;
    }

    public UserReviewPreferences updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return updatePreferences(user, request);
    }

    public UserReviewPreferences createDefaultPreferences(User user) {
        log.info("Creating default preferences for user {}", user.getId());

        UserReviewPreferences preferences = new UserReviewPreferences();
        preferences.setUser(user);
        preferences.setDailyReviewLimit(DEFAULT_DAILY_REVIEW_LIMIT);
        preferences.setDailyNewCardLimit(DEFAULT_DAILY_NEW_CARD_LIMIT);
        preferences.setSessionGoal(DEFAULT_SESSION_GOAL);
        preferences.setPreferredReviewTime(DEFAULT_PREFERRED_REVIEW_TIME);
        preferences.setEnableNotifications(true);
        preferences.setEnableEmailReminders(true);
        preferences.setUseAdvancedAlgorithm(true);
        preferences.setDefaultReviewMode(ReviewMode.DUE_CARDS);
        preferences.setAutoAdvanceCards(true);
        preferences.setShowAnswerDelay(2000);
        preferences.setEnableHints(true);
        preferences.setEnablePronunciation(true);
        preferences.setEnableProgressAnimation(true);
        preferences.setThemePreference("LIGHT");
        preferences.setLanguagePreference("en");
        preferences.setTimezone("UTC");
        preferences.setWeekendReviewMode("NORMAL");
        preferences.setVacationMode(false);
        preferences.setAdaptiveDifficulty(true);
        preferences.setFocusModeDuration(25);
        preferences.setBreakDuration(5);
        preferences.setEnableStreakProtection(true);
        preferences.setStreakProtectionTime(2);
        preferences.setEnableLearningInsights(true);
        preferences.setWeeklyGoal(350);
        preferences.setMonthlyGoal(1500);
        preferences.setEnableAchievements(true);
        preferences.setShowDetailedStatistics(true);
        preferences.setExportFormatPreference("JSON");
        preferences.setBackupFrequency("WEEKLY");

        // Add default review modes
        preferences.addPreferredMode(ReviewMode.DUE_CARDS);
        preferences.addPreferredMode(ReviewMode.NEW_CARDS);

        // Add default notification times
        preferences.addNotificationTime(LocalTime.of(9, 0));
        preferences.addNotificationTime(LocalTime.of(20, 0));

        // Add default review goals
        preferences.setReviewGoal("DAILY", DEFAULT_DAILY_REVIEW_LIMIT);
        preferences.setReviewGoal("WEEKLY", 350);

        // Include all word types by default
        for (WordType type : WordType.values()) {
            preferences.addIncludedCardType(type);
        }

        UserReviewPreferences saved = preferencesRepository.save(preferences);
        log.info("Successfully created default preferences for user {}", user.getId());

        return saved;
    }

    public void resetToDefaults(User user) {
        log.info("Resetting preferences to defaults for user {}", user.getId());

        UserReviewPreferences preferences = getPreferences(user);
        preferencesRepository.delete(preferences);

        createDefaultPreferences(user);
    }

    public void resetToDefaults(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        resetToDefaults(user);
    }

    public boolean shouldShowNotification(User user, LocalTime currentTime) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.shouldShowNotification(currentTime);
    }

    public int getEffectiveDailyLimit(User user) {
        UserReviewPreferences preferences = getPreferences(user);

        if (preferences.isInVacationMode()) {
            return 0;
        }

        // Check if it's weekend and apply weekend restrictions
        if (preferences.isWeekendReviewRestricted()) {
            return preferences.getWeekendReviewLimit();
        }

        return preferences.getDailyReviewLimit();
    }

    public int getEffectiveNewCardLimit(User user) {
        UserReviewPreferences preferences = getPreferences(user);

        if (preferences.isInVacationMode()) {
            return 0;
        }

        return preferences.getDailyNewCardLimit();
    }

    public ReviewMode getDefaultReviewMode(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getDefaultReviewMode();
    }

    public List<ReviewMode> getPreferredReviewModes(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getPreferredModes().stream()
            .collect(Collectors.toList());
    }

    public boolean isCardTypeIncluded(User user, WordType wordType) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.isCardTypeIncluded(wordType);
    }

    public void addPreferredReviewMode(User user, ReviewMode mode) {
        UserReviewPreferences preferences = getPreferences(user);
        preferences.addPreferredMode(mode);
        preferencesRepository.save(preferences);
    }

    public void removePreferredReviewMode(User user, ReviewMode mode) {
        UserReviewPreferences preferences = getPreferences(user);
        preferences.removePreferredMode(mode);
        preferencesRepository.save(preferences);
    }

    public void setNotificationTime(User user, LocalTime time) {
        UserReviewPreferences preferences = getPreferences(user);
        preferences.addNotificationTime(time);
        preferencesRepository.save(preferences);
    }

    public void removeNotificationTime(User user, LocalTime time) {
        UserReviewPreferences preferences = getPreferences(user);
        preferences.removeNotificationTime(time);
        preferencesRepository.save(preferences);
    }

    public void setVacationMode(User user, boolean vacationMode) {
        setVacationMode(user, vacationMode, null, null);
    }

    public void setVacationMode(User user, boolean vacationMode,
                               java.time.LocalDate startDate, java.time.LocalDate endDate) {
        UserReviewPreferences preferences = getPreferences(user);
        preferences.setVacationMode(vacationMode);
        preferences.setVacationStartDate(startDate);
        preferences.setVacationEndDate(endDate);
        preferencesRepository.save(preferences);
    }

    public Set<WordType> getIncludedCardTypes(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getIncludedCardTypes();
    }

    public Set<WordType> getExcludedCardTypes(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getExcludedCardTypes();
    }

    public List<LocalTime> getNotificationTimes(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getNotificationTimes();
    }

    public boolean isAdaptiveDifficultyEnabled(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getAdaptiveDifficulty() != null && preferences.getAdaptiveDifficulty();
    }

    public boolean isStreakProtectionEnabled(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getEnableStreakProtection() != null && preferences.getEnableStreakProtection();
    }

    public boolean areLearningInsightsEnabled(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getEnableLearningInsights() != null && preferences.getEnableLearningInsights();
    }

    public boolean areAchievementsEnabled(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getEnableAchievements() != null && preferences.getEnableAchievements();
    }

    public boolean isAdvancedAlgorithmEnabled(User user) {
        UserReviewPreferences preferences = getPreferences(user);
        return preferences.getUseAdvancedAlgorithm() != null && preferences.getUseAdvancedAlgorithm();
    }
}