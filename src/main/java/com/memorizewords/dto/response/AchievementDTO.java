package com.memorizewords.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for achievement information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {

    private Long id;

    private String name;

    private String description;

    private String category;

    private String icon;

    private LocalDateTime unlockedAt;

    private Integer progress;

    private Integer target;

    private Boolean isUnlocked;

    private String rarity; // COMMON, RARE, EPIC, LEGENDARY

    private Integer points;

    private String badgeColor;
}