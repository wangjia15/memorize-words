-- Create performance-optimized indexes for common query patterns
-- This migration focuses on optimizing database performance for the memorize-words application

-- Optimize user queries with composite indexes
CREATE INDEX idx_users_username_email ON users(username, email);
CREATE INDEX idx_users_role_created ON users(role, created_at);
CREATE INDEX idx_users_activity ON users(id, last_activity_date);

-- Optimize word searches and filtering
CREATE INDEX idx_words_search ON words(word, source_language, target_language);
CREATE INDEX idx_words_filtering ON words(difficulty, part_of_speech, source_language, target_language);
CREATE INDEX idx_words_language_created ON words(source_language, target_language, created_at);

-- Optimize word examples and translations for word detail pages
CREATE INDEX idx_word_examples_word_created ON word_examples(word_id, created_at);
CREATE INDEX idx_word_translations_word_language ON word_translations(word_id, language);

-- Optimize word collection queries
CREATE INDEX idx_word_collections_user_type ON word_collections(user_id, type, is_public);
CREATE INDEX idx_word_collections_public_type ON word_collections(is_public, type, created_at);
CREATE INDEX idx_word_collections_user_name ON word_collections(user_id, name);

-- Optimize collection-word relationships for efficient loading
CREATE INDEX idx_collection_words_collection_word ON collection_words(collection_id, word_id);
CREATE INDEX idx_collection_words_word_collection ON collection_words(word_id, collection_id);

-- Optimize learning progress queries for spaced repetition algorithm
CREATE INDEX idx_learning_progress_user_next_review_ordered ON learning_progress(user_id, next_review_date, mastery_level);
CREATE INDEX idx_learning_progress_word_mastery ON learning_progress(word_id, mastery_level);
CREATE INDEX idx_learning_progress_user_mastery_updated ON learning_progress(user_id, mastery_level, updated_at);
CREATE INDEX idx_learning_progress_due_reviews ON learning_progress(next_review_date, mastery_level);

-- Optimize study session queries for analytics and performance tracking
CREATE INDEX idx_study_sessions_user_completed ON study_sessions(user_id, completed_at, study_mode);
CREATE INDEX idx_study_sessions_collection_mode ON study_sessions(collection_id, study_mode, started_at);
CREATE INDEX idx_study_sessions_analytics ON study_sessions(study_mode, started_at, completed_at);
CREATE INDEX idx_study_sessions_performance ON study_sessions(user_id, duration, correct_answers);

-- Optimize study session results for detailed analytics
CREATE INDEX idx_study_session_results_session_correct ON study_session_results(study_session_id, is_correct, response_time);
CREATE INDEX idx_study_session_results_word_performance ON study_session_results(word_id, is_correct, response_time);
CREATE INDEX idx_study_session_results_analytics ON study_session_results(is_correct, response_time, created_at);

-- Optimize user statistics for dashboard and reporting
CREATE INDEX idx_user_statistics_words_learned ON user_statistics(total_words_learned, last_activity_date);
CREATE INDEX idx_user_statistics_study_time ON user_statistics(total_study_time, last_activity_date);
CREATE INDEX idx_user_statistics_accuracy ON user_statistics(average_accuracy, last_activity_date);
CREATE INDEX idx_user_statistics_streak ON user_statistics(current_streak, longest_streak, last_activity_date);

-- Create specialized indexes for spaced repetition algorithm optimization
CREATE INDEX idx_learning_progress_spaced_repetition ON learning_progress(
    user_id,
    next_review_date,
    mastery_level,
    ease_factor,
    interval_days
);

-- Create indexes for reporting and analytics queries
CREATE INDEX idx_study_sessions_reporting ON study_sessions(
    user_id,
    study_mode,
    started_at,
    completed_at,
    duration,
    correct_answers
);

CREATE INDEX idx_learning_progress_reporting ON learning_progress(
    user_id,
    mastery_level,
    correct_answers,
    total_attempts,
    ease_factor,
    interval_days,
    next_review_date
);

-- Create covering indexes for common SELECT queries to avoid table scans
CREATE INDEX idx_words_covering_search ON words(source_language, target_language, word, id, part_of_speech, difficulty);
CREATE INDEX idx_word_collections_covering_user ON word_collections(user_id, id, name, type, is_public, created_at);
CREATE INDEX idx_learning_progress_covering_user_word ON learning_progress(user_id, word_id, id, mastery_level, next_review_date, ease_factor);

-- Create indexes for foreign key constraints optimization
CREATE INDEX fk_word_examples_word_idx ON word_examples(word_id);
CREATE INDEX fk_word_translations_word_idx ON word_translations(word_id);
CREATE INDEX fk_word_collections_user_idx ON word_collections(user_id);
CREATE INDEX fk_collection_words_collection_idx ON collection_words(collection_id);
CREATE INDEX fk_collection_words_word_idx ON collection_words(word_id);
CREATE INDEX fk_learning_progress_user_idx ON learning_progress(user_id);
CREATE INDEX fk_learning_progress_word_idx ON learning_progress(word_id);
CREATE INDEX fk_study_sessions_user_idx ON study_sessions(user_id);
CREATE INDEX fk_study_sessions_collection_idx ON study_sessions(collection_id);
CREATE INDEX fk_study_session_results_session_idx ON study_session_results(study_session_id);
CREATE INDEX fk_study_session_results_word_idx ON study_session_results(word_id);
CREATE INDEX fk_user_statistics_user_idx ON user_statistics(user_id);

-- Create indexes for sorting and pagination optimization
CREATE INDEX idx_words_pagination ON words(source_language, target_language, created_at, id);
CREATE INDEX idx_word_collections_pagination ON word_collections(user_id, created_at, id);
CREATE INDEX idx_study_sessions_pagination ON study_sessions(user_id, started_at, id);
CREATE INDEX idx_learning_progress_pagination ON learning_progress(user_id, next_review_date, id);

-- Create specialized indexes for common JOIN operations
CREATE INDEX idx_words_joins ON words(id, source_language, target_language, word, part_of_speech, difficulty);
CREATE INDEX idx_users_joins ON users(id, username, email, role, created_at);
CREATE INDEX idx_word_collections_joins ON word_collections(id, user_id, name, type, is_public, created_at);

-- Add index hints for the query optimizer (MySQL specific)
-- These help the optimizer choose better execution plans

-- Create statistics for better query optimization (MySQL 8.0+)
-- ANALYZE TABLE users, words, word_examples, word_translations, word_collections,
--              collection_words, learning_progress, study_sessions, study_session_results, user_statistics;

-- Update table statistics for better index usage
-- Note: This would be run periodically in production, not during migration
-- OPTIMIZE TABLE users, words, word_examples, word_translations, word_collections,
--                   collection_words, learning_progress, study_sessions, study_session_results, user_statistics;