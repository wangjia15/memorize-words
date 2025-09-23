package com.memorizewords.dto.response;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.LearningMode;
import com.memorizewords.enums.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for learning session data.
 */
@Data
public class LearningSessionDto {

    private Long id;

    private Long userId;

    private String username;

    private Long vocabularyListId;

    private String vocabularyListName;

    private LearningMode mode;

    private DifficultyLevel difficulty;

    private SessionStatus status;

    private Integer totalWords;

    private Integer completedWords;

    private Integer correctAnswers;

    private Integer currentWordIndex;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime pausedAt;

    private LocalDateTime resumedAt;

    private Long duration; // Duration in seconds

    private Double accuracy; // Accuracy percentage

    private Boolean isCompleted;

    private Boolean isPaused;

    // Session settings
    private Boolean autoAdvance;

    private Boolean showDefinitionFirst;

    private Boolean enablePronunciation;

    private Boolean enableHints;

    private Integer timeLimit;

    private Boolean shuffleWords;

    private Boolean repeatIncorrect;

    // Words in the session
    private List<LearningWordDto> words;

    // Progress metrics
    private Double progressPercentage;

    private Long totalTimeSpent;

    private Double averageTimePerWord;

    private Integer streakCount;

    private String performanceCategory;

    public Double getProgressPercentage() {
        if (totalWords == null || totalWords == 0) {
            return 0.0;
        }
        return (double) (completedWords != null ? completedWords : 0) / totalWords * 100;
    }

    public Double getAccuracyPercentage() {
        if (completedWords == null || completedWords == 0) {
            return 0.0;
        }
        return (double) (correctAnswers != null ? correctAnswers : 0) / completedWords * 100;
    }
}