-- Create initial database schema for memorize-words application
-- This migration creates all the tables with basic structure

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    preferred_language VARCHAR(10) DEFAULT 'en',
    role ENUM('USER', 'ADMIN', 'MODERATOR') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create words table
CREATE TABLE words (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(200) NOT NULL,
    definition TEXT,
    pronunciation TEXT,
    part_of_speech ENUM('NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 'PRONOUN', 'PREPOSITION', 'CONJUNCTION', 'INTERJECTION', 'DETERMINER', 'OTHER'),
    difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'BEGINNER',
    source_language VARCHAR(10) DEFAULT 'en',
    target_language VARCHAR(10) DEFAULT 'es',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_words_word (word),
    INDEX idx_words_part_of_speech (part_of_speech),
    INDEX idx_words_difficulty (difficulty),
    INDEX idx_words_language_pair (source_language, target_language),
    FULLTEXT INDEX ft_words_search (word, definition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create word_examples table
CREATE TABLE word_examples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_id BIGINT NOT NULL,
    example_text TEXT NOT NULL,
    translation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
    INDEX idx_word_examples_word_id (word_id),
    FULLTEXT INDEX ft_word_examples_search (example_text, translation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create word_translations table
CREATE TABLE word_translations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_id BIGINT NOT NULL,
    translation VARCHAR(500) NOT NULL,
    language VARCHAR(10) NOT NULL,
    context TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
    INDEX idx_word_translations_word_id (word_id),
    INDEX idx_word_translations_language (language),
    FULLTEXT INDEX ft_word_translations_search (translation, context)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create word_collections table
CREATE TABLE word_collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type ENUM('PERSONAL', 'SHARED', 'SYSTEM') DEFAULT 'PERSONAL',
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_word_collections_user_id (user_id),
    INDEX idx_word_collections_type (type),
    INDEX idx_word_collections_is_public (is_public),
    INDEX idx_word_collections_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create collection_words junction table
CREATE TABLE collection_words (
    collection_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, word_id),
    FOREIGN KEY (collection_id) REFERENCES word_collections(id) ON DELETE CASCADE,
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
    INDEX idx_collection_words_collection_id (collection_id),
    INDEX idx_collection_words_word_id (word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create learning_progress table
CREATE TABLE learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    mastery_level ENUM('NEW', 'LEARNING', 'FAMILIAR', 'MASTERED') DEFAULT 'NEW',
    correct_answers INT DEFAULT 0,
    total_attempts INT DEFAULT 0,
    consecutive_correct INT DEFAULT 0,
    ease_factor DOUBLE DEFAULT 2.5,
    interval_days INT DEFAULT 0,
    next_review_date TIMESTAMP,
    last_reviewed TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_word_progress (user_id, word_id),
    INDEX idx_learning_progress_user_id (user_id),
    INDEX idx_learning_progress_word_id (word_id),
    INDEX idx_learning_progress_mastery_level (mastery_level),
    INDEX idx_learning_progress_next_review (next_review_date),
    INDEX idx_learning_progress_user_mastery (user_id, mastery_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create study_sessions table
CREATE TABLE study_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    collection_id BIGINT,
    study_mode ENUM('FLASHCARDS', 'QUIZ', 'TYPING', 'LISTENING') DEFAULT 'FLASHCARDS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    total_words INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    duration INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES word_collections(id) ON DELETE SET NULL,
    INDEX idx_study_sessions_user_id (user_id),
    INDEX idx_study_sessions_collection_id (collection_id),
    INDEX idx_study_sessions_study_mode (study_mode),
    INDEX idx_study_sessions_started_at (started_at),
    INDEX idx_study_sessions_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create study_session_results table
CREATE TABLE study_session_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_session_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    response_time INT,
    answer_given TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_session_id) REFERENCES study_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
    INDEX idx_study_session_results_session_id (study_session_id),
    INDEX idx_study_session_results_word_id (word_id),
    INDEX idx_study_session_results_is_correct (is_correct)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_statistics table
CREATE TABLE user_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_words_learned INT DEFAULT 0,
    total_study_sessions INT DEFAULT 0,
    total_study_time INT DEFAULT 0,
    average_accuracy DOUBLE DEFAULT 0.0,
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    last_activity_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_statistics (user_id),
    INDEX idx_user_statistics_user_id (user_id),
    INDEX idx_user_statistics_last_activity (last_activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create database version table for Flyway
CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX idx_flyway_schema_history_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;