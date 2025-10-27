package com.linyuan.storyforge.enums;

/**
 * 世界观故事类型枚举
 * 不同类型对应不同的世界观生成侧重点
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum WorldviewGenre {

    /**
     * 奇幻 - 强调魔法体系、种族设定、神话元素
     */
    FANTASY("fantasy", "奇幻", "魔法体系、多种族、神话元素、古代文明"),

    /**
     * 科幻 - 强调科技树、星际政治、未来社会
     */
    SCI_FI("sci-fi", "科幻", "科技发展、太空探索、未来社会、人工智能"),

    /**
     * 武侠 - 强调江湖门派、武功体系、侠义精神
     */
    WUXIA("wuxia", "武侠", "江湖门派、武功体系、侠义精神、恩怨情仇"),

    /**
     * 修真 - 强调修炼境界、法宝体系、宗门设定
     */
    XIANXIA("xianxia", "修真", "修炼境界、法宝丹药、宗门势力、天道规则"),

    /**
     * 现代 - 强调真实社会、地理真实性、当代文化
     */
    MODERN("modern", "现代", "真实社会、现代科技、城市生活、当代文化"),

    /**
     * 历史 - 强调历史真实性、时代背景、文化细节
     */
    HISTORICAL("historical", "历史", "历史真实、时代特征、文化习俗、社会制度"),

    /**
     * 悬疑 - 强调氛围营造、环境细节、隐藏线索
     */
    MYSTERY("mystery", "悬疑", "神秘氛围、环境细节、隐藏线索、心理描写"),

    /**
     * 恐怖 - 强调恐怖元素、压抑氛围、超自然现象
     */
    HORROR("horror", "恐怖", "恐怖元素、压抑氛围、超自然现象、心理惊悚"),

    /**
     * 末世 - 强调生存环境、资源稀缺、废土设定
     */
    POST_APOCALYPTIC("post-apocalyptic", "末世", "废土环境、资源稀缺、生存法则、变异生物"),

    /**
     * 赛博朋克 - 强调高科技低生活、企业统治、网络空间
     */
    CYBERPUNK("cyberpunk", "赛博朋克", "高科技低生活、超级企业、虚拟现实、身体改造"),

    /**
     * 蒸汽朋克 - 强调蒸汽科技、维多利亚时代、机械美学
     */
    STEAMPUNK("steampunk", "蒸汽朋克", "蒸汽动力、维多利亚风格、机械美学、工业革命"),

    /**
     * 都市异能 - 强调超能力、现代背景、隐秘组织
     */
    URBAN_FANTASY("urban-fantasy", "都市异能", "超能力体系、现代社会、隐秘组织、日常与非凡");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 中文名称
     */
    private final String displayName;

    /**
     * 核心特征描述
     */
    private final String features;

    WorldviewGenre(String code, String displayName, String features) {
        this.code = code;
        this.displayName = displayName;
        this.features = features;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFeatures() {
        return features;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应类型
     */
    public static WorldviewGenre fromCode(String code) {
        for (WorldviewGenre genre : values()) {
            if (genre.code.equalsIgnoreCase(code)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown worldview genre code: " + code);
    }

    /**
     * 获取类型的生成要求
     *
     * @return 该类型的具体生成要求
     */
    public String getGenerationRequirements() {
        return switch (this) {
            case FANTASY -> """
                    - 详细的魔法体系（魔力来源、施法规则、魔法分类）
                    - 至少3个主要种族及其特征
                    - 神话传说和创世故事
                    - 魔法物品和神器设定
                    """;
            case SCI_FI -> """
                    - 明确的科技发展水平
                    - 星际政治和势力分布
                    - 太空旅行和通讯方式
                    - AI和机器人的地位
                    """;
            case WUXIA -> """
                    - 武林门派及其武功特色
                    - 江湖规矩和侠义精神
                    - 内功心法体系
                    - 江湖恩怨和历史
                    """;
            case XIANXIA -> """
                    - 完整的修炼境界划分
                    - 法宝和丹药体系
                    - 宗门势力和等级
                    - 天道规则和天劫
                    """;
            case MODERN -> """
                    - 真实的地理环境
                    - 现代社会结构
                    - 当代科技水平
                    - 文化和生活方式
                    """;
            case HISTORICAL -> """
                    - 准确的历史时代背景
                    - 真实的社会制度
                    - 时代特有的文化习俗
                    - 历史事件和人物
                    """;
            case MYSTERY -> """
                    - 神秘的氛围营造
                    - 环境细节描写
                    - 伏笔和线索设计
                    - 心理描写重点
                    """;
            case HORROR -> """
                    - 恐怖元素和来源
                    - 压抑的环境氛围
                    - 超自然现象规则
                    - 恐惧的渐进设计
                    """;
            case POST_APOCALYPTIC -> """
                    - 灾难的具体原因
                    - 废土环境特征
                    - 资源和生存法则
                    - 变异生物设定
                    """;
            case CYBERPUNK -> """
                    - 超级企业和权力结构
                    - 虚拟现实和网络空间
                    - 身体改造技术
                    - 底层与上层的对比
                    """;
            case STEAMPUNK -> """
                    - 蒸汽动力技术
                    - 维多利亚时代特征
                    - 机械美学和发明
                    - 工业革命影响
                    """;
            case URBAN_FANTASY -> """
                    - 超能力类型和限制
                    - 隐秘组织结构
                    - 普通人与异能者的关系
                    - 现代社会的融合
                    """;
        };
    }
}
