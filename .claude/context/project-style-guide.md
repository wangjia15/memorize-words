---
created: 2025-09-23T02:01:30Z
last_updated: 2025-09-23T02:01:30Z
version: 1.0
author: Claude Code PM System
---

# Project Style Guide

## Introduction

This style guide provides comprehensive coding standards and conventions for the Memorize Words project. Following these guidelines ensures code consistency, maintainability, and quality across the entire codebase.

## Java Coding Standards

### Naming Conventions

#### Classes and Interfaces
- **Classes**: PascalCase (e.g., `WordService`, `VocabularyListController`)
- **Interfaces**: PascalCase, preferably adjectives (e.g., `Searchable`, `Cloneable`)
- **Abstract Classes**: PascalCase starting with Abstract (e.g., `AbstractBaseEntity`)
- **Exception Classes**: PascalCase ending with Exception (e.g., `DuplicateWordException`)

```java
// Good
public class WordService { }
public interface Searchable { }
public abstract class AbstractBaseEntity { }
public class DuplicateWordException extends RuntimeException { }

// Bad
public class wordService { }
public interface searchable { }
public abstract class baseEntity { }
public class DuplicateWord { }
```

#### Methods
- **Methods**: camelCase starting with verb (e.g., `createWord`, `findByUsername`)
- **Private Methods**: camelCase, can be more descriptive (e.g., `calculateSimilarityScore`)
- **Boolean Methods**: Start with is/has/should/can (e.g., `isValid`, `hasAccess`)

```java
// Good
public Word createWord(CreateWordRequest request) { }
public List<Word> findByUsername(String username) { }
private double calculateSimilarityScore(String word1, String word2) { }
public boolean isValid() { }
public boolean hasAccess() { }

// Bad
public Word CreateWord(CreateWordRequest request) { }
public List<Word> FindByUsername(String username) { }
private double calcSimScore(String word1, String word2) { }
public boolean valid() { }
public boolean access() { }
```

#### Variables
- **Instance Variables**: camelCase, descriptive names (e.g., `wordRepository`, `currentUser`)
- **Local Variables**: camelCase, concise but clear (e.g., `wordList`, `isValid`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_WORD_LENGTH`, `DEFAULT_PAGE_SIZE`)

```java
// Good
private final WordRepository wordRepository;
private User currentUser;
private List<Word> wordList;
private boolean isValid;
public static final int MAX_WORD_LENGTH = 100;
public static final String DEFAULT_PAGE_SIZE = "20";

// Bad
private final WordRepository wordRepo;
private User user;
private List<Word> words;
private boolean valid;
public static final int max = 100;
public static final String size = "20";
```

#### Packages
- **Package Names**: lowercase, reverse domain notation (e.g., `com.memorizewords.service`)
- **Subpackages**: logical grouping by functionality (e.g., `com.memorizewords.dto.request`)

```java
// Good
package com.memorizewords.service;
package com.memorizewords.dto.request;
package com.memorizewords.controller;

// Bad
package com.memorizeWords.Service;
package com.memorizewords.DtoRequest;
package com.memorizewords.controllers;
```

### Code Style and Formatting

#### Braces and Indentation
- **Braces**: K&R style (opening brace on same line)
- **Indentation**: 4 spaces, no tabs
- **Line Length**: Maximum 120 characters
- **Blank Lines**: Separate logical sections with blank lines

```java
// Good
public class WordService {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public Word createWord(CreateWordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Word word = new Word();
        word.setWord(request.getWord());
        return wordRepository.save(word);
    }
}

// Bad
public class WordService
{
    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository)
    {
        this.wordRepository = wordRepository;
    }

    public Word createWord(CreateWordRequest request)
    {
        if(request==null)
        {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Word word=new Word();
        word.setWord(request.getWord());
        return wordRepository.save(word);
    }
}
```

#### Method Organization
- **Order**: public → protected → private
- **Grouping**: Related methods together
- **Constructors**: First, then static methods, then instance methods
- **Getters/Setters**: At the end of class or use Lombok

```java
@Service
@Transactional
public class WordService {

    // Dependencies
    private final WordRepository wordRepository;

    // Constructor
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    // Public API methods
    public Word createWord(CreateWordRequest request) { }
    public Word updateWord(Long id, UpdateWordRequest request) { }
    public void deleteWord(Long id) { }

    // Private helper methods
    private void validateRequest(CreateWordRequest request) { }
    private Word mapToEntity(CreateWordRequest request) { }
    private WordDto mapToDto(Word word) { }
}
```

### Annotations and Documentation

#### Spring Annotations
- **Component Scanning**: Use appropriate stereotypes (@Service, @Repository, @Controller)
- **Transaction Management**: @Transactional at service level
- **Security**: @PreAuthorize for method-level security
- **Validation**: @Valid for request body validation

```java
// Good
@Service
@Transactional
@PreAuthorize("hasRole('USER')")
public class WordService {

    @Transactional(readOnly = true)
    public Page<WordDto> searchWords(WordSearchCriteria criteria, Pageable pageable) {
        // Implementation
    }

    public WordDto createWord(@Valid CreateWordRequest request) {
        // Implementation
    }
}

// Bad
@Service
public class WordService {

    public Page<WordDto> searchWords(WordSearchCriteria criteria, Pageable pageable) {
        // Missing @Transactional and @PreAuthorize
    }
}
```

#### JavaDoc Documentation
- **Public Methods**: Always include JavaDoc
- **Complex Logic**: Document non-obvious algorithms
- **Parameters and Return**: Document all parameters and return values
- **Exceptions**: Document thrown exceptions

```java
// Good
/**
 * Creates a new word in the vocabulary system.
 *
 * @param request the word creation request containing word details
 * @return the created word as a DTO
 * @throws DuplicateWordException if a word with the same name already exists
 * @throws IllegalArgumentException if the request is null or invalid
 */
public WordDto createWord(@Valid CreateWordRequest request) {
    // Implementation
}

// Bad
public WordDto createWord(CreateWordRequest request) {
    // No documentation
}
```

### Exception Handling

#### Custom Exceptions
- **Specific Exceptions**: Create domain-specific exceptions
- **Meaningful Messages**: Clear, actionable error messages
- **Proper Inheritance**: Extend appropriate exception classes

```java
// Good
public class DuplicateWordException extends RuntimeException {
    public DuplicateWordException(String message) {
        super(message);
    }
}

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}

// Bad
throw new RuntimeException("Duplicate word");
throw new Exception("Word not found");
```

#### Global Exception Handling
- **Controller Advice**: Centralized exception handling
- **HTTP Status Codes**: Use appropriate status codes
- **Error Response**: Consistent error response format

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateWordException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateWord(DuplicateWordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(), null));
    }
}
```

## Testing Standards

### Test Naming Conventions
- **Test Classes**: {ClassName}Test (e.g., `WordServiceTest`)
- **Test Methods**: descriptive camelCase explaining what is being tested
- **Test Organization**: Given-When-Then structure for readability

```java
// Good
class WordServiceTest {

    @Test
    void createWord_shouldCreateNewWord_whenRequestIsValid() {
        // Given
        CreateWordRequest request = createValidRequest();

        // When
        WordDto result = wordService.createWord(request, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWord()).isEqualTo(request.getWord());
    }

    @Test
    void createWord_shouldThrowException_whenWordAlreadyExists() {
        // Given
        CreateWordRequest request = createDuplicateRequest();

        // When & Then
        assertThatThrownBy(() -> wordService.createWord(request, user))
            .isInstanceOf(DuplicateWordException.class);
    }
}
```

### Test Structure
- **Arrange-Act-Assert**: Clear separation of setup, execution, and assertion
- **Test Data**: Use test builders or factories for object creation
- **Mocking**: Use Mockito for dependencies, mock only what's necessary

```java
@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    @Test
    void searchWords_shouldReturnFilteredResults_whenCriteriaProvided() {
        // Arrange
        WordSearchCriteria criteria = new WordSearchCriteria();
        criteria.setWord("test");

        User user = TestDataBuilder.createUser().build();
        Page<Word> expectedPage = new PageImpl<>(List.of(TestDataBuilder.createWord().build()));

        when(wordRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(expectedPage);

        // Act
        Page<WordDto> result = wordService.searchWords(criteria, user, Pageable.unpaged());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getWord()).isEqualTo("test");
    }
}
```

### Test Coverage
- **Target Coverage**: Minimum 80% code coverage
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Edge Cases**: Test boundary conditions and error scenarios

## Database and JPA Standards

### Entity Design
- **Base Entity**: Use `@MappedSuperclass` for common fields
- **Audit Fields**: Include created/updated timestamps and user tracking
- **Relationships**: Define appropriate fetch types and cascade operations
- **Validation**: Add Jakarta Bean Validation annotations

```java
// Good
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "words")
public class Word extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 50)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToMany(mappedBy = "words")
    private Set<VocabularyList> vocabularyLists = new HashSet<>();
}

// Bad
@Entity
public class Word {
    @Id
    private Long id;
    private String word; // Missing validation and length
    public User user; // Wrong annotation and naming
}
```

### Repository Pattern
- **Method Names**: Use Spring Data JPA naming conventions
- **Custom Queries**: Use @Query for complex queries with named parameters
- **Specifications**: Use JPA Specifications for dynamic queries
- **Pagination**: Return Page<T> for list operations

```java
// Good
@Repository
public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {

    List<Word> findByWordContainingIgnoreCase(String word);

    @Query("SELECT w FROM Word w WHERE w.createdBy = :user OR w.isPublic = true")
    Page<Word> findAccessibleWords(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(w) > 0 FROM Word w WHERE w.word = :word AND w.language = :language")
    boolean existsByWordAndLanguage(@Param("word") String word, @Param("language") String language);
}

// Bad
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    // Using custom query without proper parameter naming
    @Query("SELECT w FROM Word w WHERE w.word = ?1 AND w.language = ?2")
    List<Word> findByWordAndLanguage(String word, String language);
}
```

## API Design Standards

### REST API Conventions
- **URL Structure**: Use nouns, pluralize resource names
- **HTTP Methods**: Use appropriate methods for operations
- **Status Codes**: Use standard HTTP status codes
- **Response Format**: Consistent JSON response structure

```java
// Good
@RestController
@RequestMapping("/api/words")
@PreAuthorize("hasRole('USER')")
public class WordController {

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WordDto>>> searchWords(
            @ModelAttribute WordSearchCriteria criteria,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WordDto> words = wordService.searchWords(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Words retrieved successfully", words));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WordDto>> createWord(
            @Valid @RequestBody CreateWordRequest request) {
        WordDto word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Word created successfully", word));
    }
}

// Bad
@RestController
@RequestMapping("/word") // Singular resource name
public class WordController {

    @GetMapping("/getWords") // Verbs in URL
    public List<WordDto> getWords() { // No ResponseEntity wrapper
        // Implementation
    }
}
```

### DTO Design
- **Request DTOs**: Use for incoming data with validation
- **Response DTOs**: Use for outgoing data, control exposed fields
- **Validation**: Add Jakarta Bean Validation annotations
- **Immutability**: Make DTOs immutable where possible

```java
// Good
@Data
public class CreateWordRequest {
    @NotBlank(message = "Word cannot be blank")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String word;

    @NotBlank(message = "Language cannot be blank")
    @Size(min = 2, max = 50, message = "Language must be between 2 and 50 characters")
    private String language;

    @Size(max = 500, message = "Definition must be less than 500 characters")
    private String definition;
}

// Bad
public class CreateWordRequest {
    public String word; // No validation
    public String language;
    public String definition;
}
```

## Configuration and Properties

### Configuration Files
- **YAML Format**: Use YAML for better readability
- **Profile Separation**: Separate configurations for different environments
- **Externalized Configuration**: Externalize all configurable values
- **Sensitive Data**: Never commit secrets, use environment variables

```yaml
# application.yml
spring:
  profiles:
    active: dev
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:password}

---
# application-dev.yml
spring:
  h2:
    console:
      enabled: true
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

---
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/memorize_words
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

## Logging Standards

### Logging Practices
- **Framework**: Use SLF4J with Logback
- **Logger Naming**: Use class name for logger
- **Log Levels**: Use appropriate levels (ERROR, WARN, INFO, DEBUG, TRACE)
- **Structured Logging**: Include contextual information

```java
// Good
@Service
@Slf4j
public class WordService {

    public WordDto createWord(CreateWordRequest request, User user) {
        log.info("Creating new word: {} for user: {}", request.getWord(), user.getUsername());

        try {
            Word word = createWordEntity(request, user);
            Word savedWord = wordRepository.save(word);
            log.debug("Word created successfully with ID: {}", savedWord.getId());
            return mapToDto(savedWord);
        } catch (DataAccessException ex) {
            log.error("Database error while creating word: {}", request.getWord(), ex);
            throw new DataAccessException("Failed to create word", ex);
        }
    }
}

// Bad
@Service
public class WordService {
    private static final Logger logger = LoggerFactory.getLogger(WordService.class);

    public WordDto createWord(CreateWordRequest request, User user) {
        System.out.println("Creating word: " + request.getWord()); // Don't use System.out
        // Implementation
    }
}
```

## Code Quality Tools

### Static Analysis
- **SpotBugs**: Static code analysis for bug detection
- **PMD**: Code quality and style checking
- **Checkstyle**: Code style enforcement
- **SonarQube**: Comprehensive code quality platform

### Code Reviews
- **Checklist**: Use standardized review checklist
- **Automated Checks**: Run automated checks before manual review
- **Focus Areas**: Security, performance, maintainability, readability
- **Feedback**: Provide constructive, specific feedback

## Documentation Standards

### Code Comments
- **Public APIs**: Comprehensive JavaDoc for all public methods
- **Complex Logic**: Explain non-obvious algorithms
- **TODO Comments**: Mark temporary solutions with TODO
- **Deprecated Code**: Mark deprecated methods with @Deprecated

```java
/**
 * Service for managing word vocabulary operations including creation,
 * updating, deletion, and search functionality.
 *
 * @author Development Team
 * @since 1.0
 */
@Service
@Transactional
@Slf4j
public class WordService {

    /**
     * Creates a new word in the vocabulary system.
     *
     * @param request the word creation request containing word details
     * @param user the user creating the word
     * @return the created word as a DTO
     * @throws DuplicateWordException if a word with the same name already exists
     * @throws IllegalArgumentException if the request is null or invalid
     */
    public WordDto createWord(@Valid CreateWordRequest request, User user) {
        log.info("Creating new word: {} for user: {}", request.getWord(), user.getUsername());

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Check for duplicates using sophisticated algorithm
        // TODO: Improve duplicate detection algorithm in next sprint
        if (wordRepository.existsByWordAndLanguage(request.getWord(), request.getLanguage())) {
            throw new DuplicateWordException("Word already exists");
        }

        return createWordInternal(request, user);
    }
}
```

## Version Control Standards

### Git Workflow
- **Branching Strategy**: Feature branches from main/development
- **Commit Messages**: Clear, descriptive messages with conventional format
- **Pull Requests**: Use PRs for code review and integration
- **Branch Naming**: Use descriptive branch names (feature/word-management)

```bash
# Good commit messages
feat: implement word search functionality
fix: resolve duplicate word detection bug
docs: update API documentation for word endpoints
test: add unit tests for word service
refactor: improve word repository query performance

# Bad commit messages
fixed some bugs
update
work in progress
wip
```

## Performance Guidelines

### Database Optimization
- **Query Optimization**: Use appropriate indexes and query optimization
- **Lazy Loading**: Use FetchType.LAZY for large collections
- **Batch Operations**: Use batch operations for bulk processing
- **Connection Pooling**: Configure appropriate connection pool settings

### Memory Management
- **Object Creation**: Minimize unnecessary object creation
- **String Processing**: Use StringBuilder for complex string operations
- **Collections**: Use appropriate collection types for use cases
- **Resource Management**: Use try-with-resources for resource cleanup

```java
// Good
public List<Word> findWordsByCriteria(WordSearchCriteria criteria) {
    Specification<Word> spec = WordSpecifications.buildSpecification(criteria);
    return wordRepository.findAll(spec); // Returns only what's needed
}

// Bad
public List<Word> findWordsByCriteria(WordSearchCriteria criteria) {
    List<Word> allWords = wordRepository.findAll(); // Loads all words
    return allWords.stream()
        .filter(word -> matchesCriteria(word, criteria))
        .collect(Collectors.toList()); // Inefficient filtering
}
```

This style guide should be followed by all contributors to the Memorize Words project to ensure code quality, consistency, and maintainability throughout the development lifecycle.