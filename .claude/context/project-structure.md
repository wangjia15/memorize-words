---
created: 2025-09-23T02:01:30Z
last_updated: 2025-09-23T02:01:30Z
version: 1.0
author: Claude Code PM System
---

# Project Structure

## Directory Organization

### Root Level Structure
```
memorize-words/
├── .claude/                    # Project management and AI agent context
├── .git/                       # Git version control
├── .gitignore                  # Git ignore rules
├── AGENTS.md                   # AI agent documentation
├── COMMANDS.md                 # Custom command documentation
├── LICENSE                     # MIT license file
├── logs/                       # Application logs directory
├── pom.xml                     # Maven build configuration
├── README.md                   # Project documentation
├── screenshot.webp            # Application screenshot
├── src/                        # Source code directory
├── target/                     # Maven build output
└── frontend/                   # React frontend application
```

### Backend Structure (Spring Boot)

#### Main Source Code
```
src/main/java/com/memorizewords/
├── MemorizeWordsApplication.java    # Main Spring Boot application class
├── config/                          # Configuration classes
│   ├── WebConfig.java              # Web configuration (CORS, etc.)
│   └── [Future configs]            # Security, database, caching configs
├── controller/                      # REST API controllers
│   ├── HealthController.java       # Health check endpoints
│   ├── VocabularyListController.java # Vocabulary list management
│   └── WordController.java         # Word management endpoints
├── dto/                            # Data Transfer Objects
│   ├── request/                    # Request DTOs
│   │   ├── AddWordsRequest.java
│   │   ├── BulkImportOptions.java
│   │   ├── CreateListRequest.java
│   │   ├── CreateWordRequest.java
│   │   ├── RemoveWordsRequest.java
│   │   └── UpdateWordRequest.java
│   └── response/                   # Response DTOs
│       ├── ApiResponse.java
│       ├── VocabularyListDto.java
│       └── WordDto.java
├── entity/                         # JPA entities
│   ├── BaseEntity.java           # Base entity with audit fields
│   ├── User.java                  # User entity
│   ├── UserWordProgress.java      # User learning progress
│   ├── VocabularyList.java        # Vocabulary list entity
│   └── Word.java                  # Word entity
├── enums/                          # Enum definitions
│   ├── DifficultyLevel.java       # Word difficulty levels
│   ├── LearningStatus.java        # Learning progress status
│   ├── ListType.java             # Vocabulary list types
│   ├── UserRole.java             # User role definitions
│   └── WordCategory.java         # Word categorization
├── exception/                      # Custom exception classes
│   ├── AccessDeniedException.java
│   ├── DuplicateWordException.java
│   ├── ImportException.java
│   └── ResourceNotFoundException.java
├── repository/                     # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── UserWordProgressRepository.java
│   ├── VocabularyListRepository.java
│   └── WordRepository.java
├── service/                        # Business logic services
│   ├── DuplicateDetectionService.java # Duplicate word detection
│   ├── ImportExportService.java     # Bulk import/export operations
│   ├── VocabularyListService.java  # List management logic
│   └── WordService.java            # Word management logic
└── specification/                  # JPA Specification queries
    └── WordSpecifications.java     # Dynamic query building
```

#### Test Structure
```
src/test/java/com/memorizewords/
├── config/                          # Test configuration
│   └── TestConfig.java              # Test context configuration
├── integration/                     # Integration tests
│   ├── ApplicationStartupIntegrationTest.java
│   └── HealthControllerIntegrationTest.java
├── service/                         # Service layer tests
│   ├── DuplicateDetectionServiceTest.java
│   ├── ImportExportServiceTest.java
│   ├── VocabularyListServiceTest.java
│   └── WordServiceTest.java
├── unit/                           # Unit tests
│   ├── ConfigTest.java
│   └── MemorizeWordsApplicationTest.java
└── util/                           # Test utilities
    ├── EntityTestUtil.java         # Entity testing utilities
    ├── JsonTestUtil.java           # JSON testing utilities
    ├── SecurityTestUtil.java       # Security testing utilities
    └── TestDataBuilder.java       # Test data generation
```

#### Resources Structure
```
src/main/resources/
├── application.yml                  # Main application configuration
├── application-dev.yml              # Development profile
├── application-prod.yml             # Production profile
├── application-test.yml             # Test profile
├── logback-spring.xml               # Logging configuration
├── static/                          # Static web resources
└── templates/                       # Thymeleaf templates (future)
```

### Frontend Structure (React + TypeScript)

```
frontend/
├── package.json                     # Node.js dependencies
├── tsconfig.json                    # TypeScript configuration
├── vite.config.ts                   # Vite build configuration
├── public/                          # Static assets
└── src/
    ├── components/                   # React components
    ├── pages/                       # Page components
    ├── hooks/                       # Custom React hooks
    ├── utils/                       # Utility functions
    └── [Additional React structure] # Standard React app structure
```

### Project Management Structure

```
.claude/
├── context/                        # AI agent context documentation
│   ├── README.md                   # Context system guide
│   ├── progress.md                 # Project progress tracking
│   ├── project-structure.md       # This file
│   └── [Additional context files]    # Technical and product context
├── epics/                          # Epic management
│   └── memorize-words/            # Current epic
│       ├── epic.md                 # Epic definition and planning
│       ├── 001.md to 008.md       # Task definitions
│       ├── updates/                # Progress tracking per issue
│       └── execution-status.md     # Overall execution status
├── prds/                           # Product Requirement Documents
│   └── memorize-words.md           # Product requirements
└── rules/                          # Development rules and guidelines
    ├── [Various rule files]        # Best practices and workflows
```

## File Naming Conventions

### Java Classes
- **Entities**: PascalCase (e.g., `VocabularyList.java`)
- **Controllers**: PascalCase + Controller (e.g., `WordController.java`)
- **Services**: PascalCase + Service (e.g., `WordService.java`)
- **Repositories**: PascalCase + Repository (e.g., `WordRepository.java`)
- **DTOs**: PascalCase + type (e.g., `CreateWordRequest.java`)
- **Exceptions**: PascalCase + Exception (e.g., `DuplicateWordException.java`)

### Test Files
- **Unit Tests**: PascalCase + Test (e.g., `WordServiceTest.java`)
- **Integration Tests**: PascalCase + IntegrationTest (e.g., `HealthControllerIntegrationTest.java`)
- **Test Utilities**: PascalCase + Util (e.g., `EntityTestUtil.java`)

### Configuration Files
- **Application Config**: `application-{profile}.yml`
- **Logging**: `logback-spring.xml`
- **Build**: `pom.xml` (Maven)

### Documentation Files
- **Markdown**: kebab-case (e.g., `project-structure.md`)
- **Context Files**: kebab-case (e.g., `tech-context.md`)

## Package Organization

### Backend Packages
- **`com.memorizewords.config`**: Configuration classes
- **`com.memorizewords.controller`**: REST API controllers
- **`com.memorizewords.dto`**: Data transfer objects
- **`com.memorizewords.entity`**: JPA entities
- **`com.memorizewords.enums`**: Enum definitions
- **`com.memorizewords.exception`**: Custom exceptions
- **`com.memorizewords.repository`**: Data access layer
- **`com.memorizewords.service`**: Business logic
- **`com.memorizewords.specification`**: Dynamic query specifications

### Test Packages
- **`com.memorizewords.integration`**: Integration tests
- **`com.memorizewords.unit`**: Unit tests
- **`com.memorizewords.util`**: Test utilities

## Module Dependencies

### Core Dependencies
- **MemorizeWordsApplication** (Main entry point)
  - ↓ Controllers (REST endpoints)
  - ↓ Services (Business logic)
  - ↓ Repositories (Data access)
  - ↓ Entities (Data models)

### Test Dependencies
- **Integration Tests** → Full Spring context
- **Service Tests** → Service layer with mocked repositories
- **Unit Tests** → Individual components in isolation

### Configuration Dependencies
- **Application Configuration** → All components
- **Test Configuration** → Test-specific overrides
- **Profile Configurations** → Environment-specific settings

## Build and Deployment Structure

### Source Code Layout
- **Main Sources**: `src/main/java/`
- **Test Sources**: `src/test/java/`
- **Resources**: `src/main/resources/`
- **Test Resources**: `src/test/resources/`

### Build Output
- **Compiled Classes**: `target/classes/`
- **Test Classes**: `target/test-classes/`
- **JAR File**: `target/memorize-words-1.0.0-SNAPSHOT.jar`
- **Reports**: `target/site/`

### Frontend Build
- **Source**: `frontend/src/`
- **Build Output**: `frontend/dist/`
- **Development Server**: Available via Vite

## Key Architecture Patterns

### Layered Architecture
- **Controller Layer**: REST API endpoints and HTTP handling
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access and persistence
- **Entity Layer**: Domain models and relationships

### Separation of Concerns
- **DTOs**: Separate from entities for API communication
- **Exceptions**: Custom exceptions for specific error scenarios
- **Specifications**: Dynamic query building separate from repositories
- **Utilities**: Reusable test and application utilities

### Configuration Management
- **Environment Profiles**: Separate configurations for dev/test/prod
- **Externalized Configuration**: YAML files for all settings
- **Test Configuration**: Isolated test environment setup