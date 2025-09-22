package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Word example entity for storing usage examples.
 *
 * Represents example sentences or phrases that demonstrate
 * how a word is used in context.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "word_examples")
@EntityListeners(AuditingEntityListener.class)
public class WordExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @NotBlank(message = "Example text is required")
    @Column(name = "example_text", nullable = false, columnDefinition = "TEXT")
    private String exampleText;

    @Column(name = "translation", columnDefinition = "TEXT")
    private String translation;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public WordExample() {
    }

    public WordExample(Word word, String exampleText) {
        this.word = word;
        this.exampleText = exampleText;
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

    public String getExampleText() {
        return exampleText;
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WordExample{" +
                "id=" + id +
                ", exampleText='" + exampleText + '\'' +
                ", translation='" + translation + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}