package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.validator.ConsistencyResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GenerationResult - 生成结果
 * 包含生成的内容、验证结果、记忆创建情况等完整信息
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResult {

    /**
     * 生成的内容
     */
    private String generatedContent;

    /**
     * 是否成功
     */
    @Builder.Default
    private boolean success = false;

    /**
     * 生成时间
     */
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    /**
     * 角色一致性验证结果列表（多个角色）
     */
    @Builder.Default
    private List<ConsistencyResult> characterConsistencyResults = new ArrayList<>();

    /**
     * 世界观一致性验证结果
     */
    private ConsistencyResult worldviewConsistencyResult;

    /**
     * 是否通过所有一致性验证
     */
    @Builder.Default
    private boolean passedAllValidation = false;

    /**
     * 重试次数
     */
    @Builder.Default
    private int retryCount = 0;

    /**
     * 使用的记忆数量
     */
    @Builder.Default
    private int memoriesUsed = 0;

    /**
     * 新创建的记忆ID列表
     */
    @Builder.Default
    private List<UUID> newMemoryIds = new ArrayList<>();

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 生成耗时（毫秒）
     */
    private Long durationMs;

    /**
     * Token使用量
     */
    private Integer tokensUsed;

    /**
     * 详细日志
     */
    @Builder.Default
    private List<String> logs = new ArrayList<>();

    /**
     * 添加日志
     *
     * @param log 日志信息
     */
    public void addLog(String log) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(String.format("[%s] %s", LocalDateTime.now(), log));
    }

    /**
     * 添加新记忆ID
     *
     * @param memoryId 记忆ID
     */
    public void addNewMemoryId(UUID memoryId) {
        if (this.newMemoryIds == null) {
            this.newMemoryIds = new ArrayList<>();
        }
        this.newMemoryIds.add(memoryId);
    }

    /**
     * 添加角色一致性验证结果
     *
     * @param result 验证结果
     */
    public void addCharacterConsistencyResult(ConsistencyResult result) {
        if (this.characterConsistencyResults == null) {
            this.characterConsistencyResults = new ArrayList<>();
        }
        this.characterConsistencyResults.add(result);
    }

    /**
     * 获取最低的一致性得分
     *
     * @return 最低得分
     */
    public double getLowestConsistencyScore() {
        double lowest = 1.0;

        if (characterConsistencyResults != null) {
            for (ConsistencyResult result : characterConsistencyResults) {
                if (result.getOverallScore() < lowest) {
                    lowest = result.getOverallScore();
                }
            }
        }

        if (worldviewConsistencyResult != null &&
                worldviewConsistencyResult.getOverallScore() < lowest) {
            lowest = worldviewConsistencyResult.getOverallScore();
        }

        return lowest;
    }

    /**
     * 获取所有验证违规项
     *
     * @return 违规项列表
     */
    public List<String> getAllViolations() {
        List<String> allViolations = new ArrayList<>();

        if (characterConsistencyResults != null) {
            for (ConsistencyResult result : characterConsistencyResults) {
                if (result.getViolations() != null) {
                    allViolations.addAll(result.getViolations());
                }
            }
        }

        if (worldviewConsistencyResult != null &&
                worldviewConsistencyResult.getViolations() != null) {
            allViolations.addAll(worldviewConsistencyResult.getViolations());
        }

        return allViolations;
    }

    /**
     * 判断是否需要重试
     *
     * @return true如果需要重试
     */
    public boolean needsRetry() {
        return !passedAllValidation && !getAllViolations().isEmpty();
    }
}
