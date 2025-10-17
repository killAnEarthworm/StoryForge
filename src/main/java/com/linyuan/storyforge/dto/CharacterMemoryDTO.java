package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CharacterMemory DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterMemoryDTO {

    private UUID id;

    @NotNull(message = "Character ID cannot be null")
    private UUID characterId;

    private UUID timelineId;

    private String memoryType; // 事件/知识/情感/技能
    private String memoryContent;
    private Float emotionalWeight; // 情感权重

    // 记忆检索
    private List<String> keywords; // 关键词用于检索
    private List<UUID> relatedCharacters;
    private List<UUID> relatedLocations;

    private Float accessibility; // 记忆可访问性（遗忘曲线）默认 1.0
    private LocalDateTime lastAccessed;
    private Integer accessCount; // 默认 0

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
