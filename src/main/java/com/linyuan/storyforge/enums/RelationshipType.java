package com.linyuan.storyforge.enums;

/**
 * 角色关系类型枚举
 * 定义角色之间的关系类别
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum RelationshipType {

    /**
     * 家族关系 - 血缘关系
     */
    FAMILY("family", "家族", "父母、兄弟姐妹、子女等"),

    /**
     * 友谊 - 朋友关系
     */
    FRIENDSHIP("friendship", "友谊", "朋友、密友、知己"),

    /**
     * 爱情 - 浪漫关系
     */
    ROMANCE("romance", "爱情", "恋人、配偶、暗恋对象"),

    /**
     * 敌对 - 对抗关系
     */
    RIVALRY("rivalry", "敌对", "仇人、对手、敌人"),

    /**
     * 师徒 - 教导关系
     */
    MENTORSHIP("mentorship", "师徒", "师父与徒弟、导师与学生"),

    /**
     * 同事 - 工作关系
     */
    COLLEAGUE("colleague", "同事", "同事、合作伙伴"),

    /**
     * 上下级 - 权力关系
     */
    HIERARCHICAL("hierarchical", "上下级", "上司与下属、领导与追随者"),

    /**
     * 陌生 - 初次相遇或不熟悉
     */
    STRANGER("stranger", "陌生", "陌生人、初次见面"),

    /**
     * 复杂 - 难以定义的复杂关系
     */
    COMPLEX("complex", "复杂", "多重关系、矛盾关系");

    /**
     * 关系类型标识符
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

    RelationshipType(String code, String description, String detail) {
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
     * @param code 关系类型标识符
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的关系类型
     */
    public static RelationshipType fromCode(String code) {
        for (RelationshipType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relationship type code: " + code);
    }
}
