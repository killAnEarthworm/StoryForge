package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CharacterRelationship DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterRelationshipDTO {

    private UUID id;

    @NotNull(message = "Character A ID cannot be null")
    private UUID characterAId;

    @NotNull(message = "Character B ID cannot be null")
    private UUID characterBId;

    private String relationshipType; // 父子/朋友/敌人/恋人等
    private String relationshipDescription;
    private List<String> tensionPoints; // 冲突点
    private String sharedHistory;
    private Map<String, Object> dynamicState; // 关系动态变化

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
