package com.memorizewords.dto.response;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.ReviewOutcome;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for spaced repetition card information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetitionCardDTO {

    private Long id;

    private Long userId;

    private WordDto word;

    private Integer intervalDays;

    private BigDecimal easeFactor;

    private LocalDateTime dueDate;

    private LocalDateTime nextReview;

    private LocalDateTime lastReviewed;

    private Integer totalReviews;

    private Integer correctReviews;

    private Integer consecutiveCorrect;

    private Integer consecutiveIncorrect;

    private DifficultyLevel difficultyLevel;

    private BigDecimal performanceIndex;

    private Integer averageResponseTime;

    private Boolean isActive;

    private Boolean isSuspended;

    private BigDecimal stabilityFactor;

    private Long totalStudyTime;

    private ReviewOutcome lastReviewOutcome;

    private Integer reviewCountAgain;

    private Integer reviewCountHard;

    private Integer reviewCountGood;

    private Integer reviewCountEasy;

    private Integer cardAgeDays;

    private BigDecimal retentionRate;

    private Boolean isDue;

    private Boolean isNew;

    private Boolean isDifficult;

    private Double difficultyRating;

    private List<String> reviewHistory;
}