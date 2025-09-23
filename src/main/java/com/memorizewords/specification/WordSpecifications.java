package com.memorizewords.specification;

import com.memorizewords.dto.request.WordSearchCriteria;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.WordCategory;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * Specifications for building dynamic queries for Word entity.
 */
public class WordSpecifications {

    public static Specification<Word> buildSpecification(WordSearchCriteria criteria, User user) {
        return Specification.where(withWord(criteria.getWord()))
                .and(withLanguage(criteria.getLanguage()))
                .and(withDifficulty(criteria.getDifficulty()))
                .and(withCategory(criteria.getCategory()))
                .and(withTag(criteria.getTag()))
                .and(withPublicAccess(criteria.getIsPublic()))
                .and(withCreatedBy(criteria.getCreatedBy(), user));
    }

    private static Specification<Word> withWord(String word) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(word)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("word")), "%" + word.toLowerCase() + "%");
        };
    }

    private static Specification<Word> withLanguage(String language) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(language)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("language"), language);
        };
    }

    private static Specification<Word> withDifficulty(DifficultyLevel difficulty) {
        return (root, query, cb) -> {
            if (difficulty == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("difficulty"), difficulty);
        };
    }

    private static Specification<Word> withCategory(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) {
                return cb.conjunction();
            }

            try {
                WordCategory categoryEnum = WordCategory.valueOf(category.toUpperCase());
                return cb.isMember(categoryEnum, root.get("categories"));
            } catch (IllegalArgumentException e) {
                return cb.disjunction();
            }
        };
    }

    private static Specification<Word> withTag(String tag) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(tag)) {
                return cb.conjunction();
            }
            return cb.isMember(tag, root.get("tags"));
        };
    }

    private static Specification<Word> withPublicAccess(Boolean isPublic) {
        return (root, query, cb) -> {
            if (isPublic == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isPublic"), isPublic);
        };
    }

    private static Specification<Word> withCreatedBy(String createdBy, User currentUser) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(createdBy)) {
                return cb.conjunction();
            }

            if (currentUser != null && createdBy.equalsIgnoreCase("me")) {
                return cb.equal(root.get("createdBy"), currentUser);
            }

            Join<Word, User> userJoin = root.join("createdBy", JoinType.LEFT);
            return cb.like(cb.lower(userJoin.get("username")), "%" + createdBy.toLowerCase() + "%");
        };
    }
}