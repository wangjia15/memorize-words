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
- **MySQL 8.0+** - Primary database
- **H2 Database** - Testing database
- **Maven** - Build and dependency management
- **Java 17+** - Programming language

### Frontend
- **Thymeleaf** - Server-side templating
- **Tailwind CSS** - Utility-first CSS framework
- **shadcn/ui** - Component library
- **Vanilla JavaScript** - Interactive elements

## Quick Start

### Prerequisites

- **Java 17+** installed
- **Maven 3.6+** installed
- **MySQL 8.0+** installed and running
- **Node.js 16+** (for frontend build tools)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd memorize-words
   ```

2. **Set up the database**
   ```sql
   CREATE DATABASE memorize_words;
   CREATE USER 'memorize_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON memorize_words.* TO 'memorize_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure application properties**

   Copy `src/main/resources/application.yml.example` to `src/main/resources/application.yml` and update:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/memorize_words
       username: memorize_user
       password: your_password
   ```

4. **Build and run the application**
   ```bash
   # Build the project
   mvn clean install

   # Run the application
   mvn spring-boot:run
   ```

5. **Access the application**

   Open your browser and navigate to: `http://localhost:8080`

### Development Setup

1. **Frontend development setup**
   ```bash
   # Install frontend dependencies
   npm install

   # Start development server (for frontend-only development)
   npm run dev
   ```

2. **Running tests**
   ```bash
   # Run all tests
   mvn test

   # Run only unit tests
   mvn test -Dtest=*Unit*

   # Run only integration tests
   mvn test -Dtest=*Integration*
   ```

3. **Building for production**
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
│   │   ├── service/                         # Business logic
│   │   ├── repository/                      # Data access layer
│   │   ├── entity/                          # JPA entities
│   │   ├── dto/                             # Data transfer objects
│   │   └── exception/                       # Exception handlers
│   └── resources/
│       ├── application.yml                  # Main configuration
│       ├── application-dev.yml              # Development profile
│       ├── application-prod.yml             # Production profile
│       ├── templates/                       # Thymeleaf templates
│       └── static/                          # Static assets
└── test/
    ├── java/com/memorizewords/
    │   ├── unit/                            # Unit tests
    │   ├── integration/                     # Integration tests
    │   ├── config/                          # Test configuration
    │   └── util/                            # Test utilities
    └── resources/
        ├── application-test.yml             # Test configuration
        └── data/                            # Test data files
```

## API Endpoints

### Health Check
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Vocabulary Management
- `GET /api/words` - List all words
- `POST /api/words` - Add new word
- `GET /api/words/{id}` - Get word details
- `PUT /api/words/{id}` - Update word
- `DELETE /api/words/{id}` - Delete word

### Collections
- `GET /api/collections` - List user collections
- `POST /api/collections` - Create new collection
- `GET /api/collections/{id}` - Get collection details
- `PUT /api/collections/{id}` - Update collection
- `DELETE /api/collections/{id}` - Delete collection

### Learning Progress
- `GET /api/progress` - Get learning progress
- `POST /api/progress/review` - Record review session
- `GET /api/progress/stats` - Get learning statistics

## Configuration

### Environment Profiles

The application supports multiple environment profiles:

- **dev** - Development environment with hot reload
- **prod** - Production environment with optimizations
- **test** - Testing environment with H2 database

### Database Configuration

The application uses MySQL as the primary database. Key configuration options:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/memorize_words
    username: memorize_user
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

## Testing

### Test Coverage

The project includes comprehensive test coverage:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Controller Tests**: Test API endpoints
- **Repository Tests**: Test database operations

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MemorizeWordsApplicationTest

# Run tests with coverage
mvn clean verify jacoco:report
```

### Test Data Management

Test data is managed through:
- `TestDataBuilder` utility class for generating test data
- H2 in-memory database for isolated testing
- `@Transactional` annotation for automatic cleanup
- Test-specific configuration in `application-test.yml`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Spring Boot best practices
- Write comprehensive tests for new features
- Maintain code coverage above 80%
- Use JavaDoc for public APIs
- Follow the existing code style and structure

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Check the [documentation](docs/)
- Review existing issues and discussions

## Roadmap

- [ ] Mobile app development
- [ ] Offline mode support
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Social learning features
- [ ] AI-powered vocabulary recommendations

---

Built with ❤️ using Spring Boot 3.x and modern web technologies.