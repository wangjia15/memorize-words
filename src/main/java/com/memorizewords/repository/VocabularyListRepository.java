package com.memorizewords.repository;

import com.memorizewords.entity.User;
import com.memorizewords.entity.VocabularyList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for VocabularyList entity.
 */
@Repository
public interface VocabularyListRepository extends JpaRepository<VocabularyList, Long> {

    List<VocabularyList> findByOwnerOrderByCreatedAtDesc(User owner);

    List<VocabularyList> findByIsPublicTrueOrderByCreatedAtDesc();

    List<VocabularyList> findByOwnerAndNameContainingIgnoreCase(User owner, String name);

    @Query("SELECT vl FROM VocabularyList vl WHERE vl.owner = :user OR vl.isShared = true")
    List<VocabularyList> findAccessibleLists(@Param("user") User user);

    @Query("SELECT vl FROM VocabularyList vl WHERE vl.owner = :user AND vl.name = :name")
    Optional<VocabularyList> findByOwnerAndName(@Param("user") User user, @Param("name") String name);

    boolean existsByOwnerAndName(User owner, String name);
}