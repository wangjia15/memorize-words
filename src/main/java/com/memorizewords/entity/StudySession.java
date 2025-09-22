package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Study session entity for tracking user study activities.
 *
 * Represents a study session where users practice learning words
 * through different study modes like flashcards, quizzes, etc.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "study_sessions")
@EntityListeners(AuditingEntityListener.class)
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private WordCollection collection;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_mode", nullable = false)
    private StudyMode studyMode = StudyMode.FLASHCARDS;

    @CreatedDate
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_words")
    private Integer totalWords = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "duration")
    private Integer duration = 0; // duration in seconds

    @OneToMany(mappedBy = "studySession", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudySessionResult> results = new HashSet<>();

    public enum StudyMode {
        FLASHCARDS, QUIZ, TYPING, LISTENING
    }

    // Constructors
    public StudySession() {
    }

    public StudySession(User user, StudyMode studyMode) {
        this.user = user;
        this.studyMode = studyMode;
    }

    // Helper methods
    public void complete() {
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.duration = (int) java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public double getAccuracy() {
        if (totalWords == 0) return 0.0;
        return (double) correctAnswers / totalWords;
    }

    public void addResult(boolean isCorrect) {
        this.totalWords++;
        if (isCorrect) {
            this.correctAnswers++;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WordCollection getCollection() {
        return collection;
    }

    public void setCollection(WordCollection collection) {
        this.collection = collection;
    }

    public StudyMode getStudyMode() {
        return studyMode;
    }

    public void setStudyMode(StudyMode studyMode) {
        this.studyMode = studyMode;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(Integer totalWords) {
        this.totalWords = totalWords;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Set<StudySessionResult> getResults() {
        return results;
    }

    public void setResults(Set<StudySessionResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "StudySession{" +
                "id=" + id +
                ", studyMode=" + studyMode +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", totalWords=" + totalWords +
                ", correctAnswers=" + correctAnswers +
                ", duration=" + duration +
                '}';
    }
}