package com.memorizewords.controller;

import com.memorizewords.dto.request.BulkImportOptions;
import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.request.UpdateWordRequest;
import com.memorizewords.dto.request.WordSearchCriteria;
import com.memorizewords.dto.response.ApiResponse;
import com.memorizewords.dto.response.BulkImportResult;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.entity.User;
import com.memorizewords.service.DuplicateDetectionService;
import com.memorizewords.service.ImportExportService;
import com.memorizewords.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Set;

/**
 * Controller for word management operations.
 */
@RestController
@RequestMapping("/api/words")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Slf4j
public class WordController {

    private final WordService wordService;
    private final ImportExportService importExportService;
    private final DuplicateDetectionService duplicateDetectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<WordDto>> createWord(
            @Valid @RequestBody CreateWordRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        WordDto word = wordService.createWord(request, user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Word created successfully", word));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WordDto>>> searchWords(
            @ModelAttribute WordSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Page<WordDto> words = wordService.searchWords(criteria, user, pageable);

        return ResponseEntity.ok(ApiResponse.success("Words retrieved successfully", words));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WordDto>> getWord(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        WordDto word = wordService.getWordById(id);

        return ResponseEntity.ok(ApiResponse.success("Word retrieved successfully", word));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordDto>> updateWord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWordRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        WordDto word = wordService.updateWord(id, request, user);

        return ResponseEntity.ok(ApiResponse.success("Word updated successfully", word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        wordService.deleteWord(id, user);

        return ResponseEntity.ok(ApiResponse.success("Word deleted successfully", null));
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<ApiResponse<BulkImportResult>> bulkImport(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute BulkImportOptions options,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        BulkImportResult result = importExportService.bulkImportWords(file, options, user);

        return ResponseEntity.ok(ApiResponse.success("Bulk import completed", result));
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportWords(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Set<Long> wordIds,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        Resource resource = importExportService.exportWords(format, wordIds, user);

        String filename = "words." + format.toLowerCase();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/similar")
    public ResponseEntity<ApiResponse<List<WordDto>>> getSimilarWords(
            @RequestParam String partialWord,
            @RequestParam(required = false, defaultValue = "en") String language,
            @RequestParam(defaultValue = "10") @Min(1) int limit,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        List<WordDto> similarWords = wordService.getSimilarWords(partialWord, language, limit);

        return ResponseEntity.ok(ApiResponse.success("Similar words retrieved successfully", similarWords));
    }

    @GetMapping("/duplicates/stats")
    public ResponseEntity<ApiResponse<DuplicateDetectionService.DuplicateStats>> getDuplicateStats(
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        DuplicateDetectionService.DuplicateStats stats = wordService.getDuplicateStats(user);

        return ResponseEntity.ok(ApiResponse.success("Duplicate statistics retrieved successfully", stats));
    }

    @PostMapping("/{id}/duplicate-check")
    public ResponseEntity<ApiResponse<Boolean>> checkForDuplicates(
            @PathVariable Long id,
            @RequestParam String word,
            @RequestParam(required = false, defaultValue = "en") String language,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        boolean isDuplicate = duplicateDetectionService.isDuplicateWordForUser(word, language, user.getId());

        return ResponseEntity.ok(ApiResponse.success("Duplicate check completed", isDuplicate));
    }

    @GetMapping("/my-words")
    public ResponseEntity<ApiResponse<Page<WordDto>>> getMyWords(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        WordSearchCriteria criteria = new WordSearchCriteria();
        criteria.setCreatedByUserId(user.getId());

        Page<WordDto> words = wordService.searchWords(criteria, user, pageable);

        return ResponseEntity.ok(ApiResponse.success("User words retrieved successfully", words));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<WordDto>>> getPublicWords(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        WordSearchCriteria criteria = new WordSearchCriteria();
        criteria.setIsPublic(true);

        Page<WordDto> words = wordService.searchWords(criteria, user, pageable);

        return ResponseEntity.ok(ApiResponse.success("Public words retrieved successfully", words));
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