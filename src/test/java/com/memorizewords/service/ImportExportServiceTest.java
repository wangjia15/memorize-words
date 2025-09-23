package com.memorizewords.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorizewords.dto.request.BulkImportOptions;
import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.response.BulkImportResult;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.dto.response.WordImportDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.enum.DifficultyLevel;
import com.memorizewords.exception.DuplicateWordException;
import com.memorizewords.exception.ImportException;
import com.memorizewords.repository.WordRepository;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportExportServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private WordService wordService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ImportExportService importExportService;

    private User testUser;
    private BulkImportOptions importOptions;
    private Word testWord;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        importOptions = new BulkImportOptions();
        importOptions.setFormat("csv");
        importOptions.setSkipDuplicates(true);

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWord("test");
        testWord.setLanguage("english");
        testWord.setDefinition("A test word");
        testWord.setDifficulty(DifficultyLevel.BEGINNER);
        testWord.setCreatedBy(testUser);
        testWord.setIsPublic(true);

        mockFile = mock(MultipartFile.class);
    }

    @Test
    void bulkImportWords_CSV_Success() throws Exception {
        // Mock CSV content
        String csvContent = "word,language,definition,difficulty\n" +
                           "test,english,A test word,BEGINNER\n" +
                           "example,english,An example word,BEGINNER";

        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // Mock word service behavior
        when(wordService.createWord(any(CreateWordRequest.class), eq(testUser)))
            .thenReturn(new WordDto(1L, "test", "english", "A test word", null, null, DifficultyLevel.BEGINNER, null, null, true, 1L, "testuser", null, null));

        BulkImportResult result = importExportService.bulkImportWords(mockFile, importOptions, testUser);

        assertNotNull(result);
        assertEquals(2, result.getTotalWords());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());

        verify(wordService, times(2)).createWord(any(CreateWordRequest.class), eq(testUser));
    }

    @Test
    void bulkImportWords_CSV_WithDuplicate() throws Exception {
        // Mock CSV content with duplicate
        String csvContent = "word,language,definition,difficulty\n" +
                           "test,english,A test word,BEGINNER";

        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        // Mock word service to throw exception for duplicate
        when(wordService.createWord(any(CreateWordRequest.class), eq(testUser)))
            .thenThrow(new DuplicateWordException("test", "english"));

        BulkImportResult result = importExportService.bulkImportWords(mockFile, importOptions, testUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalWords());
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertEquals(1, result.getSkippedCount());

        verify(wordService, times(1)).createWord(any(CreateWordRequest.class), eq(testUser));
    }

    @Test
    void bulkImportWords_CSV_WithError() throws Exception {
        // Mock CSV content with missing required field
        String csvContent = "word,language,definition,difficulty\n" +
                           ",english,A test word,BEGINNER";

        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvContent.getBytes()));
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");

        BulkImportResult result = importExportService.bulkImportWords(mockFile, importOptions, testUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalWords());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());

        verify(wordService, never()).createWord(any(CreateWordRequest.class), eq(testUser));
    }

    @Test
    void bulkImportWords_JSON_Success() throws Exception {
        importOptions.setFormat("json");

        // Mock JSON content
        String jsonContent = "[{\"word\":\"test\",\"language\":\"english\",\"definition\":\"A test word\",\"difficulty\":\"BEGINNER\"}]";

        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(jsonContent.getBytes()));
        when(mockFile.getOriginalFilename()).thenReturn("test.json");

        // Mock word service behavior
        when(wordService.createWord(any(CreateWordRequest.class), eq(testUser)))
            .thenReturn(new WordDto(1L, "test", "english", "A test word", null, null, DifficultyLevel.BEGINNER, null, null, true, 1L, "testuser", null, null));

        BulkImportResult result = importExportService.bulkImportWords(mockFile, importOptions, testUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalWords());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());

        verify(wordService, times(1)).createWord(any(CreateWordRequest.class), eq(testUser));
    }

    @Test
    void bulkImportWords_UnsupportedFormat_ThrowsException() {
        importOptions.setFormat("xml");

        assertThrows(ImportException.class, () -> {
            importExportService.bulkImportWords(mockFile, importOptions, testUser);
        });

        verify(wordService, never()).createWord(any(CreateWordRequest.class), eq(testUser));
    }

    @Test
    void exportWords_CSV_Success() throws IOException {
        List<Word> words = List.of(testWord);
        Page<Word> wordPage = new PageImpl<>(words);

        when(wordRepository.findAllById(Set.of(1L))).thenReturn(words);
        when(wordRepository.findAccessibleWords(eq(testUser), any(Pageable.class))).thenReturn(wordPage);

        Resource result = importExportService.exportWords("csv", Set.of(1L), testUser);

        assertNotNull(result);
        assertTrue(result instanceof ByteArrayResource);

        // Verify the content contains the expected CSV data
        byte[] content = ((ByteArrayResource) result).getByteArray();
        String csvString = new String(content, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("test"));
        assertTrue(csvString.contains("english"));
        assertTrue(csvString.contains("A test word"));

        verify(wordRepository).findAllById(Set.of(1L));
    }

    @Test
    void exportWords_JSON_Success() throws IOException {
        List<Word> words = List.of(testWord);
        Page<Word> wordPage = new PageImpl<>(words);

        when(wordRepository.findAllById(Set.of(1L))).thenReturn(words);
        when(wordRepository.findAccessibleWords(eq(testUser), any(Pageable.class))).thenReturn(wordPage);

        // Mock ObjectMapper behavior
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectMapper);
        when(objectMapper.writeValueAsString(any(List.class))).thenReturn("[{\"word\":\"test\",\"language\":\"english\"}]");

        Resource result = importExportService.exportWords("json", Set.of(1L), testUser);

        assertNotNull(result);
        assertTrue(result instanceof ByteArrayResource);

        verify(wordRepository).findAllById(Set.of(1L));
        verify(objectMapper).writeValueAsString(any(List.class));
    }

    @Test
    void exportWords_UnsupportedFormat_ThrowsException() {
        List<Word> words = List.of(testWord);
        Page<Word> wordPage = new PageImpl<>(words);

        when(wordRepository.findAllById(Set.of(1L))).thenReturn(words);
        when(wordRepository.findAccessibleWords(eq(testUser), any(Pageable.class))).thenReturn(wordPage);

        assertThrows(ImportException.class, () -> {
            importExportService.exportWords("xml", Set.of(1L), testUser);
        });

        verify(wordRepository).findAllById(Set.of(1L));
    }

    @Test
    void exportWords_AllWords_Success() throws IOException {
        List<Word> words = List.of(testWord);
        Page<Word> wordPage = new PageImpl<>(words);

        when(wordRepository.findAccessibleWords(eq(testUser), any(Pageable.class))).thenReturn(wordPage);

        Resource result = importExportService.exportWords("csv", null, testUser);

        assertNotNull(result);
        assertTrue(result instanceof ByteArrayResource);

        verify(wordRepository, never()).findAllById(any());
        verify(wordRepository).findAccessibleWords(eq(testUser), any(Pageable.class));
    }

    @Test
    void mapImportToRequest_MissingWord_ThrowsException() {
        WordImportDto importDto = new WordImportDto();
        importDto.setWord(""); // Empty word

        assertThrows(IllegalArgumentException.class, () -> {
            importExportService.mapImportToRequest(importDto);
        });
    }

    @Test
    void mapImportToRequest_MissingLanguage_SetsDefault() {
        WordImportDto importDto = new WordImportDto();
        importDto.setWord("test");
        importDto.setLanguage(""); // Empty language

        CreateWordRequest result = importExportService.mapImportToRequest(importDto);

        assertNotNull(result);
        assertEquals("test", result.getWord());
        assertEquals("english", result.getLanguage()); // Default language
    }

    @Test
    void mapImportToRequest_InvalidDifficulty_SetsDefault() {
        WordImportDto importDto = new WordImportDto();
        importDto.setWord("test");
        importDto.setLanguage("english");
        importDto.setDifficulty("INVALID");

        CreateWordRequest result = importExportService.mapImportToRequest(importDto);

        assertNotNull(result);
        assertEquals(DifficultyLevel.BEGINNER, result.getDifficulty()); // Default difficulty
    }
}