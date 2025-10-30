package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GenerationRequest - 统一的生成请求
 * 支持章节、对话、场景等多种内容类型的生成
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRequest {

    /**
     * 项目ID
     */
    private UUID projectId;

    /**
     * 世界观ID（可选）
     */
    private UUID worldviewId;

    /**
     * 参与角色ID列表
     */
    private List<UUID> characterIds;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 场景描述/上下文
     */
    private String sceneContext;

    /**
     * 情感基调
     */
    private String emotionalTone;

    /**
     * 生成目标/具体要求
     */
    private String generationGoal;

    /**
     * 前文内容（用于章节生成的连贯性）
     */
    private String previousContent;

    /**
     * 是否启用记忆系统
     */
    @Builder.Default
    private boolean enableMemory = true;

    /**
     * 每个角色检索的记忆数量
     */
    @Builder.Default
    private int memoryCount = 5;

    /**
     * 是否进行一致性验证
     */
    @Builder.Default
    private boolean enableConsistencyCheck = true;

    /**
     * 最大重试次数（一致性验证失败时）
     */
    @Builder.Default
    private int maxRetries = 2;

    /**
     * AI生成参数 - 温度
     */
    @Builder.Default
    private Double temperature = 0.8;

    /**
     * AI生成参数 - 最大Token数
     */
    @Builder.Default
    private Integer maxTokens = 2000;

    /**
     * 额外参数
     */
    private Map<String, Object> additionalParams;

    /**
     * 章节编号（用于章节生成）
     */
    private Integer chapterNumber;

    /**
     * 时间线ID（可选）
     */
    private UUID timelineId;

    /**
     * 是否自动创建记忆
     */
    @Builder.Default
    private boolean autoCreateMemory = true;

    /**
     * 验证是否有效
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (projectId == null) {
            return false;
        }
        if (contentType == null) {
            return false;
        }
        if (characterIds == null || characterIds.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取主要角色ID
     *
     * @return 第一个角色ID
     */
    public UUID getPrimaryCharacterId() {
        if (characterIds == null || characterIds.isEmpty()) {
            return null;
        }
        return characterIds.get(0);
    }
}
