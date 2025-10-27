package com.linyuan.storyforge.enums;

/**
 * AI生成模式枚举
 * 定义不同的故事生成模式
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum GenerationMode {

    /**
     * 章节模式 - 生成完整的故事章节
     */
    CHAPTER("chapter", "章节模式", "生成完整的故事章节,包含多个场景"),

    /**
     * 短篇模式 - 生成独立的短故事
     */
    SHORT_STORY("short_story", "短篇模式", "生成完整的短篇故事"),

    /**
     * 场景模式 - 生成单个场景
     */
    SCENE("scene", "场景模式", "生成单个场景描写"),

    /**
     * 对话模式 - 生成角色对话
     */
    DIALOGUE("dialogue", "对话模式", "生成角色间的对话"),

    /**
     * 大纲模式 - 生成故事大纲
     */
    OUTLINE("outline", "大纲模式", "生成章节大纲和场景列表"),

    /**
     * 扩写模式 - 扩展现有内容
     */
    EXPANSION("expansion", "扩写模式", "基于大纲或提纲扩展细节"),

    /**
     * 续写模式 - 接续之前的内容
     */
    CONTINUATION("continuation", "续写模式", "基于前文继续写作"),

    /**
     * 改写模式 - 重写现有内容
     */
    REWRITE("rewrite", "改写模式", "改写和优化现有内容");

    /**
     * 生成模式标识符
     */
    private final String code;

    /**
     * 中文描述
     */
    private final String description;

    /**
     * 详细说明
     */
    private final String detail;

    GenerationMode(String code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 生成模式标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的生成模式
     */
    public static GenerationMode fromCode(String code) {
        for (GenerationMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown generation mode code: " + code);
    }
}
