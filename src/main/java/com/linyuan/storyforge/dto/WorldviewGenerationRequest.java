package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.enums.WorldviewGenre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 世界观生成请求DTO
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldviewGenerationRequest {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private UUID projectId;

    /**
     * 世界观名称（可选，AI可以生成）
     */
    private String name;

    /**
     * 故事类型
     */
    @NotNull(message = "故事类型不能为空")
    private WorldviewGenre genre;

    /**
     * 关键词列表
     * 例如: ["龙", "魔法学院", "工业革命"]
     */
    @NotNull(message = "关键词不能为空")
    private List<String> keywords;

    /**
     * 额外要求（可选）
     * 用户可以指定特殊要求
     */
    private String additionalRequirements;

    /**
     * 世界规模
     * 例如: "单一星球", "星系", "多元宇宙", "单个大陆", "城市"
     */
    private String worldScale;

    /**
     * 科技/魔法水平
     * 例如: "低魔"、"高魔"、"蒸汽时代"、"星际文明"
     */
    private String powerLevel;

    /**
     * 文明发展阶段
     * 例如: "原始部落", "封建社会", "工业时代", "信息时代", "星际时代"
     */
    private String civilizationStage;

    /**
     * 是否需要生成详细的地理信息
     */
    @Builder.Default
    private Boolean includeDetailedGeography = true;

    /**
     * 是否需要生成详细的历史背景
     */
    @Builder.Default
    private Boolean includeDetailedHistory = true;

    /**
     * 是否需要生成专有名词词典
     */
    @Builder.Default
    private Boolean includeTerminology = true;

    /**
     * AI创意度 (0.0-1.0)
     * 越高越创新，越低越保守
     */
    @Builder.Default
    private Double creativity = 0.8;
}
