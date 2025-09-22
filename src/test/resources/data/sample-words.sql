-- Sample vocabulary data for testing
-- This file can be used to populate test data for integration tests

-- Words table sample data
INSERT INTO words (word, definition, pronunciation, part_of_speech, difficulty, source_language, target_language, created_at, updated_at) VALUES
('hello', 'A greeting used when meeting someone', 'həˈloʊ', 'INTERJECTION', 'BEGINNER', 'en', 'es', NOW(), NOW()),
('world', 'The earth or globe, as a planet', 'wɜːrld', 'NOUN', 'BEGINNER', 'en', 'es', NOW(), NOW()),
('spring', 'The season between winter and summer', 'sprɪŋ', 'NOUN', 'INTERMEDIATE', 'en', 'es', NOW(), NOW()),
('learn', 'To acquire knowledge or skills through study', 'lɜːrn', 'VERB', 'BEGINNER', 'en', 'es', NOW(), NOW()),
('study', 'The devotion of time and attention to gaining knowledge', 'ˈstʌdi', 'NOUN', 'BEGINNER', 'en', 'es', NOW(), NOW()),
('memory', 'The faculty of the mind to store and recall information', 'ˈmeməri', 'NOUN', 'INTERMEDIATE', 'en', 'es', NOW(), NOW()),
('algorithm', 'A process or set of rules to be followed in calculations', 'ˈælɡərɪðəm', 'NOUN', 'ADVANCED', 'en', 'es', NOW(), NOW()),
('database', 'A structured set of data held in a computer', 'ˈdeɪtəbeɪs', 'NOUN', 'INTERMEDIATE', 'en', 'es', NOW(), NOW()),
('application', 'A program or software designed to perform a specific task', 'ˌæplɪˈkeɪʃən', 'NOUN', 'INTERMEDIATE', 'en', 'es', NOW(), NOW()),
('framework', 'A supporting structure or system', 'ˈfreɪmwɜːrk', 'NOUN', 'ADVANCED', 'en', 'es', NOW(), NOW());

-- Word examples table sample data
INSERT INTO word_examples (word_id, example_sentence, translation, created_at) VALUES
(1, 'Hello, how are you today?', 'Hola, ¿cómo estás hoy?', NOW()),
(2, 'The world is beautiful', 'El mundo es hermoso', NOW()),
(3, 'Spring is my favorite season', 'La primavera es mi estación favorita', NOW()),
(4, 'I want to learn Spanish', 'Quiero aprender español', NOW()),
(5, 'Study hard for your exams', 'Estudia duro para tus exámenes', NOW());

-- Word translations table sample data
INSERT INTO word_translations (word_id, translation, target_language, created_at) VALUES
(1, 'hola', 'es', NOW()),
(2, 'mundo', 'es', NOW()),
(3, 'primavera', 'es', NOW()),
(4, 'aprender', 'es', NOW()),
(5, 'estudiar', 'es', NOW()),
(6, 'memoria', 'es', NOW()),
(7, 'algoritmo', 'es', NOW()),
(8, 'base de datos', 'es', NOW()),
(9, 'aplicación', 'es', NOW()),
(10, 'marco', 'es', NOW());