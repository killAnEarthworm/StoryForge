package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * GenerationHistory DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationHistoryDTO {

    private UUID id;

    @NotNull(message = "Project ID cannot be null")
    private UUID projectId;

    private String generationType; // chapter/dialogue/scene_description
    private UUID targetId; // 关联的章节/对话等ID

    private String promptTemplate; // 使用的提示词模板
    private Map<String, Object> promptVariables; // 填充的变量
    private String fullPrompt; // 完整提示词

    private String modelName;
    private Map<String, Object> modelParameters; // temperature, max_tokens等

    private String generatedResult;
    private Float qualityScore; // 质量评分
    private String userFeedback;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
