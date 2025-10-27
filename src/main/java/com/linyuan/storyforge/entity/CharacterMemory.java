package com.linyuan.storyforge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Character Memory entity - for maintaining character consistency
 */
@Data
@Entity
@Table(name = "character_memories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterMemory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private Timeline timeline;

    @Column(name = "memory_type", length = 50)
    private String memoryType;

    @Column(name = "memory_content", columnDefinition = "TEXT")
    private String memoryContent;

    @Column(name = "emotional_weight")
    private Float emotionalWeight;

    @Column(columnDefinition = "text[]")
    private List<String> keywords;

    @Column(name = "related_characters", columnDefinition = "uuid[]")
    private List<UUID> relatedCharacters;

    @Column(name = "related_locations", columnDefinition = "uuid[]")
    private List<UUID> relatedLocations;

    @Column(name = "accessibility")
    private Float accessibility;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "access_count")
    private Integer accessCount;

    public void updateAccessibility() {
        if (lastAccessed == null) {
            this.accessibility = 1.0f;
            return;
        }
        long daysSinceAccess = ChronoUnit.DAYS.between(lastAccessed, LocalDateTime.now());
        if (daysSinceAccess == 0) {
            return;
        }
        int count = (accessCount != null) ? accessCount : 0;
        double memoryStrength = 7.0 + Math.log(1 + count) * 3.0;
        double retention = Math.exp(-daysSinceAccess / memoryStrength);
        float weight = (emotionalWeight != null) ? emotionalWeight : 0.5f;
        this.accessibility = (float) (retention * weight);
        if (this.accessibility < 0.0f) {
            this.accessibility = 0.0f;
        } else if (this.accessibility > 1.0f) {
            this.accessibility = 1.0f;
        }
    }

    public void recordAccess() {
        this.lastAccessed = LocalDateTime.now();
        if (this.accessCount == null) {
            this.accessCount = 0;
        }
        this.accessCount++;
        float recoveryRate = (float) (1.0 / (1.0 + Math.log(1 + accessCount) * 0.1));
        float currentAccessibility = (accessibility != null) ? accessibility : 0.5f;
        this.accessibility = Math.min(1.0f, currentAccessibility + recoveryRate * 0.3f);
    }

    public boolean isAccessible(float threshold) {
        if (this.accessibility == null) {
            return false;
        }
        return this.accessibility >= threshold;
    }

    public float getImportanceScore() {
        float typeWeight = 0.5f;
        if (memoryType != null) {
            switch (memoryType.toUpperCase()) {
                case "CORE":
                case "核心记忆":
                    typeWeight = 1.0f;
                    break;
                case "EMOTIONAL":
                case "情感记忆":
                    typeWeight = 0.9f;
                    break;
                case "SKILL":
                case "技能记忆":
                    typeWeight = 0.7f;
                    break;
                case "EPISODIC":
                case "情节记忆":
                    typeWeight = 0.6f;
                    break;
                case "SEMANTIC":
                case "语义记忆":
                    typeWeight = 0.5f;
                    break;
            }
        }
        float emoWeight = (emotionalWeight != null) ? emotionalWeight : 0.5f;
        float access = (accessibility != null) ? accessibility : 0.5f;
        return typeWeight * 0.4f + emoWeight * 0.3f + access * 0.3f;
    }
}
