package com.memorizewords.enums;

/**
 * Review mode enumeration for different types of review sessions.
 */
public enum ReviewMode {
    DUE_CARDS,         // Review cards that are due for review
    DIFFICULT_CARDS,   // Review cards that are marked as difficult
    RANDOM_REVIEW,     // Random review of available cards
    NEW_CARDS,         // Review new cards that haven't been studied
    TARGETED_REVIEW,   // Review specific cards or categories
    ALL_CARDS          // Review all available cards
}