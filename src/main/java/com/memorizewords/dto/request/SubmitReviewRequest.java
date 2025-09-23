package com.memorizewords.dto.request;

import com.memorizewords.enums.ReviewOutcome;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for submitting a review answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotNull(message = "Review outcome is required")
    private ReviewOutcome outcome;

    @Min(value = 100, message = "Response time must be at least 100ms")
    @Max(value = 300000, message = "Response time cannot exceed 5 minutes")
    @NotNull(message = "Response time is required")
    private Integer responseTime;

    private String userAnswer;

    private Integer hintUsed;

    private String confidenceLevel;

    private Boolean markedAsDifficult;

    private String notes;
}