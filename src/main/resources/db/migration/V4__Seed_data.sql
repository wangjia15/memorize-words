-- Seed data for development environment
-- This migration adds sample data for testing and development purposes

-- Disable foreign key checks for bulk insert
SET FOREIGN_KEY_CHECKS = 0;

-- Insert sample users
INSERT INTO users (username, email, password_hash, first_name, last_name, preferred_language, role) VALUES
('admin', 'admin@memorizewords.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', 'en', 'ADMIN'),
('john_doe', 'john@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', 'en', 'USER'),
('maria_garcia', 'maria@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Maria', 'Garcia', 'es', 'USER'),
('chen_wei', 'chen@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Chen', 'Wei', 'zh', 'USER');

-- Insert sample words (English to Spanish)
INSERT INTO words (word, definition, pronunciation, part_of_speech, difficulty, source_language, target_language) VALUES
('hello', 'A greeting used when meeting someone', '/həˈloʊ/', 'INTERJECTION', 'BEGINNER', 'en', 'es'),
('goodbye', 'A farewell expression', '/ɡʊdˈbaɪ/', 'INTERJECTION', 'BEGINNER', 'en', 'es'),
('water', 'A clear, colorless liquid', '/ˈwɔːtər/', 'NOUN', 'BEGINNER', 'en', 'es'),
('house', 'A building for human habitation', '/haʊs/', 'NOUN', 'BEGINNER', 'en', 'es'),
('book', 'A set of printed pages bound together', '/bʊk/', 'NOUN', 'BEGINNER', 'en', 'es'),
('computer', 'An electronic device for processing data', '/kəmˈpjuːtər/', 'NOUN', 'INTERMEDIATE', 'en', 'es'),
('beautiful', 'Pleasing to look at; attractive', '/ˈbjuːtɪfəl/', 'ADJECTIVE', 'BEGINNER', 'en', 'es'),
('important', 'Of great significance or value', '/ɪmˈpɔːrtənt/', 'ADJECTIVE', 'BEGINNER', 'en', 'es'),
('learn', 'To acquire knowledge or skills through study', '/lɜːrn/', 'VERB', 'BEGINNER', 'en', 'es'),
('speak', 'To talk or articulate words', '/spiːk/', 'VERB', 'BEGINNER', 'en', 'es'),
('understand', 'To comprehend the meaning of something', '/ˌʌndərˈstænd/', 'VERB', 'INTERMEDIATE', 'en', 'es'),
('practice', 'To perform an activity repeatedly to improve skill', '/ˈpræktɪs/', 'VERB', 'BEGINNER', 'en', 'es'),
('quickly', 'At a fast speed', '/ˈkwɪkli/', 'ADVERB', 'BEGINNER', 'en', 'es'),
('always', 'On every occasion; at all times', '/ˈɔːlweɪz/', 'ADVERB', 'BEGINNER', 'en', 'es'),
('however', 'Nevertheless; despite that', '/haʊˈevər/', 'ADVERB', 'INTERMEDIATE', 'en', 'es');

-- Insert word examples for English words
INSERT INTO word_examples (word_id, example_text, translation) VALUES
(1, 'Hello, how are you today?', 'Hola, ¿cómo estás hoy?'),
(1, 'She said hello to everyone in the room.', 'Ella dijo hola a todos en la sala.'),
(2, 'Goodbye, see you tomorrow!', 'Adiós, ¡nos vemos mañana!'),
(3, 'I need a glass of water.', 'Necesito un vaso de agua.'),
(3, 'Water is essential for life.', 'El agua es esencial para la vida.'),
(4, 'They live in a beautiful house.', 'Viven en una casa hermosa.'),
(4, 'The house has three bedrooms.', 'La casa tiene tres dormitorios.'),
(5, 'I love reading books.', 'Me encanta leer libros.'),
(5, 'This book is very interesting.', 'Este libro es muy interesante.'),
(6, 'I use my computer for work every day.', 'Uso mi computadora para trabajar todos los días.'),
(7, 'What a beautiful sunset!', '¡Qué hermoso atardecer!'),
(7, 'She has a beautiful voice.', 'Ella tiene una voz hermosa.'),
(8, 'This is an important meeting.', 'Esta es una reunión importante.'),
(9, 'I want to learn Spanish.', 'Quiero aprender español.'),
(10, 'Please speak slowly.', 'Por favor, habla despacio.'),
(11, 'Do you understand the instructions?', '¿Entiendes las instrucciones?'),
(12, 'You need to practice every day.', 'Necesitas practicar todos los días.'),
(13, 'He runs very quickly.', 'Él corre muy rápidamente.'),
(14, 'She always arrives on time.', 'Ella siempre llega a tiempo.'),
(15, 'It was raining; however, we went out.', 'Estaba lloviendo; sin embargo, salimos.');

-- Insert word translations
INSERT INTO word_translations (word_id, translation, language, context) VALUES
(1, 'hola', 'es', 'Common greeting'),
(2, 'adiós', 'es', 'Common farewell'),
(3, 'agua', 'es', 'Essential liquid'),
(4, 'casa', 'es', 'Place to live'),
(5, 'libro', 'es', 'Reading material'),
(6, 'computadora', 'es', 'Electronic device'),
(7, 'hermoso/hermosa', 'es', 'Attractive appearance'),
(8, 'importante', 'es', 'Significant value'),
(9, 'aprender', 'es', 'Acquire knowledge'),
(10, 'hablar', 'es', 'Articulate words'),
(11, 'entender', 'es', 'Comprehend meaning'),
(12, 'practicar', 'es', 'Repeat to improve'),
(13, 'rápidamente', 'es', 'Fast speed'),
(14, 'siempre', 'es', 'Every occasion'),
(15, 'sin embargo', 'es', 'Despite that');

-- Insert word collections for each user
INSERT INTO word_collections (user_id, name, description, type, is_public) VALUES
(1, 'Admin Collection', 'Collection for testing purposes', 'PERSONAL', false),
(2, 'Basic Spanish', 'Essential Spanish vocabulary for beginners', 'PERSONAL', true),
(2, 'Daily Conversation', 'Words used in everyday conversations', 'PERSONAL', false),
(3, 'Inglés Básico', 'Basic English words for Spanish speakers', 'PERSONAL', true),
(4, 'English Learning', 'Essential English vocabulary', 'PERSONAL', false);

-- Add words to collections
INSERT INTO collection_words (collection_id, word_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), -- Basic Spanish
(3, 1), (3, 2), (3, 7), (3, 8), (3, 9), -- Daily Conversation
(4, 1), (4, 2), (4, 3), (4, 7), (4, 8), -- Inglés Básico
(5, 1), (5, 2), (5, 6), (5, 9), (5, 10); -- English Learning

-- Insert learning progress for users
INSERT INTO learning_progress (user_id, word_id, mastery_level, correct_answers, total_attempts, consecutive_correct, ease_factor, interval_days, next_review_date, last_reviewed) VALUES
(2, 1, 'FAMILIAR', 5, 6, 3, 2.5, 3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 'FAMILIAR', 4, 5, 2, 2.3, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 'MASTERED', 8, 8, 5, 2.8, 7, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4, 'LEARNING', 2, 4, 1, 2.0, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 5, 'NEW', 0, 0, 0, 2.5, 0, NOW(), NULL),
(3, 1, 'MASTERED', 10, 10, 8, 3.0, 14, DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3, 'FAMILIAR', 6, 7, 4, 2.6, 4, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 7, 'LEARNING', 3, 5, 2, 2.2, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 1, 'LEARNING', 1, 3, 1, 2.1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 6, 'NEW', 0, 0, 0, 2.5, 0, NOW(), NULL);

-- Insert sample study sessions
INSERT INTO study_sessions (user_id, collection_id, study_mode, started_at, completed_at, total_words, correct_answers, duration) VALUES
(2, 2, 'FLASHCARDS', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 5, 4, 300),
(2, 3, 'QUIZ', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 5, 3, 450),
(3, 4, 'TYPING', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 5, 5, 600),
(4, 5, 'FLASHCARDS', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 5, 2, 400);

-- Insert study session results
INSERT INTO study_session_results (study_session_id, word_id, is_correct, response_time, answer_given) VALUES
(1, 1, true, 5000, 'hola'),
(1, 2, false, 8000, 'hasta luego'),
(1, 3, true, 3000, 'agua'),
(1, 4, true, 4000, 'casa'),
(1, 5, true, 6000, 'libro'),
(2, 1, true, 4000, 'hola'),
(2, 2, true, 5000, 'adiós'),
(2, 7, false, 10000, 'bueno'),
(2, 8, true, 3000, 'importante'),
(2, 9, false, 12000, 'estudiar'),
(3, 1, true, 3000, 'hello'),
(3, 2, true, 2000, 'goodbye'),
(3, 3, true, 2500, 'water'),
(3, 7, true, 4000, 'beautiful'),
(3, 8, true, 3500, 'important'),
(4, 1, true, 6000, 'hello'),
(4, 2, false, 15000, 'good night'),
(4, 6, false, 20000, 'book'),
(4, 9, true, 8000, 'learn'),
(4, 10, false, 25000, 'listen');

-- Insert user statistics
INSERT INTO user_statistics (user_id, total_words_learned, total_study_sessions, total_study_time, average_accuracy, current_streak, longest_streak, last_activity_date) VALUES
(1, 0, 0, 0, 0.0, 0, 0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 2, 750, 0.7, 2, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 1, 600, 1.0, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 0, 1, 400, 0.4, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;