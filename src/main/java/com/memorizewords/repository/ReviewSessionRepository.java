package com.memorizewords.repository;

import com.memorizewords.entity.ReviewSession;
import com.memorizewords.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ReviewSession entity.
 */
@Repository
public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {

    List<ReviewSession> findByUser(User user);

    List<ReviewSession> findByUserAndIsCompleted(User user, Boolean isCompleted);

    List<ReviewSession> findByUserAndMode(User user, com.memorizewords.enums.ReviewMode mode);

    Page<ReviewSession> findByUserOrderByStartTimeDesc(User user, Pageable pageable);

    Optional<ReviewSession> findByUserAndIsCompletedFalse(User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = false ORDER BY s.startTime DESC")
    List<ReviewSession> findActiveSessionsByUser(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true ORDER BY s.startTime DESC")
    Page<ReviewSession> findCompletedSessionsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.startTime >= :startDate AND s.startTime <= :endDate")
    List<ReviewSession> findByUserAndStartTimeBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true")
    Long countCompletedSessionsByUser(@Param("user") User user);

    @Query("SELECT AVG(s.sessionAccuracy) FROM ReviewSession s WHERE s.user = :user AND s.sessionAccuracy IS NOT NULL AND s.isCompleted = true")
    Double getAverageSessionAccuracy(@Param("user") User user);

    @Query("SELECT SUM(s.sessionDuration) FROM ReviewSession s WHERE s.user = :user AND s.sessionDuration IS NOT NULL AND s.isCompleted = true")
    Long getTotalStudyDuration(@Param("user") User user);

    @Query("SELECT AVG(s.totalSessionScore) FROM ReviewSession s WHERE s.user = :user AND s.totalSessionScore IS NOT NULL AND s.isCompleted = true")
    Double getAverageSessionScore(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true ORDER BY s.startTime DESC")
    List<ReviewSession> findCompletedSessionsByUser(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.startTime >= :date ORDER BY s.startTime ASC")
    List<ReviewSession> findSessionsByUserAndDate(@Param("user") User user, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Long countCompletedSessionsByUserInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(s.completedCards) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Integer getTotalCompletedCardsInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(s.correctAnswers) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Integer getTotalCorrectAnswersInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.mode = :mode AND s.isCompleted = true ORDER BY s.startTime DESC")
    List<ReviewSession> findCompletedSessionsByUserAndMode(@Param("user") User user, @Param("mode") com.memorizewords.enums.ReviewMode mode);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :date ORDER BY s.totalSessionScore DESC")
    List<ReviewSession> findBestSessionsByUserSinceDate(@Param("user") User user, @Param("date") LocalDateTime date);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true ORDER BY s.sessionDuration DESC")
    List<ReviewSession> findLongestSessionsByUser(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true ORDER BY s.cardsPerMinute DESC")
    List<ReviewSession> findFastestSessionsByUser(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :startDate AND s.startTime <= :endDate ORDER BY s.startTime ASC")
    List<ReviewSession> findCompletedSessionsInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(s.efficiencyScore) FROM ReviewSession s WHERE s.user = :user AND s.efficiencyScore IS NOT NULL AND s.isCompleted = true")
    Double getAverageEfficiencyScore(@Param("user") User user);

    @Query("SELECT SUM(s.newCardsLearned) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true")
    Integer getTotalNewCardsLearned(@Param("user") User user);

    @Query("SELECT SUM(s.difficultCardsMastered) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true")
    Integer getTotalDifficultCardsMastered(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.totalSessionScore >= :threshold ORDER BY s.startTime DESC")
    List<ReviewSession> findHighScoringSessions(@Param("user") User user, @Param("threshold") Double threshold);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = false AND s.startTime < :timeoutDate")
    List<ReviewSession> findExpiredSessions(@Param("user") User user, @Param("timeoutDate") LocalDateTime timeoutDate);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :date ORDER BY s.learningVelocity DESC")
    List<ReviewSession> findSessionsWithHighestLearningVelocity(@Param("user") User user, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(DISTINCT DATE(s.startTime)) FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.startTime >= :startDate AND s.startTime <= :endDate")
    Long countActiveDaysInPeriod(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true ORDER BY s.startTime DESC LIMIT 1")
    Optional<ReviewSession> findLastCompletedSessionByUser(@Param("user") User user);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.sessionAccuracy >= :threshold ORDER BY s.startTime DESC")
    List<ReviewSession> findHighAccuracySessions(@Param("user") User user, @Param("threshold") Double threshold);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.isCompleted = true AND s.sessionDuration >= :threshold ORDER BY s.startTime DESC")
    List<ReviewSession> findLongDurationSessions(@Param("user") User user, @Param("threshold") Long threshold);

    @Query("SELECT s FROM ReviewSession s WHERE s.user = :user AND s.mode = :mode ORDER BY s.startTime DESC")
    Page<ReviewSession> findByUserAndModeOrderByStartTimeDesc(@Param("user") User user, @Param("mode") com.memorizewords.enums.ReviewMode mode, Pageable pageable);
}