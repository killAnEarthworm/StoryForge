package com.linyuan.storyforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegenerateOptions - 重新生成配置
 * 用于指定重新生成时的参数调整
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegenerateOptions {

    /**
     * 修改说明（如：增加动作描写、减少对话、调整节奏等）
     */
    private String changeInstructions;

    /**
     * 是否保留原版本
     */
    @Builder.Default
    private Boolean keepOriginal = true;

    /**
     * 新版本号（null表示自动递增）
     */
    private Integer newVersion;

    /**
     * 是否使用更高的温度参数（更有创意）
     */
    @Builder.Default
    private Boolean moreCreative = false;

    /**
     * 是否使用更低的温度参数（更保守）
     */
    @Builder.Default
    private Boolean moreConservative = false;

    /**
     * 目标字数调整（null表示保持原有）
     */
    private Integer targetWordCount;

    /**
     * 基调调整
     */
    private String tone;

    /**
     * 节奏调整
     */
    private String pacing;

    /**
     * 是否重新检索记忆
     */
    @Builder.Default
    private Boolean refreshMemories = true;

    /**
     * 获取调整后的温度
     *
     * @param originalTemperature 原始温度
     * @return 调整后的温度
     */
    public Double getAdjustedTemperature(Double originalTemperature) {
        if (moreCreative != null && moreCreative) {
            return Math.min(1.0, originalTemperature + 0.2);
        }
        if (moreConservative != null && moreConservative) {
            return Math.max(0.3, originalTemperature - 0.2);
        }
        return originalTemperature;
    }

    /**
     * 构建附加的生成目标
     *
     * @return 附加目标文本
     */
    public String buildAdditionalGoal() {
        StringBuilder goal = new StringBuilder();

        if (changeInstructions != null && !changeInstructions.isEmpty()) {
            goal.append("\n\n## 修改要求\n");
            goal.append(changeInstructions);
        }

        if (tone != null && !tone.isEmpty()) {
            goal.append("\n\n调整基调为：").append(tone);
        }

        if (pacing != null && !pacing.isEmpty()) {
            goal.append("\n调整节奏为：").append(pacing);
        }

        return goal.toString();
    }
}
