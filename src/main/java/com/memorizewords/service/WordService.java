package com.memorizewords.service;

import com.memorizewords.dto.request.CreateWordRequest;
import com.memorizewords.dto.request.UpdateWordRequest;
import com.memorizewords.dto.request.WordSearchCriteria;
import com.memorizewords.dto.response.BulkImportResult;
import com.memorizewords.dto.response.WordDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.Word;
import com.memorizewords.exception.AccessDeniedException;
import com.memorizewords.exception.DuplicateWordException;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.WordRepository;
import com.memorizewords.specification.WordSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for word management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WordService {

    private final WordRepository wordRepository;
    private final DuplicateDetectionService duplicateDetectionService;
    private final ImportExportService importExportService;

    public WordDto createWord(CreateWordRequest request, User user) {
        log.info("Creating new word: {} for user: {}", request.getWord(), user.getUsername());

        // Check for duplicates using the duplicate detection service
        if (duplicateDetectionService.isDuplicateWord(request.getWord(), request.getLanguage())) {
            throw new DuplicateWordException(request.getWord(), request.getLanguage());
        }

        Word word = new Word();
        word.setWord(request.getWord().toLowerCase().trim());
        word.setLanguage(request.getLanguage());
        word.setDefinition(request.getDefinition());
        word.setPronunciation(request.getPronunciation());
        word.setExample(request.getExample());
        word.setDifficulty(request.getDifficulty());
        word.setCategories(request.getCategories());
        word.setTags(request.getTags());
        word.setCreatedBy(user);
        word.setIsPublic(request.getIsPublic());

        Word savedWord = wordRepository.save(word);
        log.info("Successfully created word with ID: {}", savedWord.getId());

        return mapToDto(savedWord);
    }

    @Transactional(readOnly = true)
    public Page<WordDto> searchWords(WordSearchCriteria criteria, User user, Pageable pageable) {
        log.debug("Searching words with criteria: {}", criteria);

        Specification<Word> spec = WordSpecifications.buildSpecification(criteria, user);
        Page<Word> words = wordRepository.findAll(spec, pageable);

        log.debug("Found {} words matching criteria", words.getTotalElements());
        return words.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public WordDto getWordById(Long wordId) {
        log.debug("Getting word by ID: {}", wordId);

        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", wordId));

        return mapToDto(word);
    }

    public WordDto updateWord(Long wordId, UpdateWordRequest request, User user) {
        log.info("Updating word with ID: {} by user: {}", wordId, user.getUsername());

        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", wordId));

        validateWordOwnership(word, user);

        if (request.getDefinition() != null) {
            word.setDefinition(request.getDefinition());
        }
        if (request.getPronunciation() != null) {
            word.setPronunciation(request.getPronunciation());
        }
        if (request.getExample() != null) {
            word.setExample(request.getExample());
        }
        if (request.getDifficulty() != null) {
            word.setDifficulty(request.getDifficulty());
        }
        if (request.getCategories() != null) {
            word.setCategories(request.getCategories());
        }
        if (request.getTags() != null) {
            word.setTags(request.getTags());
        }
        if (request.getIsPublic() != null) {
            word.setIsPublic(request.getIsPublic());
        }

        Word updatedWord = wordRepository.save(word);
        log.info("Successfully updated word with ID: {}", updatedWord.getId());

        return mapToDto(updatedWord);
    }

    public void deleteWord(Long wordId, User user) {
        log.info("Deleting word with ID: {} by user: {}", wordId, user.getUsername());

        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", wordId));

        validateWordOwnership(word, user);
        wordRepository.delete(word);

        log.info("Successfully deleted word with ID: {}", wordId);
    }

    public BulkImportResult bulkImportWords(org.springframework.web.multipart.MultipartFile file,
                                           com.memorizewords.dto.request.BulkImportOptions options, User user) {
        log.info("Starting bulk import for user: {} with options: {}", user.getUsername(), options);
        return importExportService.bulkImportWords(file, options, user);
    }

    public Resource exportWords(String format, Set<Long> wordIds, User user) {
        log.info("Exporting words in {} format for user: {}", format, user.getUsername());
        return importExportService.exportWords(format, wordIds, user);
    }

    public List<WordDto> getSimilarWords(String partialWord, String language, int limit) {
        log.debug("Finding similar words for: {} in language: {}", partialWord, language);
        return duplicateDetectionService.findSimilarWords(partialWord, language, limit)
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    public DuplicateDetectionService.DuplicateStats getDuplicateStats(User user) {
        log.debug("Getting duplicate stats for user: {}", user.getUsername());
        return duplicateDetectionService.getDuplicateStats(user);
    }

    private void validateWordOwnership(Word word, User user) {
        if (!word.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this word");
        }
    }

    private WordDto mapToDto(Word word) {
        WordDto dto = new WordDto();
        dto.setId(word.getId());
        dto.setWord(word.getWord());
        dto.setLanguage(word.getLanguage());
        dto.setDefinition(word.getDefinition());
        dto.setPronunciation(word.getPronunciation());
        dto.setExample(word.getExample());
        dto.setDifficulty(word.getDifficulty());
        dto.setCategories(word.getCategories());
        dto.setTags(word.getTags());
        dto.setIsPublic(word.getIsPublic());
        dto.setCreatedAt(word.getCreatedAt());
        dto.setUpdatedAt(word.getUpdatedAt());

        if (word.getCreatedBy() != null) {
            dto.setCreatedByUserId(word.getCreatedBy().getId());
            dto.setCreatedByUsername(word.getCreatedBy().getUsername());
        }

        return dto;
    }
}