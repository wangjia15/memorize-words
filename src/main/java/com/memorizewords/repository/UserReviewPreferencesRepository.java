package com.memorizewords.repository;

import com.memorizewords.entity.User;
import com.memorizewords.entity.UserReviewPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserReviewPreferences entity.
 */
@Repository
public interface UserReviewPreferencesRepository extends JpaRepository<UserReviewPreferences, Long> {

    Optional<UserReviewPreferences> findByUser(User user);

    boolean existsByUser(User user);

    @Query("SELECT p FROM UserReviewPreferences p WHERE p.user = :user")
    UserReviewPreferences getPreferencesByUser(@Param("user") User user);

    @Query("SELECT p.dailyReviewLimit FROM UserReviewPreferences p WHERE p.user = :user")
    Integer getDailyReviewLimitByUser(@Param("user") User user);

    @Query("SELECT p.dailyNewCardLimit FROM UserReviewPreferences p WHERE p.user = :user")
    Integer getDailyNewCardLimitByUser(@Param("user") User user);

    @Query("SELECT p.sessionGoal FROM UserReviewPreferences p WHERE p.user = :user")
    Integer getSessionGoalByUser(@Param("user") User user);

    @Query("SELECT p.enableNotifications FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getNotificationEnabledByUser(@Param("user") User user);

    @Query("SELECT p.useAdvancedAlgorithm FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getAdvancedAlgorithmEnabledByUser(@Param("user") User user);

    @Query("SELECT p.defaultReviewMode FROM UserReviewPreferences p WHERE p.user = :user")
    com.memorizewords.enums.ReviewMode getDefaultReviewModeByUser(@Param("user") User user);

    @Query("SELECT p.preferredReviewTime FROM UserReviewPreferences p WHERE p.user = :user")
    java.time.LocalTime getPreferredReviewTimeByUser(@Param("user") User user);

    @Query("SELECT p.vacationMode FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getVacationModeByUser(@Param("user") User user);

    @Query("SELECT p.weeklyGoal FROM UserReviewPreferences p WHERE p.user = :user")
    Integer getWeeklyGoalByUser(@Param("user") User user);

    @Query("SELECT p.monthlyGoal FROM UserReviewPreferences p WHERE p.user = :user")
    Integer getMonthlyGoalByUser(@Param("user") User user);

    @Query("SELECT p.enableAchievements FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getAchievementsEnabledByUser(@Param("user") User user);

    @Query("SELECT p.adaptiveDifficulty FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getAdaptiveDifficultyEnabledByUser(@Param("user") User user);

    @Query("SELECT p.enableStreakProtection FROM UserReviewPreferences p WHERE p.user = :user")
    Boolean getStreakProtectionEnabledByUser(@Param("user") User user);
}