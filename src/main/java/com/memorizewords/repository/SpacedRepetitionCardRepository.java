package com.memorizewords.repository;

import com.memorizewords.entity.SpacedRepetitionCard;
import com.memorizewords.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SpacedRepetitionCard entity.
 */
@Repository
public interface SpacedRepetitionCardRepository extends JpaRepository<SpacedRepetitionCard, Long> {

    List<SpacedRepetitionCard> findByUser(User user);

    List<SpacedRepetitionCard> findByUserAndIsActive(User user, Boolean isActive);

    List<SpacedRepetitionCard> findByUserAndDueDateBefore(User user, LocalDateTime dueDate);

    List<SpacedRepetitionCard> findByUserAndDueDateBeforeOrderByDueDateAsc(User user, LocalDateTime dueDate);

    List<SpacedRepetitionCard> findByUserAndDueDateBetween(User user, LocalDateTime start, LocalDateTime end);

    List<SpacedRepetitionCard> findByUserAndIsSuspended(User user, Boolean isSuspended);

    List<SpacedRepetitionCard> findByUserAndWordId(User user, Long wordId);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true AND c.isSuspended = false ORDER BY c.dueDate ASC")
    List<SpacedRepetitionCard> findActiveCardsForUser(@Param("user") User user);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.dueDate <= :dueDate AND c.isActive = true AND c.isSuspended = false")
    List<SpacedRepetitionCard> findDueCardsForUser(@Param("user") User user, @Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.dueDate <= :dueDate AND c.isActive = true AND c.isSuspended = false ORDER BY c.dueDate ASC")
    Page<SpacedRepetitionCard> findDueCardsForUser(@Param("user") User user, @Param("dueDate") LocalDateTime dueDate, Pageable pageable);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND (c.difficultyRating IS NOT NULL AND c.difficultyRating > 0.5) AND c.isActive = true AND c.isSuspended = false ORDER BY c.difficultyRating DESC")
    Page<SpacedRepetitionCard> findDifficultCardsForUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.totalReviews = 0 AND c.isActive = true AND c.isSuspended = false")
    List<SpacedRepetitionCard> findNewCardsForUser(@Param("user") User user);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.totalReviews > 0 AND c.isActive = true AND c.isSuspended = false ORDER BY FUNCTION('RANDOM')")
    Page<SpacedRepetitionCard> findRandomCardsForUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(c) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.dueDate <= :dueDate AND c.isActive = true AND c.isSuspended = false")
    Long countDueCardsForUser(@Param("user") User user, @Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT COUNT(c) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.totalReviews = 0 AND c.isActive = true AND c.isSuspended = false")
    Long countNewCardsForUser(@Param("user") User user);

    @Query("SELECT COUNT(c) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true AND c.isSuspended = false")
    Long countActiveCardsForUser(@Param("user") User user);

    @Query("SELECT AVG(c.performanceIndex) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.performanceIndex IS NOT NULL AND c.isActive = true")
    Double getAveragePerformanceIndex(@Param("user") User user);

    @Query("SELECT AVG(c.retentionRate) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.retentionRate IS NOT NULL AND c.isActive = true")
    Double getAverageRetentionRate(@Param("user") User user);

    @Query("SELECT SUM(c.totalStudyTime) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true")
    Long getTotalStudyTime(@Param("user") User user);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.lastReviewed >= :startDate AND c.lastReviewed <= :endDate")
    List<SpacedRepetitionCard> findByUserAndLastReviewedBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.consecutiveIncorrect >= :threshold AND c.isActive = true")
    List<SpacedRepetitionCard> findCardsWithConsecutiveIncorrect(@Param("user") User user, @Param("threshold") Integer threshold);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.consecutiveCorrect >= :threshold AND c.isActive = true")
    List<SpacedRepetitionCard> findCardsWithConsecutiveCorrect(@Param("user") User user, @Param("threshold") Integer threshold);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isSuspended = true")
    List<SpacedRepetitionCard> findSuspendedCards(@Param("user") User user);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.easeFactor < :threshold AND c.isActive = true")
    List<SpacedRepetitionCard> findLowEaseFactorCards(@Param("user") User user, @Param("threshold") Double threshold);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.intervalDays > :threshold AND c.isActive = true")
    List<SpacedRepetitionCard> findLongIntervalCards(@Param("user") User user, @Param("threshold") Integer threshold);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.lastReviewOutcome = 'AGAIN' AND c.isActive = true")
    List<SpacedRepetitionCard> findRecentlyFailedCards(@Param("user") User user);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.totalReviews >= :minReviews AND c.isActive = true ORDER BY c.performanceIndex ASC")
    List<SpacedRepetitionCard> findLowPerformingCards(@Param("user") User user, @Param("minReviews") Integer minReviews);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.performanceIndex >= :threshold AND c.isActive = true ORDER BY c.performanceIndex DESC")
    List<SpacedRepetitionCard> findHighPerformingCards(@Param("user") User user, @Param("threshold") Double threshold);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true AND c.isSuspended = false ORDER BY c.dueDate ASC")
    Page<SpacedRepetitionCard> findActiveCardsByUserPaged(@Param("user") User user, Pageable pageable);

    @Query("SELECT c FROM SpacedRepetitionCard c WHERE c.user = :user AND c.word.id = :wordId")
    SpacedRepetitionCard findByUserAndWordId(@Param("user") User user, @Param("wordId") Long wordId);

    @Query("SELECT COUNT(c) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true AND c.isSuspended = false AND c.dueDate <= :dueDate")
    Long countDueCardsForUserWithSuspended(@Param("user") User user, @Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT AVG(c.averageResponseTime) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.averageResponseTime IS NOT NULL AND c.isActive = true")
    Double getAverageResponseTime(@Param("user") User user);

    @Query("SELECT SUM(c.totalReviews) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true")
    Long getTotalReviews(@Param("user") User user);

    @Query("SELECT SUM(c.correctReviews) FROM SpacedRepetitionCard c WHERE c.user = :user AND c.isActive = true")
    Long getTotalCorrectReviews(@Param("user") User user);
}