package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ChapterGenerationRequest - 章节生成请求
 * 扩展 GenerationRequest，添加章节特定字段
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterGenerationRequest {

    // ========== 基础信息 ==========
    /**
     * 项目ID
     */
    private UUID projectId;

    /**
     * 章节编号
     */
    private Integer chapterNumber;

    /**
     * 章节标题
     */
    private String title;

    // ========== 章节设定 ==========
    /**
     * 章节大纲
     */
    private String outline;

    /**
     * 主要冲突/核心情节
     */
    private String mainConflict;

    /**
     * 参与角色ID列表
     */
    private List<UUID> characterIds;

    /**
     * 世界观ID（可选）
     */
    private UUID worldviewId;

    /**
     * 主要场景ID（可选）
     */
    private UUID mainSceneId;

    /**
     * 时间线ID（可选）
     */
    private UUID timelineId;

    // ========== 生成参数 ==========
    /**
     * 目标字数
     */
    @Builder.Default
    private Integer targetWordCount = 2000;

    /**
     * 基调/氛围（如：紧张、温馨、悲伤）
     */
    private String tone;

    /**
     * 节奏（如：快速、缓慢、跌宕起伏）
     */
    private String pacing;

    /**
     * 情感基调
     */
    private String emotionalTone;

    /**
     * 场景上下文描述
     */
    private String sceneContext;

    /**
     * 生成目标/具体要求
     */
    private String generationGoal;

    // ========== 上下文控制 ==========
    /**
     * 是否加载前文上下文
     */
    @Builder.Default
    private Boolean loadPreviousContext = true;

    /**
     * 前文上下文大小（加载前N章）
     */
    @Builder.Default
    private Integer previousContextSize = 2;

    // ========== 记忆和验证 ==========
    /**
     * 是否启用记忆系统
     */
    @Builder.Default
    private Boolean enableMemory = true;

    /**
     * 每个角色检索的记忆数量
     */
    @Builder.Default
    private Integer memoryCount = 5;

    /**
     * 是否进行一致性验证
     */
    @Builder.Default
    private Boolean enableConsistencyCheck = true;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetries = 2;

    // ========== AI参数 ==========
    /**
     * AI温度参数
     */
    @Builder.Default
    private Double temperature = 0.8;

    /**
     * 最大Token数（根据目标字数自动计算，或手动指定）
     */
    private Integer maxTokens;

    // ========== 版本管理 ==========
    /**
     * 保存为第几版（null表示自动递增）
     */
    private Integer saveAsVersion;

    /**
     * 章节状态（outline/drafted/revised/final）
     */
    @Builder.Default
    private String status = "drafted";

    /**
     * 是否自动创建记忆
     */
    @Builder.Default
    private Boolean autoCreateMemory = true;

    /**
     * 转换为通用的 GenerationRequest
     *
     * @return GenerationRequest
     */
    public GenerationRequest toGenerationRequest() {
        // 自动计算 maxTokens（如果未指定）
        Integer calculatedMaxTokens = maxTokens;
        if (calculatedMaxTokens == null && targetWordCount != null) {
            // 中文：1个字约等于2个token，加上20%冗余
            calculatedMaxTokens = (int) (targetWordCount * 2 * 1.2);
        }
        if (calculatedMaxTokens == null) {
            calculatedMaxTokens = 2000;
        }

        // 构建场景描述
        StringBuilder sceneContextBuilder = new StringBuilder();
        if (outline != null && !outline.isEmpty()) {
            sceneContextBuilder.append("【章节大纲】\n").append(outline).append("\n\n");
        }
        if (mainConflict != null && !mainConflict.isEmpty()) {
            sceneContextBuilder.append("【主要冲突】\n").append(mainConflict).append("\n\n");
        }
        if (sceneContext != null && !sceneContext.isEmpty()) {
            sceneContextBuilder.append("【场景描述】\n").append(sceneContext);
        }

        // 构建生成目标
        StringBuilder goalBuilder = new StringBuilder();
        if (generationGoal != null && !generationGoal.isEmpty()) {
            goalBuilder.append(generationGoal).append("\n\n");
        }
        if (targetWordCount != null) {
            goalBuilder.append(String.format("目标字数：%d字\n", targetWordCount));
        }
        if (tone != null && !tone.isEmpty()) {
            goalBuilder.append(String.format("基调：%s\n", tone));
        }
        if (pacing != null && !pacing.isEmpty()) {
            goalBuilder.append(String.format("节奏：%s\n", pacing));
        }

        return GenerationRequest.builder()
                .projectId(projectId)
                .worldviewId(worldviewId)
                .characterIds(characterIds)
                .contentType(ContentType.CHAPTER)
                .sceneContext(sceneContextBuilder.toString())
                .emotionalTone(emotionalTone)
                .generationGoal(goalBuilder.toString())
                .enableMemory(enableMemory)
                .memoryCount(memoryCount)
                .enableConsistencyCheck(enableConsistencyCheck)
                .maxRetries(maxRetries)
                .temperature(temperature)
                .maxTokens(calculatedMaxTokens)
                .autoCreateMemory(autoCreateMemory)
                .chapterNumber(chapterNumber)
                .timelineId(timelineId)
                .build();
    }

    /**
     * 验证请求有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        if (projectId == null) {
            return false;
        }
        if (chapterNumber == null || chapterNumber < 1) {
            return false;
        }
        if (characterIds == null || characterIds.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息列表
     */
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        if (projectId == null) {
            errors.append("projectId不能为空; ");
        }
        if (chapterNumber == null || chapterNumber < 1) {
            errors.append("chapterNumber必须大于0; ");
        }
        if (characterIds == null || characterIds.isEmpty()) {
            errors.append("characterIds不能为空; ");
        }
        return errors.toString();
    }
}
