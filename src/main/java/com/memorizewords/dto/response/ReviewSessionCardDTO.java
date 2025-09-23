package com.memorizewords.dto.response;

import com.memorizewords.enums.ReviewOutcome;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for review session card information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSessionCardDTO {

    private Long id;

    private Long sessionId;

    private SpacedRepetitionCardDTO card;

    private ReviewOutcome reviewOutcome;

    private Integer responseTime;

    private LocalDateTime reviewTime;

    private Integer intervalBeforeReview;

    private Integer intervalAfterReview;

    private BigDecimal easeFactorBeforeReview;

    private BigDecimal easeFactorAfterReview;

    private Integer reviewNumber;

    private Integer consecutiveCorrectBefore;

    private Boolean wasCorrect;

    private Double score;

    private String userAnswer;

    private Integer hintUsed;

    private String confidenceLevel;

    private Boolean markedAsDifficult;

    private String notes;

    private Boolean isNewCard;

    private Boolean wasDifficult;

    private Boolean isCompleted;
}