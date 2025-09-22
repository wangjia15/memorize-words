package com.memorizewords.service;

import com.memorizewords.dto.request.CreateListRequest;
import com.memorizewords.dto.response.VocabularyListDto;
import com.memorizewords.dto.response.WordSummaryDto;
import com.memorizewords.entity.User;
import com.memorizewords.entity.VocabularyList;
import com.memorizewords.entity.Word;
import com.memorizewords.exception.AccessDeniedException;
import com.memorizewords.exception.ResourceNotFoundException;
import com.memorizewords.repository.VocabularyListRepository;
import com.memorizewords.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for vocabulary list management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VocabularyListService {

    private final VocabularyListRepository listRepository;
    private final WordRepository wordRepository;

    public VocabularyListDto createList(CreateListRequest request, User user) {
        log.info("Creating new vocabulary list: {} for user: {}", request.getName(), user.getUsername());

        // Check for duplicate list name
        if (listRepository.existsByOwnerAndName(user, request.getName())) {
            throw new IllegalArgumentException("Vocabulary list with name '" + request.getName() + "' already exists");
        }

        VocabularyList list = new VocabularyList();
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        list.setOwner(user);
        list.setIsPublic(request.getIsPublic());
        list.setType(request.getType());

        VocabularyList savedList = listRepository.save(list);
        log.info("Successfully created vocabulary list with ID: {}", savedList.getId());

        return mapToDto(savedList);
    }

    @Transactional(readOnly = true)
    public VocabularyListDto getListById(Long listId, User user) {
        log.debug("Getting vocabulary list by ID: {}", listId);

        VocabularyList list = listRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("VocabularyList", "id", listId));

        // Check if user has access to this list
        if (!list.getOwner().getId().equals(user.getId()) && !list.getIsPublic() && !list.getIsShared()) {
            throw new AccessDeniedException("You don't have permission to access this list");
        }

        return mapToDto(list);
    }

    @Transactional(readOnly = true)
    public List<VocabularyListDto> getUserLists(User user) {
        log.debug("Getting vocabulary lists for user: {}", user.getUsername());

        List<VocabularyList> lists = listRepository.findByOwnerOrderByCreatedAtDesc(user);
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyListDto> getPublicLists() {
        log.debug("Getting public vocabulary lists");

        List<VocabularyList> lists = listRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyListDto> getAccessibleLists(User user) {
        log.debug("Getting accessible vocabulary lists for user: {}", user.getUsername());

        List<VocabularyList> lists = listRepository.findAccessibleLists(user);
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public VocabularyListDto addWordsToList(Long listId, java.util.Set<Long> wordIds, User user) {
        log.info("Adding {} words to list {} by user: {}", wordIds.size(), listId, user.getUsername());

        VocabularyList list = getListWithPermissionCheck(listId, user);
        java.util.Set<Word> wordsToAdd = new java.util.HashSet<>(wordRepository.findAllById(wordIds));

        list.getWords().addAll(wordsToAdd);
        list.setWordCount(list.getWords().size());

        VocabularyList updatedList = listRepository.save(list);
        log.info("Successfully added words to list. New word count: {}", updatedList.getWordCount());

        return mapToDto(updatedList);
    }

    public VocabularyListDto removeWordsFromList(Long listId, java.util.Set<Long> wordIds, User user) {
        log.info("Removing {} words from list {} by user: {}", wordIds.size(), listId, user.getUsername());

        VocabularyList list = getListWithPermissionCheck(listId, user);
        java.util.Set<Word> wordsToRemove = new java.util.HashSet<>(wordRepository.findAllById(wordIds));

        list.getWords().removeAll(wordsToRemove);
        list.setWordCount(list.getWords().size());

        VocabularyList updatedList = listRepository.save(list);
        log.info("Successfully removed words from list. New word count: {}", updatedList.getWordCount());

        return mapToDto(updatedList);
    }

    public void shareList(Long listId, User user) {
        log.info("Sharing list {} by user: {}", listId, user.getUsername());

        VocabularyList list = getListWithPermissionCheck(listId, user);
        list.setIsShared(true);
        listRepository.save(list);

        log.info("Successfully shared list: {}", listId);
    }

    public void deleteList(Long listId, User user) {
        log.info("Deleting list {} by user: {}", listId, user.getUsername());

        VocabularyList list = getListWithPermissionCheck(listId, user);
        listRepository.delete(list);

        log.info("Successfully deleted list: {}", listId);
    }

    private VocabularyList getListWithPermissionCheck(Long listId, User user) {
        VocabularyList list = listRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("VocabularyList", "id", listId));

        if (!list.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this list");
        }

        return list;
    }

    private VocabularyListDto mapToDto(VocabularyList list) {
        VocabularyListDto dto = new VocabularyListDto();
        dto.setId(list.getId());
        dto.setName(list.getName());
        dto.setDescription(list.getDescription());
        dto.setOwnerId(list.getOwner().getId());
        dto.setOwnerUsername(list.getOwner().getUsername());
        dto.setIsPublic(list.getIsPublic());
        dto.setIsShared(list.getIsShared());
        dto.setTags(list.getTags());
        dto.setType(list.getType());
        dto.setWordCount(list.getWordCount());
        dto.setCreatedAt(list.getCreatedAt());
        dto.setUpdatedAt(list.getUpdatedAt());

        if (list.getWords() != null) {
            dto.setWords(list.getWords().stream()
                .map(this::mapToWordSummary)
                .collect(java.util.stream.Collectors.toSet()));
        }

        return dto;
    }

    private WordSummaryDto mapToWordSummary(Word word) {
        WordSummaryDto dto = new WordSummaryDto();
        dto.setId(word.getId());
        dto.setWord(word.getWord());
        dto.setLanguage(word.getLanguage());
        dto.setDifficulty(word.getDifficulty());
        return dto;
    }
}