package com.memorizewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorizewords.dto.request.AddWordsRequest;
import com.memorizewords.dto.request.CreateListRequest;
import com.memorizewords.dto.request.RemoveWordsRequest;
import com.memorizewords.dto.response.VocabularyListDto;
import com.memorizewords.dto.response.WordSummaryDto;
import com.memorizewords.entity.User;
import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.ListType;
import com.memorizewords.service.VocabularyListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VocabularyListControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private VocabularyListController vocabularyListController;

    @Mock
    private VocabularyListService listService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        vocabularyListController = new VocabularyListController(listService);
        mockMvc = MockMvcBuilders.standaloneSetup(vocabularyListController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createList_shouldReturnCreatedList() throws Exception {
        // Arrange
        CreateListRequest request = new CreateListRequest();
        request.setName("My Vocabulary List");
        request.setDescription("A list of common words");
        request.setType(ListType.CUSTOM);
        request.setIsPublic(false);

        VocabularyListDto responseDto = createTestVocabularyListDto();

        when(listService.createList(any(CreateListRequest.class), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/vocabulary-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("My Vocabulary List"))
                .andExpect(jsonPath("$.data.type").value("CUSTOM"));
    }

    @Test
    void getUserLists_shouldReturnUserLists() throws Exception {
        // Arrange
        VocabularyListDto listDto = createTestVocabularyListDto();

        when(listService.getUserLists(any(User.class)))
            .thenReturn(List.of(listDto));

        // Act & Assert
        mockMvc.perform(get("/api/vocabulary-lists")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("My Vocabulary List"));
    }

    @Test
    void getPublicLists_shouldReturnPublicLists() throws Exception {
        // Arrange
        VocabularyListDto listDto = createTestVocabularyListDto();
        listDto.setIsPublic(true);

        when(listService.getPublicLists())
            .thenReturn(List.of(listDto));

        // Act & Assert
        mockMvc.perform(get("/api/vocabulary-lists/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].isPublic").value(true));
    }

    @Test
    void getList_shouldReturnList() throws Exception {
        // Arrange
        VocabularyListDto responseDto = createTestVocabularyListDto();

        when(listService.getListById(eq(1L), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/vocabulary-lists/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("My Vocabulary List"));
    }

    @Test
    void updateList_shouldReturnUpdatedList() throws Exception {
        // Arrange
        CreateListRequest request = new CreateListRequest();
        request.setName("Updated List Name");
        request.setDescription("Updated description");

        VocabularyListDto responseDto = createTestVocabularyListDto();
        responseDto.setName("Updated List Name");
        responseDto.setDescription("Updated description");

        when(listService.updateList(eq(1L), any(CreateListRequest.class), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/vocabulary-lists/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated List Name"));
    }

    @Test
    void deleteList_shouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/vocabulary-lists/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List deleted successfully"));
    }

    @Test
    void addWordsToList_shouldReturnUpdatedList() throws Exception {
        // Arrange
        AddWordsRequest request = new AddWordsRequest();
        request.setWordIds(Set.of(1L, 2L, 3L));

        VocabularyListDto responseDto = createTestVocabularyListDto();
        responseDto.setWordCount(3);

        when(listService.addWordsToList(eq(1L), eq(Set.of(1L, 2L, 3L)), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/vocabulary-lists/1/words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wordCount").value(3));
    }

    @Test
    void removeWordsFromList_shouldReturnUpdatedList() throws Exception {
        // Arrange
        RemoveWordsRequest request = new RemoveWordsRequest();
        request.setWordIds(Set.of(1L));

        VocabularyListDto responseDto = createTestVocabularyListDto();
        responseDto.setWordCount(2);

        when(listService.removeWordsFromList(eq(1L), eq(Set.of(1L)), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(delete("/api/vocabulary-lists/1/words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wordCount").value(2));
    }

    @Test
    void shareList_shouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/vocabulary-lists/1/share")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List shared successfully"));
    }

    @Test
    void unshareList_shouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/vocabulary-lists/1/unshare")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List unshared successfully"));
    }

    @Test
    void getListWords_shouldReturnPagedWords() throws Exception {
        // Arrange
        WordSummaryDto wordDto = new WordSummaryDto();
        wordDto.setId(1L);
        wordDto.setWord("hello");
        wordDto.setLanguage("en");
        wordDto.setDifficulty(DifficultyLevel.BEGINNER);

        Page<WordSummaryDto> page = new PageImpl<>(List.of(wordDto));

        when(listService.getListWords(eq(1L), any(User.class), any(Pageable.class)))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/vocabulary-lists/1/words")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].word").value("hello"));
    }

    @Test
    void searchLists_shouldReturnMatchingLists() throws Exception {
        // Arrange
        VocabularyListDto listDto = createTestVocabularyListDto();

        when(listService.searchLists(eq("vocabulary"), any(User.class)))
            .thenReturn(List.of(listDto));

        // Act & Assert
        mockMvc.perform(get("/api/vocabulary-lists/search")
                .param("query", "vocabulary")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("My Vocabulary List"));
    }

    @Test
    void cloneList_shouldReturnClonedList() throws Exception {
        // Arrange
        VocabularyListDto responseDto = createTestVocabularyListDto();
        responseDto.setId(2L);
        responseDto.setName("My Vocabulary List (Copy)");

        when(listService.cloneList(eq(1L), eq("My Vocabulary List (Copy)"), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/vocabulary-lists/1/clone")
                .param("newName", "My Vocabulary List (Copy)")
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("My Vocabulary List (Copy)"));
    }

    private VocabularyListDto createTestVocabularyListDto() {
        VocabularyListDto dto = new VocabularyListDto();
        dto.setId(1L);
        dto.setName("My Vocabulary List");
        dto.setDescription("A list of common words");
        dto.setOwnerId(1L);
        dto.setOwnerUsername("testuser");
        dto.setIsPublic(false);
        dto.setIsShared(false);
        dto.setTags(Set.of("common", "basic"));
        dto.setType(ListType.CUSTOM);
        dto.setWordCount(3);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}