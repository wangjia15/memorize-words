package com.memorizewords.service;

import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for detecting duplicate words and providing suggestions.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DuplicateDetectionService {

    private final WordRepository wordRepository;

    /**
     * Check if a word already exists for the given language.
     */
    public boolean isDuplicateWord(String word, String language) {
        return wordRepository.findByWordAndLanguage(word, language).isPresent();
    }

    /**
     * Check if a word exists and was created by a specific user.
     */
    public boolean isDuplicateWordForUser(String word, String language, User user) {
        Optional<Word> existingWord = wordRepository.findByWordAndLanguage(word, language);
        return existingWord.isPresent() && existingWord.get().getCreatedBy().getId().equals(user.getId());
    }

    /**
     * Find similar words based on partial matches.
     */
    public List<Word> findSimilarWords(String partialWord, String language, int limit) {
        List<Word> similarWords = wordRepository.findByWordContainingIgnoreCase(partialWord);

        // Filter by language and limit results
        return similarWords.stream()
            .filter(word -> language == null || language.equals(word.getLanguage()))
            .limit(limit)
            .toList();
    }

    /**
     * Check if a user has reached their word limit.
     */
    public boolean hasReachedWordLimit(User user, int maxWords) {
        long userWordCount = wordRepository.countByCreatedBy(user);
        return userWordCount >= maxWords;
    }

    /**
     * Get duplicate statistics for a user.
     */
    public DuplicateStats getDuplicateStats(User user) {
        long totalUserWords = wordRepository.countByCreatedBy(user);
        long publicWordsWithSameLanguage = wordRepository.countByLanguageAndIsPublicTrue(
            user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "english"
        );

        return new DuplicateStats(totalUserWords, publicWordsWithSameLanguage);
    }

    /**
     * Suggest alternative words if duplicate is found.
     */
    public List<String> suggestAlternativeWords(String word, String language) {
        List<Word> similarWords = findSimilarWords(word.substring(0, Math.min(3, word.length())), language, 5);

        return similarWords.stream()
            .map(Word::getWord)
            .filter(w -> !w.equalsIgnoreCase(word))
            .distinct()
            .limit(3)
            .toList();
    }

    /**
     * Statistics class for duplicate detection.
     */
    public static class DuplicateStats {
        private final long userWordCount;
        private final long publicWordsInLanguage;

        public DuplicateStats(long userWordCount, long publicWordsInLanguage) {
            this.userWordCount = userWordCount;
            this.publicWordsInLanguage = publicWordsInLanguage;
        }

        public long getUserWordCount() {
            return userWordCount;
        }

        public long getPublicWordsInLanguage() {
            return publicWordsInLanguage;
        }
    }
}