package com.memorizewords.exception;

/**
 * Exception thrown when attempting to create a duplicate word.
 */
public class DuplicateWordException extends RuntimeException {

    public DuplicateWordException(String message) {
        super(message);
    }

    public DuplicateWordException(String word, String language) {
        super(String.format("Word '%s' already exists in language '%s'", word, language));
    }
}