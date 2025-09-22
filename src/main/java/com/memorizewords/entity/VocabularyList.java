package com.memorizewords.entity;

import com.memorizewords.enum.ListType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Vocabulary list entity for organizing words into collections.
 */
@Entity
@Table(name = "vocabulary_lists")
@Data
@EqualsAndHashCode(callSuper = true)
public class VocabularyList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vocabulary_list_words",
        joinColumns = @JoinColumn(name = "list_id"),
        inverseJoinColumns = @JoinColumn(name = "word_id")
    )
    private Set<Word> words = new HashSet<>();

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "is_shared")
    private Boolean isShared = false;

    @ElementCollection
    @CollectionTable(name = "vocabulary_list_tags", joinColumns = @JoinColumn(name = "list_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ListType type = ListType.CUSTOM;

    @Column(name = "word_count")
    private Integer wordCount = 0;

    @PrePersist
    @PreUpdate
    public void updateWordCount() {
        this.wordCount = words != null ? words.size() : 0;
    }
}