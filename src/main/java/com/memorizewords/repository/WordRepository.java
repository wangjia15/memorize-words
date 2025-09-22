package com.memorizewords.repository;

import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enum.DifficultyLevel;
import com.memorizewords.enum.WordCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for Word entity.
 */
@Repository
public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {

    List<Word> findByWordContainingIgnoreCase(String word);

    List<Word> findByCreatedByAndIsPublicTrue(User user);

    List<Word> findByLanguageAndDifficulty(String language, DifficultyLevel difficulty);

    @Query("SELECT w FROM Word w WHERE w.word = :word AND w.language = :language")
    Optional<Word> findByWordAndLanguage(@Param("word") String word, @Param("language") String language);

    @Query("SELECT w FROM Word w JOIN w.categories c WHERE c IN :categories")
    List<Word> findByCategories(@Param("categories") Set<WordCategory> categories);

    @Query("SELECT w FROM Word w WHERE w.createdBy = :user OR w.isPublic = true")
    Page<Word> findAccessibleWords(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT w FROM Word w LEFT JOIN w.tags t WHERE w.word LIKE %:searchTerm% OR w.definition LIKE %:searchTerm% OR w.example LIKE %:searchTerm% OR t LIKE %:searchTerm%")
    Page<Word> searchByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByWordAndLanguage(String word, String language);
}