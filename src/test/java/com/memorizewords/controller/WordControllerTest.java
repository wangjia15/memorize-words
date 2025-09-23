package com.memorizewords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.request.UpdateWordRequest;
import com.memorizewords.dto.request.WordSearchCriteria;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.entity.User;
import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.WordCategory;
import com.memorizewords.service.WordService;
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
class WordControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private WordController wordController;

    @Mock
    private WordService wordService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        wordController = new WordController(wordService, null, null);
        mockMvc = MockMvcBuilders.standaloneSetup(wordController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createWord_shouldReturnCreatedWord() throws Exception {
        // Arrange
        CreateWordRequest request = new CreateWordRequest();
        request.setWord("hello");
        request.setLanguage("en");
        request.setDefinition("A greeting");
        request.setDifficulty(DifficultyLevel.BEGINNER);
        request.setCategories(Set.of(WordCategory.GREETING));
        request.setTags(Set.of("basic"));

        WordDto responseDto = createTestWordDto();

        when(wordService.createWord(any(CreateWordRequest.class), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.word").value("hello"))
                .andExpect(jsonPath("$.data.language").value("en"));
    }

    @Test
    void createWord_shouldReturnBadRequest_whenInvalidData() throws Exception {
        // Arrange
        CreateWordRequest request = new CreateWordRequest();
        request.setWord(""); // Invalid: empty word
        request.setLanguage(""); // Invalid: empty language

        // Act & Assert
        mockMvc.perform(post("/api/words")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWord_shouldReturnWord() throws Exception {
        // Arrange
        WordDto responseDto = createTestWordDto();

        when(wordService.getWordById(1L))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/words/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.word").value("hello"));
    }

    @Test
    void searchWords_shouldReturnPagedResults() throws Exception {
        // Arrange
        WordDto wordDto = createTestWordDto();
        Page<WordDto> page = new PageImpl<>(List.of(wordDto));

        when(wordService.searchWords(any(WordSearchCriteria.class), any(User.class), any(Pageable.class)))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/words")
                .param("word", "hello")
                .param("language", "en")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].word").value("hello"));
    }

    @Test
    void updateWord_shouldReturnUpdatedWord() throws Exception {
        // Arrange
        UpdateWordRequest request = new UpdateWordRequest();
        request.setDefinition("Updated definition");
        request.setDifficulty(DifficultyLevel.INTERMEDIATE);

        WordDto responseDto = createTestWordDto();
        responseDto.setDefinition("Updated definition");
        responseDto.setDifficulty(DifficultyLevel.INTERMEDIATE);

        when(wordService.updateWord(eq(1L), any(UpdateWordRequest.class), any(User.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/words/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.definition").value("Updated definition"))
                .andExpect(jsonPath("$.data.difficulty").value("INTERMEDIATE"));
    }

    @Test
    void deleteWord_shouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/words/1")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Word deleted successfully"));
    }

    @Test
    void getSimilarWords_shouldReturnSimilarWords() throws Exception {
        // Arrange
        WordDto wordDto = createTestWordDto();
        wordDto.setWord("holla");

        when(wordService.getSimilarWords(eq("hell"), eq("en"), eq(5)))
            .thenReturn(List.of(wordDto));

        // Act & Assert
        mockMvc.perform(get("/api/words/similar")
                .param("partialWord", "hell")
                .param("language", "en")
                .param("limit", "5")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].word").value("holla"));
    }

    @Test
    void getMyWords_shouldReturnUserWords() throws Exception {
        // Arrange
        WordDto wordDto = createTestWordDto();
        Page<WordDto> page = new PageImpl<>(List.of(wordDto));

        when(wordService.searchWords(any(WordSearchCriteria.class), any(User.class), any(Pageable.class)))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/words/my-words")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getPublicWords_shouldReturnPublicWords() throws Exception {
        // Arrange
        WordDto wordDto = createTestWordDto();
        wordDto.setIsPublic(true);
        Page<WordDto> page = new PageImpl<>(List.of(wordDto));

        when(wordService.searchWords(any(WordSearchCriteria.class), any(User.class), any(Pageable.class)))
            .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/words/public")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].isPublic").value(true));
    }

    private WordDto createTestWordDto() {
        WordDto dto = new WordDto();
        dto.setId(1L);
        dto.setWord("hello");
        dto.setLanguage("en");
        dto.setDefinition("A greeting");
        dto.setDifficulty(DifficultyLevel.BEGINNER);
        dto.setCategories(Set.of(WordCategory.GREETING));
        dto.setTags(Set.of("basic"));
        dto.setIsPublic(false);
        dto.setCreatedByUserId(1L);
        dto.setCreatedByUsername("testuser");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}