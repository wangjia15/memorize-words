package com.memorizewords.enums;

/**
 * Review outcome enumeration for spaced repetition algorithm.
 */
public enum ReviewOutcome {
    AGAIN,     // Card needs immediate review (incorrect answer)
    HARD,      // Card was difficult but correct
    GOOD,      // Card was answered correctly with some effort
    EASY       // Card was answered correctly very easily
}