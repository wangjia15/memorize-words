package com.memorizewords.entity;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.LearningMode;
import com.memorizewords.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Learning session entity representing user learning sessions.
 */
@Entity
@Table(name = "learning_sessions")
@Data
@EqualsAndHashCode(callSuper = true)
public class LearningSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_list_id")
    private VocabularyList vocabularyList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "total_words", nullable = false)
    private Integer totalWords = 0;

    @Column(name = "completed_words")
    private Integer completedWords = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "current_word_index")
    private Integer currentWordIndex = 0;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @Column(name = "duration") // Duration in seconds
    private Long duration;

    @Column(name = "accuracy") // Accuracy percentage
    private Double accuracy;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "is_paused")
    private Boolean isPaused = false;

    // Session settings stored as JSON
    @Column(name = "auto_advance")
    private Boolean autoAdvance = true;

    @Column(name = "show_definition_first")
    private Boolean showDefinitionFirst = false;

    @Column(name = "enable_pronunciation")
    private Boolean enablePronunciation = true;

    @Column(name = "enable_hints")
    private Boolean enableHints = true;

    @Column(name = "time_limit") // Time limit per word in seconds
    private Integer timeLimit;

    @Column(name = "shuffle_words")
    private Boolean shuffleWords = true;

    @Column(name = "repeat_incorrect")
    private Boolean repeatIncorrect = true;

    // Words included in this session
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "learning_session_words",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "word_id")
    )
    private Set<Word> words = new HashSet<>();

    // Answers submitted during this session
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningAnswer> answers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (totalWords == null) {
            totalWords = words.size();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (status == SessionStatus.COMPLETED && endTime == null) {
            endTime = LocalDateTime.now();
            isCompleted = true;
        }
        if (status == SessionStatus.PAUSED) {
            isPaused = true;
            if (pausedAt == null) {
                pausedAt = LocalDateTime.now();
            }
        } else {
            isPaused = false;
        }
    }

    public void addWord(Word word) {
        words.add(word);
        totalWords = words.size();
    }

    public void removeWord(Word word) {
        words.remove(word);
        totalWords = words.size();
    }

    public void addAnswer(LearningAnswer answer) {
        answers.add(answer);
        answer.setSession(this);
    }

    public void removeAnswer(LearningAnswer answer) {
        answers.remove(answer);
        answer.setSession(null);
    }

    public Double getAccuracyPercentage() {
        if (answers.isEmpty()) {
            return 0.0;
        }
        long correctCount = answers.stream()
            .mapToLong(answer -> answer.getIsCorrect() ? 1 : 0)
            .sum();
        return (double) correctCount / answers.size() * 100;
    }

    public Long getTotalTimeSpent() {
        return answers.stream()
            .mapToLong(LearningAnswer::getTimeSpent)
            .sum();
    }

    public Double getAverageTimePerWord() {
        if (answers.isEmpty()) {
            return 0.0;
        }
        return (double) getTotalTimeSpent() / answers.size();
    }
}