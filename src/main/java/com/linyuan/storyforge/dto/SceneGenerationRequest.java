package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.enums.SceneType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 场景生成请求DTO
 * 包含场景生成所需的所有参数和配置
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SceneGenerationRequest {

    // ==================== 基础信息 ====================

    /**
     * 所属项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private UUID projectId;

    /**
     * 所属世界观ID（可选）
     * 如果提供，将使用世界观的规则和设定来约束场景生成
     */
    private UUID worldviewId;

    /**
     * 所属章节ID（可选）
     * 如果提供，场景将关联到该章节
     */
    private UUID chapterId;

    // ==================== 场景类型和定位 ====================

    /**
     * 场景类型
     */
    @NotNull(message = "场景类型不能为空")
    private SceneType sceneType;

    /**
     * 场景目的
     * 例如: "展示角色成长"、"推进主线剧情"、"揭示真相"、"营造紧张氛围"
     */
    private String scenePurpose;

    /**
     * 场景标题（可选）
     * 如果不提供，AI将自动生成
     */
    private String sceneTitle;

    // ==================== 场景设定 ====================

    /**
     * 地点
     * 例如: "城堡大厅"、"森林深处"、"废弃工厂"、"咖啡馆"
     */
    @NotNull(message = "地点不能为空")
    private String location;

    /**
     * 时间（一天中的时刻）
     * 例如: "黎明"、"正午"、"黄昏"、"午夜"、"深夜"
     */
    private String timeOfDay;

    /**
     * 天气
     * 例如: "晴朗"、"阴雨"、"暴风雪"、"雾天"、"烈日"
     */
    private String weather;

    /**
     * 季节（可选）
     * 例如: "春天"、"夏日"、"深秋"、"寒冬"
     */
    private String season;

    // ==================== 氛围和情绪 ====================

    /**
     * 情绪基调
     * 例如: "紧张"、"温馨"、"诡异"、"激动"、"压抑"、"轻松"
     */
    @NotNull(message = "情绪基调不能为空")
    private String mood;

    /**
     * 氛围描述
     * 例如: "压抑的"、"轻松的"、"神秘的"、"欢快的"、"阴森的"
     */
    private String atmosphere;

    /**
     * 期望的情感强度 (1-10)
     * 1表示平淡，10表示极端强烈
     */
    @Min(value = 1, message = "情感强度最小为1")
    @Max(value = 10, message = "情感强度最大为10")
    @Builder.Default
    private Integer emotionalIntensity = 5;

    // ==================== 参与角色 ====================

    /**
     * 参与角色ID列表
     * 场景中会根据这些角色的设定和记忆来生成内容
     */
    private List<UUID> characterIds;

    /**
     * 角色关系描述
     * 例如: "主角与导师的首次对话"、"敌对双方的对峙"、"老友重逢"
     */
    private String characterRelations;

    /**
     * 主要视角角色ID（可选）
     * 如果提供，场景将主要从该角色的视角展开
     */
    private UUID perspectiveCharacterId;

    // ==================== 情节要素 ====================

    /**
     * 情节上下文
     * 描述场景发生的背景和前因
     * 例如: "主角刚刚发现了真相，正在寻找证据"
     */
    private String plotContext;

    /**
     * 关键事件列表
     * 场景中必须发生的事件
     * 例如: ["发现密室", "找到古老的卷轴", "触发了机关"]
     */
    private List<String> keyEvents;

    /**
     * 冲突点（可选）
     * 场景中的主要矛盾或冲突
     * 例如: "主角与守卫的对峙"、"内心的道德困境"
     */
    private String conflict;

    /**
     * 前置场景ID（可选）
     * 如果提供，将考虑前置场景的内容来保持连贯性
     */
    private UUID previousSceneId;

    // ==================== 感官细节要求 ====================

    /**
     * 是否包含详细的视觉细节
     * 包括色彩、光影、形态、动作等
     */
    @Builder.Default
    private Boolean includeVisualDetails = true;

    /**
     * 是否包含详细的听觉细节
     * 包括声音、音调、节奏等
     */
    @Builder.Default
    private Boolean includeAuditoryDetails = true;

    /**
     * 是否包含嗅觉细节
     * 包括气味、香气等
     */
    @Builder.Default
    private Boolean includeOlfactory = false;

    /**
     * 是否包含触觉细节
     * 包括质感、温度、触感等
     */
    @Builder.Default
    private Boolean includeTactile = false;

    /**
     * 是否包含味觉细节（用于饮食场景）
     */
    @Builder.Default
    private Boolean includeTaste = false;

    /**
     * 感官侧重点
     * 可以指定特别强调哪些感官
     * 例如: ["视觉", "听觉"]，如果不指定则根据场景类型自动选择
     */
    private List<String> sensoryFocus;

    // ==================== 长度和风格 ====================

    /**
     * 目标字数
     * 生成场景的目标长度
     */
    @Min(value = 100, message = "目标字数最小为100")
    @Max(value = 3000, message = "目标字数最大为3000")
    @Builder.Default
    private Integer targetWordCount = 600;

    /**
     * 写作风格
     * 例如: "细腻"、"简洁"、"诗意"、"写实"、"戏剧性"、"悬疑"
     */
    @Builder.Default
    private String writingStyle = "细腻";

    /**
     * 叙事节奏
     * 例如: "快速"、"缓慢"、"跌宕起伏"、"平稳"
     */
    private String narrativePace;

    /**
     * 描写密度
     * LOW: 简洁明快，少量描写
     * MEDIUM: 适度描写，平衡叙事
     * HIGH: 细致入微，大量细节
     */
    @Builder.Default
    private DescriptionDensity descriptionDensity = DescriptionDensity.MEDIUM;

    // ==================== AI参数 ====================

    /**
     * AI创意度 (0.0-1.0)
     * 越高越富有创意和想象力，越低越保守和可预测
     * 推荐值: 动作场景0.6-0.7, 情感场景0.7-0.8, 描写场景0.8-0.9
     */
    @Min(value = 0, message = "创意度最小为0.0")
    @Max(value = 1, message = "创意度最大为1.0")
    @Builder.Default
    private Double creativity = 0.75;

    // ==================== 约束条件 ====================

    /**
     * 必须包含的元素
     * 场景中必须出现的物品、动作、台词等
     * 例如: ["古老的剑", "\"我会回来的\"这句话", "突然的雷声"]
     */
    private List<String> mustInclude;

    /**
     * 必须避免的元素
     * 场景中不能出现的内容
     * 例如: ["血腥描写", "暴力", "不适合儿童的内容"]
     */
    private List<String> mustAvoid;

    /**
     * 语言限制
     * 例如: ["不使用现代网络用语", "避免过度文言", "口语化表达"]
     */
    private List<String> languageConstraints;

    // ==================== 其他选项 ====================

    /**
     * 是否生成内心独白
     * 如果为true且有视角角色，将包含角色的内心活动
     */
    @Builder.Default
    private Boolean includeInnerMonologue = false;

    /**
     * 是否包含环境互动
     * 角色与环境的互动细节
     */
    @Builder.Default
    private Boolean includeEnvironmentInteraction = true;

    /**
     * 是否生成对话
     * 如果为false，场景将主要是叙述和描写，不包含对话
     */
    @Builder.Default
    private Boolean includeDialogue = true;

    /**
     * 对话比例 (0.0-1.0)
     * 0表示纯描写，1表示大部分是对话
     * 仅在includeDialogue=true时有效
     */
    @Min(value = 0, message = "对话比例最小为0.0")
    @Max(value = 1, message = "对话比例最大为1.0")
    @Builder.Default
    private Double dialogueRatio = 0.3;

    /**
     * 是否提取时间线事件
     * 如果为true，将自动从生成的场景中提取关键事件添加到时间线
     */
    @Builder.Default
    private Boolean extractTimelineEvents = true;

    /**
     * 备注（可选）
     * 用户的额外说明或特殊要求
     */
    private String notes;

    /**
     * 描写密度枚举
     */
    public enum DescriptionDensity {
        /**
         * 低密度 - 简洁明快
         */
        LOW,

        /**
         * 中密度 - 适度描写
         */
        MEDIUM,

        /**
         * 高密度 - 细致入微
         */
        HIGH
    }

    /**
     * 验证请求的有效性
     *
     * @return 验证消息，如果为null表示验证通过
     */
    public String validate() {
        // 检查字数范围
        if (targetWordCount != null && (targetWordCount < 100 || targetWordCount > 3000)) {
            return "目标字数必须在100-3000之间";
        }

        // 检查创意度范围
        if (creativity != null && (creativity < 0.0 || creativity > 1.0)) {
            return "创意度必须在0.0-1.0之间";
        }

        // 检查情感强度范围
        if (emotionalIntensity != null && (emotionalIntensity < 1 || emotionalIntensity > 10)) {
            return "情感强度必须在1-10之间";
        }

        // 检查对话比例范围
        if (dialogueRatio != null && (dialogueRatio < 0.0 || dialogueRatio > 1.0)) {
            return "对话比例必须在0.0-1.0之间";
        }

        // 如果有视角角色，必须在参与角色列表中
        if (perspectiveCharacterId != null && characterIds != null
                && !characterIds.isEmpty() && !characterIds.contains(perspectiveCharacterId)) {
            return "视角角色必须在参与角色列表中";
        }

        return null; // 验证通过
    }

    /**
     * 获取简化的场景描述
     * 用于日志或展示
     *
     * @return 场景简述
     */
    public String getSimpleDescription() {
        return String.format("[%s] %s场景 - %s (%s)",
                sceneType.getDisplayName(),
                location,
                mood,
                targetWordCount + "字");
    }
}
