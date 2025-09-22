package com.memorizewords.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Word entity representing vocabulary entries in the system.
 *
 * Stores word information including definition, pronunciation,
 * part of speech, difficulty level, and language information.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Entity
@Table(name = "words")
@EntityListeners(AuditingEntityListener.class)
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Word is required")
    @Size(min = 1, max = 200, message = "Word must be between 1 and 200 characters")
    @Column(name = "word", nullable = false, length = 200)
    private String word;

    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    @Column(name = "pronunciation", columnDefinition = "TEXT")
    private String pronunciation;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech")
    private PartOfSpeech partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty = Difficulty.BEGINNER;

    @Column(name = "source_language", length = 10)
    private String sourceLanguage = "en";

    @Column(name = "target_language", length = 10)
    private String targetLanguage = "es";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WordExample> examples = new HashSet<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WordTranslation> translations = new HashSet<>();

    @ManyToMany(mappedBy = "words")
    private Set<WordCollection> collections = new HashSet<>();

    public enum PartOfSpeech {
        NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION, DETERMINER, OTHER
    }

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // Constructors
    public Word() {
    }

    public Word(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<WordExample> getExamples() {
        return examples;
    }

    public void setExamples(Set<WordExample> examples) {
        this.examples = examples;
    }

    public Set<WordTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<WordTranslation> translations) {
        this.translations = translations;
    }

    public Set<WordCollection> getCollections() {
        return collections;
    }

    public void setCollections(Set<WordCollection> collections) {
        this.collections = collections;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", partOfSpeech=" + partOfSpeech +
                ", difficulty=" + difficulty +
                ", sourceLanguage='" + sourceLanguage + '\'' +
                ", targetLanguage='" + targetLanguage + '\'' +
                '}';
    }
}