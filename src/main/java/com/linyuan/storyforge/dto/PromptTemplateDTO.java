package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PromptTemplate DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateDTO {

    private UUID id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String category; // character/scene/dialogue/chapter

    private String templateContent; // 模板内容，使用 {{variable}} 占位符
    private List<String> requiredVariables; // 必需的变量列表
    private List<String> optionalVariables; // 可选的变量列表

    private String exampleUsage;
    private Float effectivenessScore; // 效果评分

    private Boolean isActive; // 默认 true

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
