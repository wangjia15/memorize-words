package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Word translation entity for storing translations of words.
 *
 * Represents translations of words in different languages,
 * with optional context information.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "word_translations")
@EntityListeners(AuditingEntityListener.class)
public class WordTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @NotBlank(message = "Translation is required")
    @Size(min = 1, max = 500, message = "Translation must be between 1 and 500 characters")
    @Column(name = "translation", nullable = false, length = 500)
    private String translation;

    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 10, message = "Language must be between 2 and 10 characters")
    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public WordTranslation() {
    }

    public WordTranslation(Word word, String translation, String language) {
        this.word = word;
        this.translation = translation;
        this.language = language;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WordTranslation{" +
                "id=" + id +
                ", translation='" + translation + '\'' +
                ", language='" + language + '\'' +
                ", context='" + context + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}