package com.linyuan.storyforge.enums;

/**
 * 内容类型枚举
 * 定义系统中可生成和验证的内容类型
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum ContentType {

    /**
     * 角色设定 - 完整的角色配置
     */
    CHARACTER("character", "角色设定"),

    /**
     * 对话 - 角色间的对话内容
     */
    DIALOGUE("dialogue", "对话"),

    /**
     * 场景描写 - 环境、氛围的描述
     */
    SCENE("scene", "场景描写"),

    /**
     * 叙述 - 第三人称叙事内容
     */
    NARRATIVE("narrative", "叙述"),

    /**
     * 行动 - 角色的动作描述
     */
    ACTION("action", "行动"),

    /**
     * 内心独白 - 角色的心理活动
     */
    INNER_MONOLOGUE("inner_monologue", "内心独白"),

    /**
     * 章节 - 完整的故事章节
     */
    CHAPTER("chapter", "章节"),

    /**
     * 世界观 - 世界观设定内容
     */
    WORLDVIEW("worldview", "世界观");

    /**
     * 内容类型标识符
     */
    private final String code;

    /**
     * 中文描述
     */
    private final String description;

    ContentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 内容类型标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的内容类型
     */
    public static ContentType fromCode(String code) {
        for (ContentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type code: " + code);
    }
}
