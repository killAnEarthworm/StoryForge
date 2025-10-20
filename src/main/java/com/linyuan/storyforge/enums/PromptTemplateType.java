package com.linyuan.storyforge.enums;

/**
 * 提示词模板类型枚举
 * 定义系统中所有支持的AI提示词模板类型
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum PromptTemplateType {

    /**
     * 角色创建 - 基于关键词生成完整角色设定
     */
    CHARACTER_CREATION("character-creation", "角色创建"),

    /**
     * 角色对话生成 - 生成符合角色性格的对话内容
     */
    CHARACTER_DIALOGUE("character-dialogue", "角色对话"),

    /**
     * 场景描写 - 生成沉浸式场景描述
     */
    SCENE_DESCRIPTION("scene-description", "场景描写"),

    /**
     * 章节生成大纲 - 生成章节结构和场景列表
     */
    CHAPTER_OUTLINE("chapter-outline", "章节大纲"),

    /**
     * 章节场景扩写 - 将场景大纲扩展为完整内容
     */
    CHAPTER_SCENE_EXPANSION("chapter-scene-expansion", "场景扩写"),

    /**
     * 对话优化 - 优化对话的个性化和潜台词
     */
    DIALOGUE_REFINEMENT("dialogue-refinement", "对话优化"),

    /**
     * 冲突处理 - 处理角色间冲突和矛盾
     */
    CONFLICT_RESOLUTION("conflict-resolution", "冲突处理"),

    /**
     * 世界观扩展 - 扩展世界观细节和规则
     */
    WORLDVIEW_EXPANSION("worldview-expansion", "世界观扩展"),

    /**
     * 多角色互动 - 生成多个角色的协同场景
     */
    MULTI_CHARACTER_INTERACTION("multi-character-interaction", "多角色互动"),

    /**
     * 角色一致性检查 - 验证角色行为是否符合设定
     */
    CHARACTER_CONSISTENCY_CHECK("character-consistency-check", "一致性检查"),

    /**
     * 内容质量评估 - 评估生成内容的质量
     */
    QUALITY_EVALUATION("quality-evaluation", "质量评估");

    /**
     * 模板标识符 - 对应配置文件中的key
     */
    private final String templateKey;

    /**
     * 中文描述
     */
    private final String description;

    PromptTemplateType(String templateKey, String description) {
        this.templateKey = templateKey;
        this.description = description;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据模板key获取枚举
     *
     * @param templateKey 模板标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的模板类型
     */
    public static PromptTemplateType fromTemplateKey(String templateKey) {
        for (PromptTemplateType type : values()) {
            if (type.templateKey.equals(templateKey)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown template key: " + templateKey);
    }
}
