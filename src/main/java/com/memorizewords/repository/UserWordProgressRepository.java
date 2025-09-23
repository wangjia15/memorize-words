package com.memorizewords.repository;

import com.memorizewords.entity.User;
import com.memorizewords.entity.UserWordProgress;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.LearningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserWordProgress entity.
 */
@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserAndWord(User user, Word word);

    List<UserWordProgress> findByUser(User user);

    List<UserWordProgress> findByUserAndStatus(User user, LearningStatus status);

    @Query("SELECT uwp FROM UserWordProgress uwp WHERE uwp.user = :user AND uwp.nextReviewAt <= :now")
    List<UserWordProgress> findWordsForReview(@Param("user") User user, @Param("now") java.time.LocalDateTime now);

    @Query("SELECT uwp FROM UserWordProgress uwp WHERE uwp.user = :user AND uwp.isFavorite = true")
    List<UserWordProgress> findFavoriteWords(@Param("user") User user);

    @Query("SELECT COUNT(uwp) FROM UserWordProgress uwp WHERE uwp.user = :user AND uwp.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") LearningStatus status);

    void deleteByUserAndWord(User user, Word word);
}