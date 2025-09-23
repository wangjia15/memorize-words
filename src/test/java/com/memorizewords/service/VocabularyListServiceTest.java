package com.memorizewords.service;

import com.memorizewords.dto.request.CreateListRequest;
import com.memorizewords.dto.response.VocabularyListDto;
import com.memorizewords.dto.response.WordSummaryDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.VocabularyList;
import com.memorizewords.entity.Word;
import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.ListType;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.VocabularyListRepository;
import com.memorizewords.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyListServiceTest {

    @Mock
    private VocabularyListRepository listRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private VocabularyListService listService;

    private User testUser;
    private CreateListRequest createRequest;
    private VocabularyList testList;
    private Word testWord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        createRequest = new CreateListRequest();
        createRequest.setName("Test List");
        createRequest.setDescription("A test vocabulary list");
        createRequest.setIsPublic(true);
        createRequest.setType(ListType.CUSTOM);

        testList = new VocabularyList();
        testList.setId(1L);
        testList.setName("Test List");
        testList.setDescription("A test vocabulary list");
        testList.setOwner(testUser);
        testList.setIsPublic(true);
        testList.setType(ListType.CUSTOM);
        testList.setWords(new HashSet<>());

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWord("test");
        testWord.setLanguage("english");
        testWord.setDifficulty(DifficultyLevel.BEGINNER);
    }

    @Test
    void createList_Success() {
        when(listRepository.existsByOwnerAndName(testUser, "Test List")).thenReturn(false);
        when(listRepository.save(any(VocabularyList.class))).thenReturn(testList);

        VocabularyListDto result = listService.createList(createRequest, testUser);

        assertNotNull(result);
        assertEquals("Test List", result.getName());
        assertEquals("A test vocabulary list", result.getDescription());
        assertTrue(result.getIsPublic());
        assertEquals(ListType.CUSTOM, result.getType());

        verify(listRepository).existsByOwnerAndName(testUser, "Test List");
        verify(listRepository).save(any(VocabularyList.class));
    }

    @Test
    void createList_DuplicateName_ThrowsException() {
        when(listRepository.existsByOwnerAndName(testUser, "Test List")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            listService.createList(createRequest, testUser);
        });

        verify(listRepository).existsByOwnerAndName(testUser, "Test List");
        verify(listRepository, never()).save(any(VocabularyList.class));
    }

    @Test
    void getListById_Success() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));

        VocabularyListDto result = listService.getListById(1L, testUser);

        assertNotNull(result);
        assertEquals("Test List", result.getName());
        assertEquals(1L, result.getId());

        verify(listRepository).findById(1L);
    }

    @Test
    void getListById_NotFound_ThrowsException() {
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            listService.getListById(1L, testUser);
        });

        verify(listRepository).findById(1L);
    }

    @Test
    void getUserLists_Success() {
        List<VocabularyList> lists = List.of(testList);
        when(listRepository.findByOwnerOrderByCreatedAtDesc(testUser)).thenReturn(lists);

        List<VocabularyListDto> result = listService.getUserLists(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test List", result.get(0).getName());

        verify(listRepository).findByOwnerOrderByCreatedAtDesc(testUser);
    }

    @Test
    void getPublicLists_Success() {
        List<VocabularyList> lists = List.of(testList);
        when(listRepository.findByIsPublicTrueOrderByCreatedAtDesc()).thenReturn(lists);

        List<VocabularyListDto> result = listService.getPublicLists();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test List", result.get(0).getName());

        verify(listRepository).findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Test
    void getAccessibleLists_Success() {
        List<VocabularyList> lists = List.of(testList);
        when(listRepository.findAccessibleLists(testUser)).thenReturn(lists);

        List<VocabularyListDto> result = listService.getAccessibleLists(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test List", result.get(0).getName());

        verify(listRepository).findAccessibleLists(testUser);
    }

    @Test
    void addWordsToList_Success() {
        Set<Long> wordIds = Set.of(1L);
        Set<Word> words = Set.of(testWord);

        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(wordRepository.findAllById(wordIds)).thenReturn(List.copyOf(words));
        when(listRepository.save(any(VocabularyList.class))).thenReturn(testList);

        VocabularyListDto result = listService.addWordsToList(1L, wordIds, testUser);

        assertNotNull(result);
        assertEquals(1, result.getWordCount());
        assertTrue(result.getWords().stream().anyMatch(w -> w.getId().equals(1L)));

        verify(listRepository).findById(1L);
        verify(wordRepository).findAllById(wordIds);
        verify(listRepository).save(any(VocabularyList.class));
    }

    @Test
    void addWordsToList_ListNotFound_ThrowsException() {
        Set<Long> wordIds = Set.of(1L);

        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            listService.addWordsToList(1L, wordIds, testUser);
        });

        verify(listRepository).findById(1L);
        verify(wordRepository, never()).findAllById(any());
        verify(listRepository, never()).save(any());
    }

    @Test
    void removeWordsFromList_Success() {
        // First add a word to the list
        testList.getWords().add(testWord);
        testList.setWordCount(1);

        Set<Long> wordIds = Set.of(1L);

        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(wordRepository.findAllById(wordIds)).thenReturn(List.of(testWord));
        when(listRepository.save(any(VocabularyList.class))).thenReturn(testList);

        VocabularyListDto result = listService.removeWordsFromList(1L, wordIds, testUser);

        assertNotNull(result);
        assertEquals(0, result.getWordCount());
        assertTrue(result.getWords().isEmpty());

        verify(listRepository).findById(1L);
        verify(wordRepository).findAllById(wordIds);
        verify(listRepository).save(any(VocabularyList.class));
    }

    @Test
    void shareList_Success() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(listRepository.save(any(VocabularyList.class))).thenReturn(testList);

        listService.shareList(1L, testUser);

        assertTrue(testList.getIsShared());

        verify(listRepository).findById(1L);
        verify(listRepository).save(any(VocabularyList.class));
    }

    @Test
    void deleteList_Success() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(testList));

        listService.deleteList(1L, testUser);

        verify(listRepository).findById(1L);
        verify(listRepository).delete(testList);
    }

    @Test
    void deleteList_NotFound_ThrowsException() {
        when(listRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            listService.deleteList(1L, testUser);
        });

        verify(listRepository).findById(1L);
        verify(listRepository, never()).delete(any());
    }
}