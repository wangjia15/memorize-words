# Task 005-GITHUB: Vocabulary Management System (GitHub Issue #5)

## Metadata
```yaml
epic: memorize-words
task_number: 005-github
title: Vocabulary Management System (GitHub Issue #5)
status: completed
priority: high
effort_estimate: 12
parallel: true
dependencies: [003]
created: 2025-09-23T00:00:00Z
updated: 2025-09-23T05:51:12Z
assignee: developer
github: https://github.com/wangjia15/memorize-words/issues/5
tags: [vocabulary, crud, word-management, custom-lists, spring-data, github-issue]
```

## Summary
Develop a comprehensive vocabulary management system enabling users to perform CRUD operations on words, create custom vocabulary lists, import/export word collections, and manage word metadata including definitions, pronunciations, and learning progress tracking. This task corresponds to GitHub issue #5.

## Acceptance Criteria
- [ ] Word CRUD operations (Create, Read, Update, Delete)
- [ ] Custom vocabulary list creation and management
- [ ] Word search and filtering capabilities
- [ ] Bulk import/export functionality (CSV, JSON)
- [ ] Word metadata management (definitions, pronunciations, examples)
- [ ] Learning progress tracking per word
- [ ] Word categorization and tagging system
- [ ] Duplicate word detection and prevention
- [ ] Word difficulty level assignment
- [ ] User-specific word collections
- [ ] Sharing capabilities for custom lists
- [ ] Word statistics and analytics

## Technical Requirements

### Word Entity and Repository
```java
@Entity
@Table(name = "words")
@Data
@EqualsAndHashCode(callSuper = true)
public class Word extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 50)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(length = 200)
    private String pronunciation;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<WordCategory> categories = new HashSet<>();

    @ElementCollection
    private Set<String> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserWordProgress> userProgress = new ArrayList<>();

    @ManyToMany(mappedBy = "words")
    private Set<VocabularyList> vocabularyLists = new HashSet<>();
}

@Repository
public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {

    List<Word> findByWordContainingIgnoreCase(String word);

    List<Word> findByCreatedByAndIsPublicTrue(User user);

    List<Word> findByLanguageAndDifficulty(String language, DifficultyLevel difficulty);

    @Query("SELECT w FROM Word w WHERE w.word = :word AND w.language = :language")
    Optional<Word> findByWordAndLanguage(@Param("word") String word, @Param("language") String language);

    @Query("SELECT w FROM Word w JOIN w.categories c WHERE c IN :categories")
    List<Word> findByCategories(@Param("categories") Set<WordCategory> categories);

    @Query("SELECT w FROM Word w WHERE w.createdBy = :user OR w.isPublic = true")
    Page<Word> findAccessibleWords(@Param("user") User user, Pageable pageable);
}
```

### Vocabulary List Entity
```java
@Entity
@Table(name = "vocabulary_lists")
@Data
@EqualsAndHashCode(callSuper = true)
public class VocabularyList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vocabulary_list_words",
        joinColumns = @JoinColumn(name = "list_id"),
        inverseJoinColumns = @JoinColumn(name = "word_id")
    )
    private Set<Word> words = new HashSet<>();

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "is_shared")
    private Boolean isShared = false;

    @ElementCollection
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ListType type = ListType.CUSTOM;

    @Column(name = "word_count")
    private Integer wordCount = 0;
}

@Repository
public interface VocabularyListRepository extends JpaRepository<VocabularyList, Long> {

    List<VocabularyList> findByOwnerOrderByCreatedAtDesc(User owner);

    List<VocabularyList> findByIsPublicTrueOrderByCreatedAtDesc();

    List<VocabularyList> findByOwnerAndNameContainingIgnoreCase(User owner, String name);

    @Query("SELECT vl FROM VocabularyList vl WHERE vl.owner = :user OR vl.isShared = true")
    List<VocabularyList> findAccessibleLists(@Param("user") User user);
}
```

### Word Service
```java
@Service
@Transactional
public class WordService {

    private final WordRepository wordRepository;
    private final VocabularyListRepository vocabularyListRepository;
    private final UserWordProgressRepository progressRepository;
    private final DuplicateDetectionService duplicateDetectionService;

    public WordDto createWord(CreateWordRequest request, User user) {
        // Check for duplicates
        Optional<Word> existingWord = wordRepository.findByWordAndLanguage(
            request.getWord(), request.getLanguage());

        if (existingWord.isPresent()) {
            throw new DuplicateWordException("Word already exists in the specified language");
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
        return mapToDto(savedWord);
    }

    @Transactional(readOnly = true)
    public Page<WordDto> searchWords(WordSearchCriteria criteria, User user, Pageable pageable) {
        Specification<Word> spec = WordSpecifications.buildSpecification(criteria, user);
        Page<Word> words = wordRepository.findAll(spec, pageable);
        return words.map(this::mapToDto);
    }

    public WordDto updateWord(Long wordId, UpdateWordRequest request, User user) {
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", wordId));

        validateWordOwnership(word, user);

        word.setDefinition(request.getDefinition());
        word.setPronunciation(request.getPronunciation());
        word.setExample(request.getExample());
        word.setDifficulty(request.getDifficulty());
        word.setCategories(request.getCategories());
        word.setTags(request.getTags());

        Word updatedWord = wordRepository.save(word);
        return mapToDto(updatedWord);
    }

    public void deleteWord(Long wordId, User user) {
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word", "id", wordId));

        validateWordOwnership(word, user);
        wordRepository.delete(word);
    }

    public BulkImportResult bulkImportWords(MultipartFile file, BulkImportOptions options, User user) {
        try {
            List<WordImportDto> importWords = parseImportFile(file, options.getFormat());
            BulkImportResult result = new BulkImportResult();

            for (WordImportDto importWord : importWords) {
                try {
                    CreateWordRequest request = mapImportToRequest(importWord);
                    WordDto created = createWord(request, user);
                    result.addSuccess(created);
                } catch (DuplicateWordException e) {
                    if (options.isSkipDuplicates()) {
                        result.addSkipped(importWord.getWord(), "Duplicate word");
                    } else {
                        result.addError(importWord.getWord(), e.getMessage());
                    }
                } catch (Exception e) {
                    result.addError(importWord.getWord(), e.getMessage());
                }
            }

            return result;
        } catch (Exception e) {
            throw new ImportException("Failed to import words: " + e.getMessage());
        }
    }

    private void validateWordOwnership(Word word, User user) {
        if (!word.getCreatedBy().getId().equals(user.getId()) && !user.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to modify this word");
        }
    }
}
```

### Vocabulary List Service
```java
@Service
@Transactional
public class VocabularyListService {

    private final VocabularyListRepository listRepository;
    private final WordRepository wordRepository;

    public VocabularyListDto createList(CreateListRequest request, User user) {
        VocabularyList list = new VocabularyList();
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        list.setOwner(user);
        list.setIsPublic(request.getIsPublic());
        list.setTags(request.getTags());
        list.setType(request.getType());

        VocabularyList savedList = listRepository.save(list);
        return mapToDto(savedList);
    }

    public VocabularyListDto addWordsToList(Long listId, Set<Long> wordIds, User user) {
        VocabularyList list = getListWithPermissionCheck(listId, user);
        Set<Word> wordsToAdd = new HashSet<>(wordRepository.findAllById(wordIds));

        list.getWords().addAll(wordsToAdd);
        list.setWordCount(list.getWords().size());

        VocabularyList updatedList = listRepository.save(list);
        return mapToDto(updatedList);
    }

    public VocabularyListDto removeWordsFromList(Long listId, Set<Long> wordIds, User user) {
        VocabularyList list = getListWithPermissionCheck(listId, user);
        Set<Word> wordsToRemove = new HashSet<>(wordRepository.findAllById(wordIds));

        list.getWords().removeAll(wordsToRemove);
        list.setWordCount(list.getWords().size());

        VocabularyList updatedList = listRepository.save(list);
        return mapToDto(updatedList);
    }

    @Transactional(readOnly = true)
    public List<VocabularyListDto> getUserLists(User user) {
        List<VocabularyList> lists = listRepository.findByOwnerOrderByCreatedAtDesc(user);
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyListDto> getPublicLists() {
        List<VocabularyList> lists = listRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public void shareList(Long listId, User user) {
        VocabularyList list = getListWithPermissionCheck(listId, user);
        list.setIsShared(true);
        listRepository.save(list);
    }

    private VocabularyList getListWithPermissionCheck(Long listId, User user) {
        VocabularyList list = listRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("VocabularyList", "id", listId));

        if (!list.getOwner().getId().equals(user.getId()) && !user.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to modify this list");
        }

        return list;
    }
}
```

### REST Controllers
```java
@RestController
@RequestMapping("/api/words")
@PreAuthorize("hasRole('USER')")
public class WordController {

    private final WordService wordService;

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
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        Page<WordDto> words = wordService.searchWords(criteria, user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Words retrieved successfully", words));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WordDto>> getWord(@PathVariable Long id) {
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
        return ResponseEntity.ok(ApiResponse.success("Word deleted successfully"));
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<ApiResponse<BulkImportResult>> bulkImport(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute BulkImportOptions options,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        BulkImportResult result = wordService.bulkImportWords(file, options, user);
        return ResponseEntity.ok(ApiResponse.success("Bulk import completed", result));
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportWords(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Set<Long> wordIds,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        Resource resource = wordService.exportWords(format, wordIds, user);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=words." + format)
            .body(resource);
    }
}

@RestController
@RequestMapping("/api/vocabulary-lists")
@PreAuthorize("hasRole('USER')")
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

    @PostMapping("/{id}/words")
    public ResponseEntity<ApiResponse<VocabularyListDto>> addWords(
            @PathVariable Long id,
            @RequestBody AddWordsRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.addWordsToList(id, request.getWordIds(), user);
        return ResponseEntity.ok(ApiResponse.success("Words added to list successfully", list));
    }

    @DeleteMapping("/{id}/words")
    public ResponseEntity<ApiResponse<VocabularyListDto>> removeWords(
            @PathVariable Long id,
            @RequestBody RemoveWordsRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        VocabularyListDto list = listService.removeWordsFromList(id, request.getWordIds(), user);
        return ResponseEntity.ok(ApiResponse.success("Words removed from list successfully", list));
    }
}
```

## Implementation Details

### Key Features
1. **Word Management**: Complete CRUD operations with validation and duplicate detection
2. **Custom Lists**: User-created vocabulary collections with sharing capabilities
3. **Search & Filter**: Advanced search with multiple criteria and sorting options
4. **Import/Export**: Support for CSV and JSON formats with bulk operations
5. **Access Control**: User-specific content with public sharing options
6. **Progress Tracking**: Integration with learning progress for each word

### Data Integrity
- Unique constraints on word-language combinations
- Cascade operations for list-word relationships
- Soft delete options for important data
- Audit trail through BaseEntity

### Performance Optimizations
- Lazy loading for large collections
- Pagination for search results
- Database indexes on frequently queried fields
- Batch operations for bulk imports

## Testing Strategy
- Unit tests for service layer business logic
- Integration tests for repository queries
- Controller tests for API endpoints
- Performance tests for bulk operations
- Import/export functionality tests
- Access control and security tests

## Definition of Done
- [ ] All CRUD operations functional and tested
- [ ] Custom vocabulary lists working correctly
- [ ] Search and filtering capabilities implemented
- [ ] Bulk import/export functionality working
- [ ] Access control properly enforced
- [ ] Performance requirements met
- [ ] All tests passing with good coverage
- [ ] API documentation complete
- [ ] Database migrations created and tested

## Notes
- Implement caching for frequently accessed words
- Consider search optimization with Elasticsearch for large datasets
- Plan for internationalization support
- Implement word validation services for definition accuracy