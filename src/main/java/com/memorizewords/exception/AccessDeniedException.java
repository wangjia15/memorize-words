package com.memorizewords.exception;

/**
 * Exception thrown when access is denied to a resource.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}