package com.memorizewords.service;

import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateDetectionServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private DuplicateDetectionService duplicateDetectionService;

    private User testUser;
    private Word testWord;
    private Word similarWord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPreferredLanguage("english");

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWord("test");
        testWord.setLanguage("english");
        testWord.setCreatedBy(testUser);

        similarWord = new Word();
        similarWord.setId(2L);
        similarWord.setWord("testament");
        similarWord.setLanguage("english");
        similarWord.setCreatedBy(testUser);
    }

    @Test
    void isDuplicateWord_True() {
        when(wordRepository.findByWordAndLanguage("test", "english")).thenReturn(Optional.of(testWord));

        boolean result = duplicateDetectionService.isDuplicateWord("test", "english");

        assertTrue(result);

        verify(wordRepository).findByWordAndLanguage("test", "english");
    }

    @Test
    void isDuplicateWord_False() {
        when(wordRepository.findByWordAndLanguage("test", "english")).thenReturn(Optional.empty());

        boolean result = duplicateDetectionService.isDuplicateWord("test", "english");

        assertFalse(result);

        verify(wordRepository).findByWordAndLanguage("test", "english");
    }

    @Test
    void isDuplicateWordForUser_True() {
        when(wordRepository.findByWordAndLanguage("test", "english")).thenReturn(Optional.of(testWord));

        boolean result = duplicateDetectionService.isDuplicateWordForUser("test", "english", testUser);

        assertTrue(result);

        verify(wordRepository).findByWordAndLanguage("test", "english");
    }

    @Test
    void isDuplicateWordForUser_False_DifferentUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(wordRepository.findByWordAndLanguage("test", "english")).thenReturn(Optional.of(testWord));

        boolean result = duplicateDetectionService.isDuplicateWordForUser("test", "english", otherUser);

        assertFalse(result);

        verify(wordRepository).findByWordAndLanguage("test", "english");
    }

    @Test
    void isDuplicateWordForUser_False_NoWord() {
        when(wordRepository.findByWordAndLanguage("test", "english")).thenReturn(Optional.empty());

        boolean result = duplicateDetectionService.isDuplicateWordForUser("test", "english", testUser);

        assertFalse(result);

        verify(wordRepository).findByWordAndLanguage("test", "english");
    }

    @Test
    void findSimilarWords_Success() {
        List<Word> similarWords = List.of(testWord, similarWord);

        when(wordRepository.findByWordContainingIgnoreCase("tes")).thenReturn(similarWords);

        List<Word> result = duplicateDetectionService.findSimilarWords("tes", "english", 5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test", result.get(0).getWord());
        assertEquals("testament", result.get(1).getWord());

        verify(wordRepository).findByWordContainingIgnoreCase("tes");
    }

    @Test
    void findSimilarWords_WithLanguageFilter() {
        Word spanishWord = new Word();
        spanishWord.setId(3L);
        spanishWord.setWord("prueba");
        spanishWord.setLanguage("spanish");

        List<Word> allSimilarWords = List.of(testWord, similarWord, spanishWord);

        when(wordRepository.findByWordContainingIgnoreCase("tes")).thenReturn(allSimilarWords);

        List<Word> result = duplicateDetectionService.findSimilarWords("tes", "english", 5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(w -> "english".equals(w.getLanguage())));

        verify(wordRepository).findByWordContainingIgnoreCase("tes");
    }

    @Test
    void findSimilarWords_WithLimit() {
        List<Word> manySimilarWords = List.of(
            testWord, similarWord,
            new Word(3L, "testing", "english"),
            new Word(4L, "tester", "english"),
            new Word(5L, "testify", "english"),
            new Word(6L, "testamentary", "english")
        );

        when(wordRepository.findByWordContainingIgnoreCase("tes")).thenReturn(manySimilarWords);

        List<Word> result = duplicateDetectionService.findSimilarWords("tes", "english", 3);

        assertNotNull(result);
        assertEquals(3, result.size());

        verify(wordRepository).findByWordContainingIgnoreCase("tes");
    }

    @Test
    void hasReachedWordLimit_True() {
        when(wordRepository.countByCreatedBy(testUser)).thenReturn(100L);

        boolean result = duplicateDetectionService.hasReachedWordLimit(testUser, 100);

        assertTrue(result);

        verify(wordRepository).countByCreatedBy(testUser);
    }

    @Test
    void hasReachedWordLimit_False() {
        when(wordRepository.countByCreatedBy(testUser)).thenReturn(50L);

        boolean result = duplicateDetectionService.hasReachedWordLimit(testUser, 100);

        assertFalse(result);

        verify(wordRepository).countByCreatedBy(testUser);
    }

    @Test
    void getDuplicateStats_Success() {
        when(wordRepository.countByCreatedBy(testUser)).thenReturn(25L);
        when(wordRepository.countByLanguageAndIsPublicTrue("english")).thenReturn(1000L);

        DuplicateDetectionService.DuplicateStats result = duplicateDetectionService.getDuplicateStats(testUser);

        assertNotNull(result);
        assertEquals(25L, result.getUserWordCount());
        assertEquals(1000L, result.getPublicWordsInLanguage());

        verify(wordRepository).countByCreatedBy(testUser);
        verify(wordRepository).countByLanguageAndIsPublicTrue("english");
    }

    @Test
    void getDuplicateStats_NoPreferredLanguage() {
        testUser.setPreferredLanguage(null);

        when(wordRepository.countByCreatedBy(testUser)).thenReturn(25L);
        when(wordRepository.countByLanguageAndIsPublicTrue("english")).thenReturn(1000L);

        DuplicateDetectionService.DuplicateStats result = duplicateDetectionService.getDuplicateStats(testUser);

        assertNotNull(result);
        assertEquals(25L, result.getUserWordCount());
        assertEquals(1000L, result.getPublicWordsInLanguage());

        verify(wordRepository).countByCreatedBy(testUser);
        verify(wordRepository).countByLanguageAndIsPublicTrue("english");
    }

    @Test
    void suggestAlternativeWords_Success() {
        List<Word> similarWords = List.of(testWord, similarWord);

        when(wordRepository.findByWordContainingIgnoreCase("tes")).thenReturn(similarWords);

        List<String> result = duplicateDetectionService.suggestAlternativeWords("test", "english");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testament", result.get(0));

        verify(wordRepository).findByWordContainingIgnoreCase("tes");
    }

    @Test
    void suggestAlternativeWords_NoSimilarWords() {
        when(wordRepository.findByWordContainingIgnoreCase("xyz")).thenReturn(List.of());

        List<String> result = duplicateDetectionService.suggestAlternativeWords("xyz", "english");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(wordRepository).findByWordContainingIgnoreCase("xyz");
    }

    @Test
    void suggestAlternativeWords_OnlyExactMatch() {
        when(wordRepository.findByWordContainingIgnoreCase("test")).thenReturn(List.of(testWord));

        List<String> result = duplicateDetectionService.suggestAlternativeWords("test", "english");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(wordRepository).findByWordContainingIgnoreCase("test");
    }

    @Test
    void suggestAlternativeWords_LimitResults() {
        List<Word> manySimilarWords = List.of(
            testWord,
            similarWord,
            new Word(3L, "testing", "english"),
            new Word(4L, "tester", "english"),
            new Word(5L, "testify", "english")
        );

        when(wordRepository.findByWordContainingIgnoreCase("tes")).thenReturn(manySimilarWords);

        List<String> result = duplicateDetectionService.suggestAlternativeWords("test", "english");

        assertNotNull(result);
        assertEquals(3, result.size());

        verify(wordRepository).findByWordContainingIgnoreCase("tes");
    }

    @Test
    void suggestAlternativeWords_ShortWord() {
        List<Word> similarWords = List.of(testWord, similarWord);

        when(wordRepository.findByWordContainingIgnoreCase("a")).thenReturn(similarWords);

        List<String> result = duplicateDetectionService.suggestAlternativeWords("a", "english");

        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should be empty since word length < 3

        verify(wordRepository, never()).findByWordContainingIgnoreCase(any());
    }
}