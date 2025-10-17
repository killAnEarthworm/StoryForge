package com.linyuan.storyforge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private String memoryType; // 事件/知识/情感/技能

    @Column(name = "memory_content", columnDefinition = "TEXT")
    private String memoryContent;

    @Column(name = "emotional_weight")
    private Float emotionalWeight; // 情感权重

    // 记忆检索
    @Column(columnDefinition = "text[]")
    private List<String> keywords; // 关键词用于检索

    @Column(name = "related_characters", columnDefinition = "uuid[]")
    private List<UUID> relatedCharacters;

    @Column(name = "related_locations", columnDefinition = "uuid[]")
    private List<UUID> relatedLocations;

    @Column(name = "accessibility")
    private Float accessibility; // 记忆可访问性（遗忘曲线）

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "access_count")
    private Integer accessCount;
}
