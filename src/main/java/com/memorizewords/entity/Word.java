package com.memorizewords.entity;

import com.memorizewords.enums.DifficultyLevel;
import com.memorizewords.enums.WordCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Word entity representing vocabulary words.
 */
@Entity
@Table(name = "words")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Word extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 50)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(length = 200)
    private String pronunciation;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "word_categories", joinColumns = @JoinColumn(name = "word_id"))
    @Column(name = "category")
    private Set<WordCategory> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "word_tags", joinColumns = @JoinColumn(name = "word_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserWordProgress> userProgress = new ArrayList<>();

    @ManyToMany(mappedBy = "words")
    private Set<VocabularyList> vocabularyLists = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void normalizeWord() {
        if (word != null) {
            this.word = word.toLowerCase().trim();
        }
    }
}