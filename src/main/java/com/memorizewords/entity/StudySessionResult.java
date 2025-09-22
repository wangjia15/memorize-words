package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Study session result entity for storing individual answers.
 *
 * Represents the result of a single word answer during a study session,
 * including correctness, response time, and the given answer.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "study_session_results")
@EntityListeners(AuditingEntityListener.class)
public class StudySessionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id", nullable = false)
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @NotNull
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "response_time")
    private Integer responseTime; // response time in milliseconds

    @Column(name = "answer_given", columnDefinition = "TEXT")
    private String answerGiven;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public StudySessionResult() {
    }

    public StudySessionResult(StudySession studySession, Word word, Boolean isCorrect) {
        this.studySession = studySession;
        this.word = word;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudySession getStudySession() {
        return studySession;
    }

    public void setStudySession(StudySession studySession) {
        this.studySession = studySession;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public String getAnswerGiven() {
        return answerGiven;
    }

    public void setAnswerGiven(String answerGiven) {
        this.answerGiven = answerGiven;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StudySessionResult{" +
                "id=" + id +
                ", isCorrect=" + isCorrect +
                ", responseTime=" + responseTime +
                ", answerGiven='" + answerGiven + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}