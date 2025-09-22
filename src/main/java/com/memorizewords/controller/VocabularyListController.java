package com.memorizewords.controller;

import com.memorizewords.dto.request.AddWordsRequest;
import com.memorizewords.dto.request.CreateListRequest;
import com.memorizewords.dto.request.RemoveWordsRequest;
import com.memorizewords.dto.response.ApiResponse;
import com.memorizewords.dto.response.VocabularyListDto;
import com.memorizewords.entity.User;
import com.memorizewords.service.VocabularyListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * Controller for vocabulary list management operations.
 */
@RestController
@RequestMapping("/api/vocabulary-lists")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Slf4j
public class VocabularyListController {

    private final VocabularyListService listService;

    @PostMapping
    public ResponseEntity<ApiResponse<VocabularyListDto>> createList(
            @Valid @RequestBody CreateListRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.createList(request, user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Vocabulary list created successfully", list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VocabularyListDto>>> getUserLists(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        List<VocabularyListDto> lists = listService.getUserLists(user);

        return ResponseEntity.ok(ApiResponse.success("User lists retrieved successfully", lists));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<VocabularyListDto>>> getPublicLists() {
        List<VocabularyListDto> lists = listService.getPublicLists();

        return ResponseEntity.ok(ApiResponse.success("Public lists retrieved successfully", lists));
    }

    @GetMapping("/accessible")
    public ResponseEntity<ApiResponse<List<VocabularyListDto>>> getAccessibleLists(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        List<VocabularyListDto> lists = listService.getAccessibleLists(user);

        return ResponseEntity.ok(ApiResponse.success("Accessible lists retrieved successfully", lists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyListDto>> getList(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.getListById(id, user);

        return ResponseEntity.ok(ApiResponse.success("Vocabulary list retrieved successfully", list));
    }

    @PostMapping("/{id}/words")
    public ResponseEntity<ApiResponse<VocabularyListDto>> addWords(
            @PathVariable Long id,
            @Valid @RequestBody AddWordsRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.addWordsToList(id, request.getWordIds(), user);

        return ResponseEntity.ok(ApiResponse.success("Words added to list successfully", list));
    }

    @DeleteMapping("/{id}/words")
    public ResponseEntity<ApiResponse<VocabularyListDto>> removeWords(
            @PathVariable Long id,
            @Valid @RequestBody RemoveWordsRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.removeWordsFromList(id, request.getWordIds(), user);

        return ResponseEntity.ok(ApiResponse.success("Words removed from list successfully", list));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<Void>> shareList(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        listService.shareList(id, user);

        return ResponseEntity.ok(ApiResponse.success("List shared successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteList(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        listService.deleteList(id, user);

        return ResponseEntity.ok(ApiResponse.success("List deleted successfully", null));
    }

    private User getCurrentUser(Authentication authentication) {
        // This would typically get the user from the authentication principal
        // For now, we'll create a simple user implementation
        // In a real application, this would be properly implemented with Spring Security
        User user = new User();
        user.setId(1L); // Default user ID for development
        user.setUsername(authentication.getName());
        return user;
    }
}