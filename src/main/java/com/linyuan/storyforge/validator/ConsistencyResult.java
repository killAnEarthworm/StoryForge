package com.linyuan.storyforge.validator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * ConsistencyResult - 一致性验证结果
 * 包含各项一致性检查的得分和建议
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsistencyResult {

    /**
     * 整体一致性得分 (0.0-1.0)
     */
    @Builder.Default
    private Double overallScore = 0.0;

    /**
     * 性格向量匹配得分 (0.0-1.0)
     */
    @Builder.Default
    private Double vectorScore = 0.0;

    /**
     * 语言模式是否匹配
     */
    @Builder.Default
    private Boolean speechPatternValid = true;

    /**
     * 行为模式是否匹配
     */
    @Builder.Default
    private Boolean behaviorPatternValid = true;

    /**
     * 一致性违规列表
     */
    @Builder.Default
    private List<String> violations = new ArrayList<>();

    /**
     * AI深度验证建议
     */
    private String aiSuggestions;

    /**
     * 验证通过
     */
    @Builder.Default
    private Boolean passed = true;

    /**
     * 详细说明
     */
    private String details;

    /**
     * 判断是否需要深度AI验证
     * 当基础验证得分较低或发现违规时需要
     *
     * @return true如果需要深度验证
     */
    public boolean needsDeepValidation() {
        return overallScore < 0.7 || !violations.isEmpty();
    }

    /**
     * 添加违规项
     *
     * @param violation 违规描述
     */
    public void addViolation(String violation) {
        if (this.violations == null) {
            this.violations = new ArrayList<>();
        }
        this.violations.add(violation);
        this.passed = false;
    }

    /**
     * 判断是否通过验证
     * 综合考虑得分和违规项
     *
     * @param threshold 通过阈值
     * @return true如果通过
     */
    public boolean isPassed(double threshold) {
        return overallScore >= threshold && violations.isEmpty();
    }

    /**
     * 获取验证等级
     *
     * @return 等级描述
     */
    public String getValidationLevel() {
        if (overallScore >= 0.9) {
            return "优秀";
        } else if (overallScore >= 0.7) {
            return "良好";
        } else if (overallScore >= 0.5) {
            return "一般";
        } else {
            return "较差";
        }
    }
}
