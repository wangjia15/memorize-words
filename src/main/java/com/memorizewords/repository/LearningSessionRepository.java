package com.memorizewords.repository;

import com.memorizewords.entity.LearningSession;
import com.memorizewords.entity.User;
import com.memorizewords.enums.LearningMode;
import com.memorizewords.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LearningSession entity.
 */
@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, Long>, JpaSpecificationExecutor<LearningSession> {

    /**
     * Find all sessions for a user.
     */
    Page<LearningSession> findByUser(User user, Pageable pageable);

    /**
     * Find sessions by user and status.
     */
    List<LearningSession> findByUserAndStatus(User user, SessionStatus status);

    /**
     * Find active session for a user.
     */
    Optional<LearningSession> findByUserAndStatus(User user, SessionStatus status);

    /**
     * Find the most recent session for a user.
     */
    Optional<LearningSession> findTopByUserOrderByStartTimeDesc(User user);

    /**
     * Find sessions by user and learning mode.
     */
    List<LearningSession> findByUserAndMode(User user, LearningMode mode);

    /**
     * Find incomplete sessions for a user (active or paused).
     */
    @Query("SELECT s FROM LearningSession s WHERE s.user = :user AND s.status IN ('ACTIVE', 'PAUSED')")
    List<LearningSession> findIncompleteSessionsByUser(@Param("user") User user);

    /**
     * Find sessions within a date range.
     */
    @Query("SELECT s FROM LearningSession s WHERE s.user = :user AND s.startTime BETWEEN :startDate AND :endDate")
    List<LearningSession> findByUserAndDateRange(@Param("user") User user,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count completed sessions for a user.
     */
    long countByUserAndStatus(User user, SessionStatus status);

    /**
     * Calculate total time spent in completed sessions.
     */
    @Query("SELECT COALESCE(SUM(s.duration), 0) FROM LearningSession s WHERE s.user = :user AND s.status = 'COMPLETED'")
    Long getTotalLearningTimeByUser(@Param("user") User user);

    /**
     * Get average accuracy for completed sessions.
     */
    @Query("SELECT COALESCE(AVG(s.accuracy), 0) FROM LearningSession s WHERE s.user = :user AND s.status = 'COMPLETED' AND s.accuracy IS NOT NULL")
    Double getAverageAccuracyByUser(@Param("user") User user);

    /**
     * Find sessions with low accuracy (below threshold).
     */
    @Query("SELECT s FROM LearningSession s WHERE s.user = :user AND s.accuracy < :threshold AND s.status = 'COMPLETED'")
    List<LearningSession> findLowAccuracySessions(@Param("user") User user, @Param("threshold") Double threshold);

    /**
     * Get session statistics for a user.
     */
    @Query("SELECT new map(" +
           "COUNT(s) as totalSessions, " +
           "COALESCE(SUM(s.duration), 0) as totalTime, " +
           "COALESCE(AVG(s.accuracy), 0) as averageAccuracy, " +
           "COALESCE(SUM(s.correctAnswers), 0) as totalCorrectAnswers, " +
           "COALESCE(SUM(s.totalWords), 0) as totalWordsStudied) " +
           "FROM LearningSession s WHERE s.user = :user AND s.status = 'COMPLETED'")
    Object getSessionStatsByUser(@Param("user") User user);

    /**
     * Find sessions by vocabulary list.
     */
    @Query("SELECT s FROM LearningSession s WHERE s.vocabularyList.id = :listId")
    List<LearningSession> findByVocabularyListId(@Param("listId") Long listId);

    /**
     * Get recent sessions for a user (last N sessions).
     */
    @Query("SELECT s FROM LearningSession s WHERE s.user = :user ORDER BY s.startTime DESC")
    Page<LearningSession> findRecentSessionsByUser(@Param("user") User user, Pageable pageable);

    /**
     * Count sessions by mode for a user.
     */
    @Query("SELECT s.mode, COUNT(s) FROM LearningSession s WHERE s.user = :user AND s.status = 'COMPLETED' GROUP BY s.mode")
    List<Object[]> countSessionsByModeForUser(@Param("user") User user);

    /**
     * Find sessions with answers containing specific word.
     */
    @Query("SELECT DISTINCT s FROM LearningSession s JOIN s.answers a WHERE a.word.id = :wordId AND s.user = :user")
    List<LearningSession> findSessionsWithWord(@Param("user") User user, @Param("wordId") Long wordId);

    /**
     * Check if user has any active or paused sessions.
     */
    @Query("SELECT COUNT(s) > 0 FROM LearningSession s WHERE s.user = :user AND s.status IN ('ACTIVE', 'PAUSED')")
    boolean hasActiveOrPausedSession(@Param("user") User user);

    /**
     * Get sessions that need to be automatically completed (stale sessions).
     */
    @Query("SELECT s FROM LearningSession s WHERE s.status IN ('ACTIVE', 'PAUSED') AND s.updatedAt < :cutoffTime")
    List<LearningSession> findStaleSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}