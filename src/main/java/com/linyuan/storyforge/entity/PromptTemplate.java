package com.linyuan.storyforge.entity;

import lombok.AllArgsConstructor;

import javax.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Prompt Template entity - for AI generation
 */
@Data
@Entity
@Table(name = "prompt_templates")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String category; // character/scene/dialogue/chapter

    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent; // 模板内容，使用 {{variable}} 占位符

    @Column(name = "required_variables", columnDefinition = "text[]")
    private List<String> requiredVariables; // 必需的变量列表

    @Column(name = "optional_variables", columnDefinition = "text[]")
    private List<String> optionalVariables; // 可选的变量列表

    @Column(name = "example_usage", columnDefinition = "TEXT")
    private String exampleUsage;

    @Column(name = "effectiveness_score")
    private Float effectivenessScore; // 效果评分

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
