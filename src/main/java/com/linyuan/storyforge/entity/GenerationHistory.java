package com.linyuan.storyforge.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.UUID;

/**
 * Generation History entity - for version control and optimization
 */
@Data
@Entity
@Table(name = "generation_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "generation_type", length = 50)
    private String generationType; // chapter/dialogue/scene_description

    @Column(name = "target_id")
    private UUID targetId; // 关联的章节/对话等ID

    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate; // 使用的提示词模板

    @Type(JsonBinaryType.class)
    @Column(name = "prompt_variables", columnDefinition = "jsonb")
    private Map<String, Object> promptVariables; // 填充的变量

    @Column(name = "full_prompt", columnDefinition = "TEXT")
    private String fullPrompt; // 完整提示词

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Type(JsonBinaryType.class)
    @Column(name = "model_parameters", columnDefinition = "jsonb")
    private Map<String, Object> modelParameters; // temperature, max_tokens等

    @Column(name = "generated_result", columnDefinition = "TEXT")
    private String generatedResult;

    @Column(name = "quality_score")
    private Float qualityScore; // 质量评分

    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;
}
