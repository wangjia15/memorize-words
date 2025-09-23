# Memorize Words

A modern vocabulary learning application built with Spring Boot 3.x, featuring scientific spaced repetition algorithms and comprehensive progress tracking.

## Features

- **Spaced Repetition Learning**: Scientifically proven algorithm for optimal vocabulary retention
- **Progress Tracking**: Comprehensive statistics and learning analytics
- **Word Collections**: Organize vocabulary into custom collections
- **Multiple Study Modes**: Flashcards, quizzes, typing practice, and listening exercises
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Spring Boot 3.x**: Latest framework features with improved performance and security

## Tech Stack

### Backend
- **Spring Boot 3.2.x** - Core framework
- **Spring Security 6.x** - Authentication and authorization
- **Spring Data JPA** - Database access layer
- **MySQL 8.0+** - Primary database (production)
- **H2 Database** - Development and testing database
- **Maven** - Build and dependency management
- **Java 17+** - Programming language

### Frontend
- **React** - Frontend framework
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS** - Utility-first CSS framework
- **shadcn/ui** - Component library
- **Vite** - Build tool and development server

## Quick Start

### Prerequisites

- **Java 17+** installed
- **Maven 3.6+** installed
- **Node.js 16+** (for frontend build tools)
- **MySQL 8.0+** (for production deployment)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd memorize-words
   ```

2. **Backend setup**
   ```bash
   # Build the project
   mvn clean install

   # Run the application (development mode)
   mvn spring-boot:run
   ```

3. **Frontend setup**
   ```bash
   # Navigate to frontend directory
   cd frontend

   # Install dependencies
   npm install

   # Start development server
   npm run dev
   ```

4. **Access the application**

   - Backend API: `http://localhost:8080`
   - Frontend: `http://localhost:3000`
   - Health Check: `http://localhost:8080/api/health`
   - H2 Console: `http://localhost:8080/h2-console` (development only)

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Unit*

# Run only integration tests
mvn test -Dtest=*Integration*
```

### Building for Production

```bash
# Build optimized production version
mvn clean package -Pprod

# Run the production JAR
java -jar target/memorize-words-1.0.0-SNAPSHOT.jar
```

## Project Structure

```
src/
├── main/
│   ├── java/com/memorizewords/
│   │   ├── MemorizeWordsApplication.java    # Main application class
│   │   ├── config/                          # Configuration classes
│   │   ├── controller/                      # REST controllers
│   │   ├── exception/                       # Exception handling
│   │   └── service/                         # Business logic (to be implemented)
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-dev.yml              # Development profile
│       ├── application-prod.yml             # Production profile
│       └── logback-spring.xml               # Logging configuration
└── test/
    ├── java/com/memorizewords/
    │   ├── unit/                            # Unit tests
    │   ├── integration/                     # Integration tests
    │   ├── config/                          # Test configuration
    │   └── util/                            # Test utilities
    └── resources/
        ├── application-test.yml             # Test configuration
        └── data/                            # Test data files

frontend/                                       # React frontend
├── src/
│   ├── components/                           # React components
│   ├── pages/                               # Page components
│   ├── hooks/                               # Custom React hooks
│   └── utils/                               # Utility functions
├── public/                                  # Static assets
└── package.json                            # Frontend dependencies
```

## API Endpoints

### Health Check
- `GET /api/health` - Basic application health status
- `GET /api/health/detailed` - Detailed health information with system metrics
- `GET /actuator/health` - Spring Boot health endpoint
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

## Configuration

### Environment Profiles

The application supports multiple environment profiles:

- **dev** - Development environment with H2 database and debug logging
- **prod** - Production environment with MySQL database and optimized logging
- **test** - Testing environment with H2 in-memory database and disabled security

## Testing

### Test Coverage

The project includes comprehensive test coverage:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions with full Spring context
- **Controller Tests**: Test API endpoints with MockMvc

### Test Configuration

- **H2 Database**: In-memory database for isolated testing
- **Test Security**: Disabled security for test endpoints
- **Test Data**: `TestDataBuilder` utility for generating test data
- **Transaction Management**: Automatic cleanup with `@Transactional`

## Current Implementation Status

### ✅ Completed Features
- Spring Boot 3.2.x project setup
- Health check endpoints (`/api/health`, `/api/health/detailed`)
- Global exception handling
- CORS configuration for frontend integration
- Comprehensive test infrastructure (21 passing tests)
- Development and production environment configurations
- Logging configuration with logback
- React frontend setup with shadcn/ui
- Maven build configuration

### 🚧 In Progress
- User authentication system
- Vocabulary management endpoints
- Spaced repetition algorithm implementation
- Database schema and entities
- Frontend-backend integration

### 📋 Planned Features
- Word collections and categories
- Learning progress tracking
- Multiple study modes
- Advanced analytics dashboard
- Offline mode support
- Mobile app development

## Development Guidelines

### Code Quality
- Follow Spring Boot best practices
- Maintain test coverage above 80%
- Use JavaDoc for public APIs
- Follow existing code style and structure
- Write comprehensive tests for new features

### Git Workflow
- Create feature branches from `main`
- Use descriptive commit messages
- Run tests before committing
- Keep pull requests focused and small

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

Built with ❤️ using Spring Boot 3.x and modern web technologies.
