package com.linyuan.storyforge.enums;

/**
 * 角色记忆类型枚举
 * 定义角色记忆的五层分类体系
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum MemoryType {

    /**
     * 核心记忆 - 最重要的记忆,定义角色的本质
     * 例如:童年创伤、人生转折、核心信念
     */
    CORE("core", "核心记忆", 1.0f),

    /**
     * 情感记忆 - 强烈情感关联的事件
     * 例如:爱恨情仇、重要的人际互动
     */
    EMOTIONAL("emotional", "情感记忆", 0.9f),

    /**
     * 技能记忆 - 习得的能力和经验
     * 例如:战斗技巧、专业知识、生活技能
     */
    SKILL("skill", "技能记忆", 0.7f),

    /**
     * 情节记忆 - 具体发生的事件
     * 例如:某次对话、某个场景、某件事情
     */
    EPISODIC("episodic", "情节记忆", 0.6f),

    /**
     * 语义记忆 - 一般性知识和概念
     * 例如:世界观知识、社会常识、文化背景
     */
    SEMANTIC("semantic", "语义记忆", 0.5f);

    /**
     * 记忆类型标识符
     */
    private final String code;

    /**
     * 中文描述
     */
    private final String description;

    /**
     * 默认重要性权重 (0.0-1.0)
     */
    private final Float defaultWeight;

    MemoryType(String code, String description, Float defaultWeight) {
        this.code = code;
        this.description = description;
        this.defaultWeight = defaultWeight;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Float getDefaultWeight() {
        return defaultWeight;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 记忆类型标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的记忆类型
     */
    public static MemoryType fromCode(String code) {
        for (MemoryType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown memory type code: " + code);
    }
}
