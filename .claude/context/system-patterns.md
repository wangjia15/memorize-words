---
created: 2025-09-23T02:01:30Z
last_updated: 2025-09-23T02:01:30Z
version: 1.0
author: Claude Code PM System
---

# System Patterns

## Architectural Patterns

### Layered Architecture
**Implementation**: Full implementation across all components

**Pattern Structure**:
```
Controller Layer (HTTP Handling)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity Layer (Domain Models)
```

**Key Benefits**:
- Clear separation of concerns
- Testable components in isolation
- Scalable and maintainable codebase
- Consistent request processing flow

**Current Implementation**:
- ✅ Controllers: `WordController`, `VocabularyListController`, `HealthController`
- ✅ Services: `WordService`, `VocabularyListService`, `ImportExportService`, `DuplicateDetectionService`
- ✅ Repositories: `WordRepository`, `VocabularyListRepository`, `UserRepository`, `UserWordProgressRepository`
- ✅ Entities: `Word`, `VocabularyList`, `User`, `UserWordProgress`, `BaseEntity`

### Repository Pattern
**Implementation**: Complete with Spring Data JPA

**Pattern Structure**:
```java
@Repository
public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {
    // Custom query methods
    List<Word> findByWordContainingIgnoreCase(String word);

    @Query("SELECT w FROM Word w WHERE w.createdBy = :user OR w.isPublic = true")
    Page<Word> findAccessibleWords(@Param("user") User user, Pageable pageable);
}
```

**Key Features**:
- Automatic CRUD operations from Spring Data JPA
- Custom query methods with derived queries
- JPA Specifications for dynamic queries
- Pagination and sorting support
- Type-safe query construction

### Data Transfer Object (DTO) Pattern
**Implementation**: Comprehensive DTO structure

**Pattern Categories**:
- **Request DTOs**: `CreateWordRequest`, `UpdateWordRequest`, `CreateListRequest`
- **Response DTOs**: `WordDto`, `VocabularyListDto`, `ApiResponse`
- **Search DTOs**: `WordSearchCriteria`, `BulkImportOptions`

**Benefits**:
- Separates API contracts from domain models
- Prevents over-fetching of data
- Enables input validation
- Supports API versioning

### Specification Pattern
**Implementation**: Dynamic query building with JPA Specifications

**Pattern Implementation**:
```java
public class WordSpecifications {
    public static Specification<Word> buildSpecification(WordSearchCriteria criteria, User user) {
        return Specification.where(hasAccess(user))
            .and(containsWord(criteria.getWord()))
            .and(hasLanguage(criteria.getLanguage()))
            .and(hasDifficulty(criteria.getDifficulty()))
            .and(inCategories(criteria.getCategories()));
    }
}
```

**Use Cases**:
- Dynamic search with multiple criteria
- Complex query conditions
- Reusable query components
- Type-safe query construction

## Design Patterns

### Service Pattern
**Implementation**: Business logic encapsulation

**Pattern Structure**:
```java
@Service
@Transactional
public class WordService {
    private final WordRepository wordRepository;
    private final DuplicateDetectionService duplicateDetectionService;

    @Transactional(readOnly = true)
    public Page<WordDto> searchWords(WordSearchCriteria criteria, User user, Pageable pageable) {
        Specification<Word> spec = WordSpecifications.buildSpecification(criteria, user);
        Page<Word> words = wordRepository.findAll(spec, pageable);
        return words.map(this::mapToDto);
    }
}
```

**Characteristics**:
- Transaction management at service level
- Business logic orchestration
- Dependency injection of repositories and other services
- Clear public API for controllers

### Factory Pattern
**Implementation**: Test data generation and DTO mapping

**Example Implementation**:
```java
@Component
public class TestDataBuilder {
    public Word createTestWord(String word, String language, User user) {
        Word wordEntity = new Word();
        wordEntity.setWord(word);
        wordEntity.setLanguage(language);
        wordEntity.setCreatedBy(user);
        // ... additional setup
        return wordEntity;
    }
}
```

### Strategy Pattern
**Implementation**: Pluggable algorithms for different operations

**Current Usage**:
- **Import Strategies**: Different formats (CSV, JSON) in `ImportExportService`
- **Search Strategies**: Dynamic query building with specifications
- **Validation Strategies**: Different validation rules for different operations

### Observer Pattern
**Implementation**: Event-driven architecture (planned)

**Future Implementation**:
- **Word Creation Events**: Trigger notifications and analytics
- **Learning Progress Events**: Update user statistics
- **System Events**: Logging and monitoring

## Data Access Patterns

### Active Record Pattern
**Implementation**: JPA entities with business methods

**Entity Structure**:
```java
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Word extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String word;

    @ManyToMany(mappedBy = "words")
    private Set<VocabularyList> vocabularyLists = new HashSet<>();

    // Business methods can be added here
    public void addToList(VocabularyList list) {
        this.vocabularyLists.add(list);
        list.getWords().add(this);
    }
}
```

### Unit of Work Pattern
**Implementation**: Spring transaction management

**Pattern Benefits**:
- Automatic transaction management
- Consistent state across multiple operations
- Rollback capabilities on failures
- Declarative transaction boundaries

## API Design Patterns

### REST API Patterns
**Implementation**: Full REST compliance

**API Endpoints Structure**:
```
GET    /api/words                    # List words with search/filter
POST   /api/words                    # Create new word
GET    /api/words/{id}               # Get specific word
PUT    /api/words/{id}               # Update word
DELETE /api/words/{id}               # Delete word
POST   /api/words/bulk-import        # Bulk import words
GET    /api/words/export             # Export words
```

### API Response Pattern
**Implementation**: Standardized response wrapper

**Response Structure**:
```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ApiError> errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, List<ApiError> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
```

### Pagination Pattern
**Implementation**: Spring Data pagination

**Pattern Usage**:
```java
@GetMapping("/api/words")
public ResponseEntity<ApiResponse<Page<WordDto>>> searchWords(
        @ModelAttribute WordSearchCriteria criteria,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    // Implementation
}
```

## Error Handling Patterns

### Global Exception Handler Pattern
**Implementation**: Centralized error processing

**Handler Structure**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateWordException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateWord(DuplicateWordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), null));
    }
}
```

### Custom Exception Pattern
**Implementation**: Domain-specific exceptions

**Exception Types**:
- `ResourceNotFoundException`: Missing resources
- `DuplicateWordException`: Duplicate data
- `ImportException`: Import/export failures
- `AccessDeniedException`: Authorization failures

## Testing Patterns

### Test Slice Pattern
**Implementation**: Focused testing with Spring Boot Test

**Test Categories**:
- **@WebMvcTest**: Controller layer testing
- **@DataJpaTest**: Repository layer testing
- **@ServiceTest**: Service layer testing
- **@SpringBootTest**: Full integration testing

### Test Builder Pattern
**Implementation**: Fluent test data creation

**Builder Usage**:
```java
@Test
public void testCreateWord() {
    User user = testDataBuilder.createUser()
        .withUsername("testuser")
        .withEmail("test@example.com")
        .build();

    Word word = testDataBuilder.createWord()
        .withWord("test")
        .withLanguage("en")
        .withCreatedBy(user)
        .build();
}
```

### Mock Pattern
**Implementation**: Mockito for isolated testing

**Mock Usage**:
```java
@ExtendWith(MockitoExtension.class)
class WordServiceTest {
    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    @Test
    void testCreateWord() {
        // Given
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        // When
        WordDto result = wordService.createWord(request, user);

        // Then
        assertThat(result).isNotNull();
        verify(wordRepository).save(any(Word.class));
    }
}
```

## Security Patterns

### Role-Based Access Control (RBAC)
**Implementation**: Method-level security

**Security Configuration**:
```java
@RestController
@RequestMapping("/api/words")
@PreAuthorize("hasRole('USER')")
public class WordController {

    @PreAuthorize("hasRole('ADMIN') or @wordSecurityService.isOwner(#id, authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordDto>> updateWord(@PathVariable Long id) {
        // Implementation
    }
}
```

### JWT Authentication Pattern
**Implementation**: Token-based authentication (from GitHub issue #4)

**Pattern Components**:
- **Token Generation**: JWT creation with user claims
- **Token Validation**: Token verification and parsing
- **Security Context**: Authentication context setup
- **Authorization**: Role-based access control

## Configuration Patterns

### Externalized Configuration Pattern
**Implementation**: Environment-specific configuration files

**Configuration Structure**:
```yaml
# application.yml
spring:
  profiles:
    active: dev

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  h2:
    console:
      enabled: true

---
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/memorize_words
```

### Profile-based Configuration
**Implementation**: Environment-specific settings

**Profile Types**:
- **dev**: Development with H2 database and debug logging
- **test**: Testing with in-memory database
- **prod**: Production with MySQL and optimized settings

## Future Pattern Considerations

### CQRS Pattern (Command Query Responsibility Segregation)
**Potential Use**: Separating read and write operations for complex queries

### Event Sourcing Pattern
**Potential Use**: Audit trail for learning progress and word changes

### Circuit Breaker Pattern
**Potential Use**: External service integration resilience

### Saga Pattern
**Potential Use**: Complex business transactions across multiple services

### API Gateway Pattern
**Potential Use**: Microservices architecture scaling

## Pattern Documentation and Standards

### Naming Conventions
- **Classes**: PascalCase (e.g., `WordService`)
- **Methods**: camelCase (e.g., `createWord`)
- **Variables**: camelCase (e.g., `wordRepository`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_WORD_LENGTH`)

### Code Organization Standards
- **Package Structure**: Logical grouping by functionality
- **Class Responsibilities**: Single Responsibility Principle
- **Method Length**: Keep methods focused and concise
- **Documentation**: Comprehensive JavaDoc for public APIs

### Pattern Selection Guidelines
- **Simplicity**: Choose the simplest pattern that solves the problem
- **Consistency**: Maintain consistent patterns across the codebase
- **Testability**: Ensure patterns support easy testing
- **Performance**: Consider performance implications of pattern choices
- **Maintainability**: Choose patterns that make code easy to understand and modify