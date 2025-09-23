package com.memorizewords.dto.response;

import com.memorizewords.enums.ReviewMode;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for review session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSessionDTO {

    private Long id;

    private Long userId;

    private ReviewMode mode;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalCards;

    private Integer completedCards;

    private Integer correctAnswers;

    private Integer averageResponseTime;

    private Boolean isCompleted;

    private Double sessionAccuracy;

    private Duration sessionDuration;

    private Integer cardsPerMinute;

    private Double totalSessionScore;

    private Double efficiencyScore;

    private Integer newCardsLearned;

    private Integer difficultCardsMastered;

    private Double learningVelocity;

    private List<ReviewSessionCardDTO> cards;

    private Integer currentCardIndex;

    private ReviewSessionCardDTO currentCard;

    private Integer remainingCards;

    private Double progressPercentage;
}