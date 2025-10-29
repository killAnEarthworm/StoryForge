package com.linyuan.storyforge.enums;

/**
 * 时间线事件类型枚举
 * 定义故事时间线上可能发生的各种事件类型
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum TimelineEventType {

    /**
     * 对话 - 角色间的对话交流
     */
    DIALOGUE("dialogue", "对话", "角色间的语言交流和信息传递"),

    /**
     * 行动 - 角色的具体行动
     */
    ACTION("action", "行动", "角色的具体行为和动作"),

    /**
     * 内心变化 - 角色的心理活动和情感变化
     */
    INNER_CHANGE("inner_change", "内心变化", "角色的心理状态、情绪或想法的转变"),

    /**
     * 环境变化 - 场景或环境的变化
     */
    ENVIRONMENT_CHANGE("environment_change", "环境变化", "场景、天气、氛围等环境因素的改变"),

    /**
     * 冲突 - 矛盾和对抗
     */
    CONFLICT("conflict", "冲突", "角色间或角色与环境的矛盾冲突"),

    /**
     * 发现 - 发现重要信息或真相
     */
    DISCOVERY("discovery", "发现", "揭示重要信息、线索或真相"),

    /**
     * 决定 - 重要的决策
     */
    DECISION("decision", "决定", "角色做出的重要选择或决策"),

    /**
     * 转折 - 情节的重大转折
     */
    TURNING_POINT("turning_point", "转折", "故事发展方向的重大转变"),

    /**
     * 成长 - 角色的成长和改变
     */
    GROWTH("growth", "成长", "角色能力、性格或认知的提升"),

    /**
     * 失去 - 失去重要的人或物
     */
    LOSS("loss", "失去", "角色失去重要的人、物或能力"),

    /**
     * 获得 - 获得新的能力、物品或关系
     */
    GAIN("gain", "获得", "角色获得新的能力、物品或建立新关系"),

    /**
     * 相遇 - 角色初次相遇
     */
    ENCOUNTER("encounter", "相遇", "角色间的首次接触或重要会面"),

    /**
     * 分离 - 角色的分别
     */
    SEPARATION("separation", "分离", "角色间的分别或关系的中断"),

    /**
     * 危机 - 危险或紧急情况
     */
    CRISIS("crisis", "危机", "面临的危险、威胁或紧急状况"),

    /**
     * 高潮 - 故事的高潮部分
     */
    CLIMAX("climax", "高潮", "故事情节的最高峰"),

    /**
     * 解决 - 问题或冲突的解决
     */
    RESOLUTION("resolution", "解决", "矛盾的化解或问题的解决"),

    /**
     * 其他 - 其他类型的事件
     */
    OTHER("other", "其他", "未分类的特殊事件");

    /**
     * 事件类型代码
     */
    private final String code;

    /**
     * 中文显示名称
     */
    private final String displayName;

    /**
     * 描述
     */
    private final String description;

    TimelineEventType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 事件类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应类型
     */
    public static TimelineEventType fromCode(String code) {
        for (TimelineEventType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown timeline event type code: " + code);
    }

    /**
     * 判断是否为关键事件类型
     * 关键事件包括：转折、高潮、危机、决定、发现
     *
     * @return 是否为关键事件
     */
    public boolean isKeyEvent() {
        return this == TURNING_POINT
                || this == CLIMAX
                || this == CRISIS
                || this == DECISION
                || this == DISCOVERY;
    }

    /**
     * 判断是否为角色相关事件
     * 角色相关事件包括：成长、内心变化、相遇、分离、失去、获得
     *
     * @return 是否为角色相关事件
     */
    public boolean isCharacterEvent() {
        return this == GROWTH
                || this == INNER_CHANGE
                || this == ENCOUNTER
                || this == SEPARATION
                || this == LOSS
                || this == GAIN;
    }

    /**
     * 判断是否为情节推进事件
     * 情节推进事件包括：行动、冲突、发现、决定、解决
     *
     * @return 是否为情节推进事件
     */
    public boolean isPlotEvent() {
        return this == ACTION
                || this == CONFLICT
                || this == DISCOVERY
                || this == DECISION
                || this == RESOLUTION;
    }

    /**
     * 获取推荐的记忆重要度（1-10）
     *
     * @return 推荐的重要度分数
     */
    public int getRecommendedImportance() {
        return switch (this) {
            case CLIMAX -> 10;
            case TURNING_POINT, CRISIS -> 9;
            case DECISION, DISCOVERY, LOSS -> 8;
            case GROWTH, GAIN -> 7;
            case CONFLICT, RESOLUTION -> 6;
            case ENCOUNTER, SEPARATION -> 5;
            case INNER_CHANGE -> 4;
            case ACTION, DIALOGUE -> 3;
            case ENVIRONMENT_CHANGE -> 2;
            case OTHER -> 1;
        };
    }
}
