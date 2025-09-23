package com.memorizewords.service;

import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.request.UpdateWordRequest;
import com.memorizewords.dto.request.WordSearchCriteria;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.WordCategory;
import com.memorizewords.exception.DuplicateWordException;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.WordRepository;
import com.memorizewords.specification.WordSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private DuplicateDetectionService duplicateDetectionService;

    @Mock
    private ImportExportService importExportService;

    @InjectMocks
    private WordService wordService;

    private User testUser;
    private CreateWordRequest createRequest;
    private UpdateWordRequest updateRequest;
    private Word testWord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        createRequest = new CreateWordRequest();
        createRequest.setWord("test");
        createRequest.setLanguage("english");
        createRequest.setDefinition("A test word");
        createRequest.setDifficulty(DifficultyLevel.BEGINNER);
        createRequest.setCategories(Set.of(WordCategory.GENERAL));
        createRequest.setTags(Set.of("test", "example"));
        createRequest.setIsPublic(true);

        updateRequest = new UpdateWordRequest();
        updateRequest.setDefinition("Updated definition");
        updateRequest.setDifficulty(DifficultyLevel.INTERMEDIATE);

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWord("test");
        testWord.setLanguage("english");
        testWord.setDefinition("A test word");
        testWord.setDifficulty(DifficultyLevel.BEGINNER);
        testWord.setCategories(new HashSet<>(Set.of(WordCategory.GENERAL)));
        testWord.setTags(new HashSet<>(Set.of("test", "example")));
        testWord.setCreatedBy(testUser);
        testWord.setIsPublic(true);
    }

    @Test
    void createWord_Success() {
        when(duplicateDetectionService.isDuplicateWord("test", "english")).thenReturn(false);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        WordDto result = wordService.createWord(createRequest, testUser);

        assertNotNull(result);
        assertEquals("test", result.getWord());
        assertEquals("english", result.getLanguage());
        assertEquals(DifficultyLevel.BEGINNER, result.getDifficulty());
        assertTrue(result.getIsPublic());

        verify(duplicateDetectionService).isDuplicateWord("test", "english");
        verify(wordRepository).save(any(Word.class));
    }

    @Test
    void createWord_Duplicate_ThrowsException() {
        when(duplicateDetectionService.isDuplicateWord("test", "english")).thenReturn(true);

        assertThrows(DuplicateWordException.class, () -> {
            wordService.createWord(createRequest, testUser);
        });

        verify(duplicateDetectionService).isDuplicateWord("test", "english");
        verify(wordRepository, never()).save(any(Word.class));
    }

    @Test
    void getWordById_Success() {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

        WordDto result = wordService.getWordById(1L);

        assertNotNull(result);
        assertEquals("test", result.getWord());
        assertEquals(1L, result.getId());

        verify(wordRepository).findById(1L);
    }

    @Test
    void getWordById_NotFound_ThrowsException() {
        when(wordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            wordService.getWordById(1L);
        });

        verify(wordRepository).findById(1L);
    }

    @Test
    void updateWord_Success() {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        WordDto result = wordService.updateWord(1L, updateRequest, testUser);

        assertNotNull(result);
        assertEquals("Updated definition", testWord.getDefinition());
        assertEquals(DifficultyLevel.INTERMEDIATE, testWord.getDifficulty());

        verify(wordRepository).findById(1L);
        verify(wordRepository).save(any(Word.class));
    }

    @Test
    void updateWord_NotFound_ThrowsException() {
        when(wordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            wordService.updateWord(1L, updateRequest, testUser);
        });

        verify(wordRepository).findById(1L);
        verify(wordRepository, never()).save(any(Word.class));
    }

    @Test
    void deleteWord_Success() {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

        wordService.deleteWord(1L, testUser);

        verify(wordRepository).findById(1L);
        verify(wordRepository).delete(testWord);
    }

    @Test
    void deleteWord_NotFound_ThrowsException() {
        when(wordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            wordService.deleteWord(1L, testUser);
        });

        verify(wordRepository).findById(1L);
        verify(wordRepository, never()).delete(any(Word.class));
    }

    @Test
    void searchWords_Success() {
        WordSearchCriteria criteria = new WordSearchCriteria();
        criteria.setSearchTerm("test");

        Page<Word> wordPage = new PageImpl<>(List.of(testWord));

        try (MockedStatic<WordSpecifications> mockedStatic = mockStatic(WordSpecifications.class)) {
            mockedStatic.when(() -> WordSpecifications.buildSpecification(criteria, testUser))
                       .thenReturn((Specification<Word>) (root, query, cb) -> cb.equal(root.get("word"), "test"));

            when(wordRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(wordPage);

            Page<WordDto> result = wordService.searchWords(criteria, testUser, Pageable.unpaged());

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("test", result.getContent().get(0).getWord());

            verify(wordRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Test
    void bulkImportWords_Success() {
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        com.memorizewords.dto.request.BulkImportOptions options = new com.memorizewords.dto.request.BulkImportOptions();
        options.setFormat("csv");
        options.setSkipDuplicates(true);

        com.memorizewords.dto.response.BulkImportResult mockResult = new com.memorizewords.dto.response.BulkImportResult();
        mockResult.setTotalWords(1);
        mockResult.setSuccessCount(1);

        when(importExportService.bulkImportWords(mockFile, options, testUser)).thenReturn(mockResult);

        com.memorizewords.dto.response.BulkImportResult result = wordService.bulkImportWords(mockFile, options, testUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalWords());

        verify(importExportService).bulkImportWords(mockFile, options, testUser);
    }

    @Test
    void exportWords_Success() {
        Set<Long> wordIds = Set.of(1L);
        org.springframework.core.io.Resource mockResource = mock(org.springframework.core.io.Resource.class);

        when(importExportService.exportWords("csv", wordIds, testUser)).thenReturn(mockResource);

        org.springframework.core.io.Resource result = wordService.exportWords("csv", wordIds, testUser);

        assertNotNull(result);
        assertEquals(mockResource, result);

        verify(importExportService).exportWords("csv", wordIds, testUser);
    }

    @Test
    void getSimilarWords_Success() {
        Word similarWord = new Word();
        similarWord.setId(2L);
        similarWord.setWord("testament");
        similarWord.setLanguage("english");

        when(duplicateDetectionService.findSimilarWords("test", "english", 5))
            .thenReturn(List.of(similarWord));

        List<WordDto> result = wordService.getSimilarWords("test", "english", 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testament", result.get(0).getWord());

        verify(duplicateDetectionService).findSimilarWords("test", "english", 5);
    }

    @Test
    void getDuplicateStats_Success() {
        DuplicateDetectionService.DuplicateStats mockStats = new DuplicateDetectionService.DuplicateStats(10L, 50L);

        when(duplicateDetectionService.getDuplicateStats(testUser)).thenReturn(mockStats);

        DuplicateDetectionService.DuplicateStats result = wordService.getDuplicateStats(testUser);

        assertNotNull(result);
        assertEquals(10L, result.getUserWordCount());
        assertEquals(50L, result.getPublicWordsInLanguage());

        verify(duplicateDetectionService).getDuplicateStats(testUser);
    }
}