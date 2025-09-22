-- Add additional constraints and improve table definitions
-- This migration adds constraints, check conditions, and improves data integrity

-- Add unique constraints for users
ALTER TABLE users
ADD CONSTRAINT uc_users_username UNIQUE (username),
ADD CONSTRAINT uc_users_email UNIQUE (email);

-- Add constraints for word fields
ALTER TABLE words
ADD CONSTRAINT chk_words_word_not_empty CHECK (word <> ''),
ADD CONSTRAINT chk_words_word_length CHECK (LENGTH(word) >= 1 AND LENGTH(word) <= 200),
ADD CONSTRAINT chk_words_language_not_empty CHECK (source_language <> '' AND target_language <> ''),
ADD CONSTRAINT chk_words_language_length CHECK (LENGTH(source_language) >= 2 AND LENGTH(source_language) <= 10),
ADD CONSTRAINT chk_words_target_language_length CHECK (LENGTH(target_language) >= 2 AND LENGTH(target_language) <= 10);

-- Add constraints for word_examples
ALTER TABLE word_examples
ADD CONSTRAINT chk_word_examples_example_not_empty CHECK (example_text <> ''),
ADD CONSTRAINT chk_word_examples_example_length CHECK (LENGTH(example_text) >= 1),
ADD CONSTRAINT chk_word_examples_translation_length CHECK (translation IS NULL OR LENGTH(translation) >= 1);

-- Add constraints for word_translations
ALTER TABLE word_translations
ADD CONSTRAINT chk_word_translations_translation_not_empty CHECK (translation <> ''),
ADD CONSTRAINT chk_word_translations_translation_length CHECK (LENGTH(translation) >= 1 AND LENGTH(translation) <= 500),
ADD CONSTRAINT chk_word_translations_language_not_empty CHECK (language <> ''),
ADD CONSTRAINT chk_word_translations_language_length CHECK (LENGTH(language) >= 2 AND LENGTH(language) <= 10),
ADD CONSTRAINT chk_word_translations_context_length CHECK (context IS NULL OR LENGTH(context) >= 1);

-- Add constraints for word_collections
ALTER TABLE word_collections
ADD CONSTRAINT chk_word_collections_name_not_empty CHECK (name <> ''),
ADD CONSTRAINT chk_word_collections_name_length CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 200),
ADD CONSTRAINT chk_word_collections_description_length CHECK (description IS NULL OR LENGTH(description) >= 1);

-- Add constraints for learning_progress
ALTER TABLE learning_progress
ADD CONSTRAINT chk_learning_progress_attempts_non_negative CHECK (correct_answers >= 0 AND total_attempts >= 0),
ADD CONSTRAINT chk_learning_progress_consecutive_correct_non_negative CHECK (consecutive_correct >= 0),
ADD CONSTRAINT chk_learning_progress_ease_factor_positive CHECK (ease_factor > 0),
ADD CONSTRAINT chk_learning_progress_interval_non_negative CHECK (interval_days >= 0),
ADD CONSTRAINT chk_learning_progress_correct_attempts_ratio CHECK (total_attempts = 0 OR correct_answers <= total_attempts),
ADD CONSTRAINT chk_learning_progress_consecutive_correct_attempts CHECK (consecutive_correct <= correct_answers);

-- Add constraints for study_sessions
ALTER TABLE study_sessions
ADD CONSTRAINT chk_study_sessions_total_words_non_negative CHECK (total_words >= 0),
ADD CONSTRAINT chk_study_sessions_correct_answers_non_negative CHECK (correct_answers >= 0),
ADD CONSTRAINT chk_study_sessions_duration_non_negative CHECK (duration >= 0),
ADD CONSTRAINT chk_study_sessions_correct_answers_ratio CHECK (total_words = 0 OR correct_answers <= total_words),
ADD CONSTRAINT chk_study_sessions_completion_order CHECK (completed_at IS NULL OR completed_at >= started_at);

-- Add constraints for study_session_results
ALTER TABLE study_session_results
ADD CONSTRAINT chk_study_session_results_response_time_non_negative CHECK (response_time IS NULL OR response_time >= 0),
ADD CONSTRAINT chk_study_session_results_answer_length CHECK (answer_given IS NULL OR LENGTH(answer_given) >= 1);

-- Add constraints for user_statistics
ALTER TABLE user_statistics
ADD CONSTRAINT chk_user_statistics_non_negative CHECK (
    total_words_learned >= 0 AND
    total_study_sessions >= 0 AND
    total_study_time >= 0 AND
    current_streak >= 0 AND
    longest_streak >= 0
),
ADD CONSTRAINT chk_user_statistics_accuracy_range CHECK (average_accuracy >= 0 AND average_accuracy <= 1),
ADD CONSTRAINT chk_user_statistics_streak_consistency CHECK (current_streak <= longest_streak);

-- Add default values and improve column definitions
ALTER TABLE words
MODIFY COLUMN part_of_speech ENUM('NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 'PRONOUN', 'PREPOSITION', 'CONJUNCTION', 'INTERJECTION', 'DETERMINER', 'OTHER') DEFAULT 'OTHER',
MODIFY COLUMN difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'BEGINNER';

-- Add default values for learning_progress
ALTER TABLE learning_progress
MODIFY COLUMN mastery_level ENUM('NEW', 'LEARNING', 'FAMILIAR', 'MASTERED') DEFAULT 'NEW',
MODIFY COLUMN ease_factor DOUBLE DEFAULT 2.5,
MODIFY COLUMN interval_days INT DEFAULT 0,
MODIFY COLUMN correct_answers INT DEFAULT 0,
MODIFY COLUMN total_attempts INT DEFAULT 0,
MODIFY COLUMN consecutive_correct INT DEFAULT 0;

-- Add default values for study_sessions
ALTER TABLE study_sessions
MODIFY COLUMN study_mode ENUM('FLASHCARDS', 'QUIZ', 'TYPING', 'LISTENING') DEFAULT 'FLASHCARDS',
MODIFY COLUMN total_words INT DEFAULT 0,
MODIFY COLUMN correct_answers INT DEFAULT 0,
MODIFY COLUMN duration INT DEFAULT 0;

-- Add default values for user_statistics
ALTER TABLE user_statistics
MODIFY COLUMN total_words_learned INT DEFAULT 0,
MODIFY COLUMN total_study_sessions INT DEFAULT 0,
MODIFY COLUMN total_study_time INT DEFAULT 0,
MODIFY COLUMN average_accuracy DOUBLE DEFAULT 0.0,
MODIFY COLUMN current_streak INT DEFAULT 0,
MODIFY COLUMN longest_streak INT DEFAULT 0;

-- Add check constraints for email format validation (simplified)
ALTER TABLE users
ADD CONSTRAINT chk_users_email_format CHECK (email LIKE '%@%.%');

-- Add check constraints for username format
ALTER TABLE users
ADD CONSTRAINT chk_users_username_format CHECK (
    username REGEXP '^[a-zA-Z0-9_]+$' AND
    LENGTH(username) >= 3 AND
    LENGTH(username) <= 50
);

-- Add check constraints for password hash minimum length
ALTER TABLE users
ADD CONSTRAINT chk_users_password_hash_length CHECK (LENGTH(password_hash) >= 60);

-- Add indexes for frequently queried columns that were missed in V1
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_words_created_at ON words(created_at);
CREATE INDEX idx_word_collections_created_at ON word_collections(created_at);
CREATE INDEX idx_learning_progress_created_at ON learning_progress(created_at);
CREATE INDEX idx_study_sessions_user_started ON study_sessions(user_id, started_at);

-- Add composite indexes for common query patterns
CREATE INDEX idx_learning_progress_user_next_review ON learning_progress(user_id, next_review_date);
CREATE INDEX idx_study_sessions_user_mode ON study_sessions(user_id, study_mode);
CREATE INDEX idx_collection_words_collection_added ON collection_words(collection_id, added_at);
CREATE INDEX idx_words_difficulty_language ON words(difficulty, source_language, target_language);

-- Add trigger for updating user statistics (simplified approach using stored procedure)
DELIMITER //
CREATE PROCEDURE update_user_statistics(IN p_user_id BIGINT)
BEGIN
    DECLARE total_words INT DEFAULT 0;
    DECLARE total_sessions INT DEFAULT 0;
    DECLARE total_time INT DEFAULT 0;
    DECLARE correct_count INT DEFAULT 0;
    DECLARE total_count INT DEFAULT 0;
    DECLARE avg_accuracy DOUBLE DEFAULT 0.0;

    -- Count mastered words
    SELECT COUNT(*) INTO total_words
    FROM learning_progress
    WHERE user_id = p_user_id AND mastery_level = 'MASTERED';

    -- Count study sessions
    SELECT COUNT(*) INTO total_sessions
    FROM study_sessions
    WHERE user_id = p_user_id AND completed_at IS NOT NULL;

    -- Sum study time
    SELECT COALESCE(SUM(duration), 0) INTO total_time
    FROM study_sessions
    WHERE user_id = p_user_id AND completed_at IS NOT NULL;

    -- Calculate accuracy
    SELECT COALESCE(SUM(correct_answers), 0), COALESCE(SUM(total_words), 0)
    INTO correct_count, total_count
    FROM study_sessions
    WHERE user_id = p_user_id AND completed_at IS NOT NULL;

    IF total_count > 0 THEN
        SET avg_accuracy = correct_count / total_count;
    END IF;

    -- Update or insert user statistics
    INSERT INTO user_statistics (user_id, total_words_learned, total_study_sessions, total_study_time, average_accuracy, last_activity_date)
    VALUES (p_user_id, total_words, total_sessions, total_time, avg_accuracy, CURDATE())
    ON DUPLICATE KEY UPDATE
        total_words_learned = total_words,
        total_study_sessions = total_sessions,
        total_study_time = total_time,
        average_accuracy = avg_accuracy,
        last_activity_date = CURDATE(),
        updated_at = CURRENT_TIMESTAMP;
END //
DELIMITER ;