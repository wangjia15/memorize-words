---
created: 2025-09-23T02:01:30Z
last_updated: 2025-09-23T02:01:30Z
version: 1.0
author: Claude Code PM System
---

# Technical Context

## Technology Stack

### Backend Technologies

#### Core Framework
- **Spring Boot 3.2.0** - Primary application framework
  - Auto-configuration and dependency management
  - Embedded server support (Tomcat)
  - Actuator for monitoring and management
  - Comprehensive testing support

#### Security Framework
- **Spring Security 6.x** - Authentication and authorization
  - JWT token support (from GitHub issue #4)
  - Role-based access control
  - Method-level security annotations
  - CSRF protection and security headers

#### Data Layer
- **Spring Data JPA** - Database access and ORM
  - Repository pattern implementation
  - JPA Specifications for dynamic queries
  - Automatic CRUD operations
  - Transaction management
- **MySQL 8.0.33** - Production database
  - Relational database with ACID compliance
  - Full-text search capabilities
  - JSON data type support
- **H2 Database** - Development and testing
  - In-memory database for fast testing
  - Web console for development debugging
  - Compatibility with MySQL for testing

#### Build and Dependency Management
- **Maven 3.6+** - Build tool and dependency management
  - Standardized project structure
  - Dependency resolution and management
  - Plugin-based build system
  - Multi-profile support

### Frontend Technologies

#### Core Framework
- **React 18.2.0** - UI framework
  - Component-based architecture
  - Hooks for state management
  - Virtual DOM for performance
  - Concurrent features and suspense

#### Type Safety
- **TypeScript 5.2.2** - Type-safe JavaScript
  - Static type checking
  - Enhanced IDE support
  - Better refactoring capabilities
  - Interface and type definitions

#### Styling and UI
- **Tailwind CSS 3.3.5** - Utility-first CSS framework
  - Rapid UI development
  - Responsive design utilities
  - Custom theme support
  - Optimized production builds
- **shadcn/ui Components** - High-quality UI components
  - Radix UI primitives foundation
  - Accessible component design
  - Customizable themes
  - Modern design patterns

#### Development and Build Tools
- **Vite 4.5.0** - Build tool and development server
  - Fast development server with HMR
  - Optimized production builds
  - TypeScript support out of the box
  - Plugin ecosystem
- **ESLint 8.53.0** - Code linting and quality
  - Code consistency enforcement
  - Best practice rules
  - TypeScript support

#### Animation and UX
- **Framer Motion 10.16.5** - Animation library
  - Smooth animations and transitions
  - Gesture support
  - Performance optimized
- **Lucide React 0.294.0** - Icon library
  - Consistent icon design
  - Customizable icons
  - Wide icon selection

### Testing Technologies

#### Backend Testing
- **JUnit 5** - Unit testing framework
  - Modern testing features
  - Parameterized tests
  - Extension model
- **Mockito** - Mocking framework
  - Isolated unit testing
  - Behavior verification
  - Stub creation
- **Spring Boot Test** - Integration testing
  - Test slices for focused testing
  - MockMvc for controller testing
  - Test context management
- **Spring Security Test** - Security testing support
  - Security context testing
  - Mock authentication
  - Method security testing

#### Frontend Testing
- **React Testing Library** - Component testing
  - User-centric testing approach
  - DOM testing utilities
  - Accessibility testing
- **Vitest** - Unit and integration testing
  - Fast test execution
  - Vite integration
  - TypeScript support

### Development and Operations

#### Version Control
- **Git** - Distributed version control
  - Branching and merging
  - Collaboration features
  - History tracking

#### Database Tools
- **Flyway** - Database migration management (planned)
  - Version-controlled database changes
  - Repeatable migrations
  - Rollback capabilities

#### Monitoring and Observability
- **Spring Boot Actuator** - Application monitoring
  - Health endpoints
  - Metrics collection
  - Environment information
  - HTTP tracing

## Dependencies Analysis

### Core Spring Boot Dependencies
```xml
<!-- Core Framework -->
spring-boot-starter-web           # REST API, embedded server
spring-boot-starter-data-jpa      # Database access layer
spring-boot-starter-security      # Authentication & authorization
spring-boot-starter-validation    # Input validation
spring-boot-starter-actuator      # Monitoring & management
```

### Database Dependencies
```xml
mysql-connector-java:8.0.33      # MySQL JDBC driver
com.h2database:h2                 # In-memory test database
```

### Testing Dependencies
```xml
spring-boot-starter-test           # Spring Boot testing
spring-security-test               # Security testing
```

### Frontend Dependencies
```json
// Core Framework
"react": "^18.2.0"                 # UI framework
"react-dom": "^18.2.0"            # DOM rendering

// Type Safety and Tooling
"typescript": "^5.2.2"            # Type system
"@types/react": "^18.2.37"        # React types
"@types/react-dom": "^18.2.15"    # React DOM types

// Styling and UI
"tailwindcss": "^3.3.5"            # CSS framework
"clsx": "^2.0.0"                   # Conditional classes
"tailwind-merge": "^2.0.0"         # Class merging utility

// shadcn/ui Components
"@radix-ui/react-card": "^1.0.4"    # Card component
"@radix-ui/react-button": "^1.0.4"  # Button component
"@radix-ui/react-progress": "^1.0.3" # Progress component
"@radix-ui/react-badge": "^1.0.3"   # Badge component

// Animation and UX
"framer-motion": "^10.16.5"        # Animation library
"lucide-react": "^0.294.0"         # Icon library

// Development and Build
"vite": "^4.5.0"                   # Build tool
"@vitejs/plugin-react": "^4.1.1"   # React plugin

// Code Quality
"eslint": "^8.53.0"                 # Linting
"@typescript-eslint/eslint-plugin": "^6.10.0" # TypeScript linting
"@typescript-eslint/parser": "^6.10.0"     # TypeScript parser
"eslint-plugin-react-hooks": "^4.6.0"        # React hooks linting
"eslint-plugin-react-refresh": "^0.4.4"       # React refresh linting

// Build Tools
"postcss": "^8.4.31"               # CSS processing
"autoprefixer": "^10.4.16"         # CSS prefixing
```

## Technical Architecture

### Backend Architecture Patterns

#### Layered Architecture
- **Controller Layer**: HTTP request handling and response formatting
- **Service Layer**: Business logic implementation and orchestration
- **Repository Layer**: Data access and persistence operations
- **Entity Layer**: Domain model definitions and relationships

#### Design Patterns Implemented
- **Repository Pattern**: Abstract data access with Spring Data JPA
- **DTO Pattern**: Separate API data transfer objects from domain entities
- **Service Pattern**: Business logic encapsulation
- **Specification Pattern**: Dynamic query building with JPA Specifications
- **Exception Handling Pattern**: Centralized error handling with global exception handler

#### Security Architecture
- **Spring Security Filters**: Request interception and security enforcement
- **JWT Authentication**: Token-based authentication (from issue #4)
- **Role-Based Authorization**: Method-level security with annotations
- **CORS Configuration**: Cross-origin resource sharing for frontend integration
- **Password Encryption**: BCrypt password encoding

### Frontend Architecture Patterns

#### Component Architecture
- **Functional Components**: Modern React with hooks
- **Composition Pattern**: Flexible component composition
- **Container/Presentational Pattern**: Logic and UI separation
- **Custom Hooks**: Reusable stateful logic

#### State Management
- **React State**: Local component state
- **Props Drilling**: Parent-to-child data flow
- **Context API**: Global state management (potential future)
- **Server State**: API-driven state management

### Data Flow Architecture

#### Request Processing Flow
1. **HTTP Request** → Spring Controller
2. **Validation** → DTO validation with Jakarta Bean Validation
3. **Service Layer** → Business logic execution
4. **Repository Layer** → Database operations
5. **Response** → Formatted API response with proper HTTP status

#### Error Handling Flow
1. **Exception** → Custom business exceptions
2. **Global Handler** → Centralized exception processing
3. **HTTP Response** → Appropriate status codes and error messages
4. **Logging** → Error logging and monitoring

### Database Architecture

#### Schema Design
- **Relational Model**: MySQL with normalized tables
- **Entity Relationships**: JPA relationships with proper foreign keys
- **Indexing Strategy**: Optimized queries with database indexes
- **Data Integrity**: Constraints and cascading operations

#### Transaction Management
- **Spring Transactions**: Declarative transaction management
- **ACID Properties**: Database transaction guarantees
- **Rollback Support**: Automatic rollback on exceptions
- **Isolation Levels**: Configurable transaction isolation

## Development Environment

### Build Configuration
- **Maven**: Standardized build lifecycle
- **Profiles**: Development, test, and production configurations
- **Dependency Management**: Centralized version control
- **Plugin Configuration**: Spring Boot Maven plugin

### Testing Configuration
- **Test Slices**: Focused testing with Spring Boot Test
- **Mock Configuration**: Mockito for isolated testing
- **Test Containers**: Integration testing with real databases
- **Coverage Reporting**: JaCoCo for code coverage metrics

### Development Tools
- **IDE Support**: Spring Tool Suite or IntelliJ IDEA
- **Hot Reload**: Spring Boot DevTools for development
- **Database Console**: H2 web console for development
- **API Testing**: Postman or curl for endpoint testing

## Performance Considerations

### Backend Optimization
- **Lazy Loading**: JPA lazy loading for large collections
- **Query Optimization**: JPA Specifications for efficient queries
- **Caching Strategy**: Spring Cache abstraction (future implementation)
- **Connection Pooling**: HikariCP for database connections

### Frontend Optimization
- **Code Splitting**: Vite automatic code splitting
- **Tree Shaking**: Unused code elimination
- **Bundle Optimization**: Optimized production builds
- **Lazy Loading**: Component and route lazy loading

### Database Performance
- **Indexing**: Strategic indexes on frequently queried fields
- **Pagination**: Pageable results for large datasets
- **Batch Operations**: Bulk operations for efficiency
- **Connection Management**: Proper connection pool configuration

## Security Considerations

### Backend Security
- **Input Validation**: Comprehensive input sanitization
- **SQL Injection**: Parameterized queries with JPA
- **XSS Protection**: Content Security Policy headers
- **Authentication**: JWT token validation
- **Authorization**: Method-level security checks

### Frontend Security
- **Content Security**: Secure resource loading
- **XSS Prevention**: React's built-in XSS protection
- **HTTPS**: Secure communication (production)
- **Input Validation**: Client-side validation with server verification

## Monitoring and Observability

### Application Monitoring
- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Micrometer metrics collection
- **Logging**: Structured logging with correlation IDs
- **Tracing**: Request tracing support

### Performance Monitoring
- **Response Times**: API endpoint performance
- **Database Queries**: Query performance monitoring
- **Memory Usage**: JVM memory monitoring
- **Error Rates**: Error tracking and alerting