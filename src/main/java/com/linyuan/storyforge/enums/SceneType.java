package com.linyuan.storyforge.enums;

/**
 * 场景类型枚举
 * 不同类型对应不同的场景生成侧重点和感官描写策略
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
public enum SceneType {

    /**
     * 动作场景 - 战斗、追逐、冒险等动作场景
     */
    ACTION(
            "action",
            "动作",
            "节奏紧凑、动作描写、紧张刺激",
            "紧张、激烈、快节奏",
            "视觉、听觉、触觉"
    ),

    /**
     * 对话场景 - 角色间的对话交流场景
     */
    DIALOGUE(
            "dialogue",
            "对话",
            "角色互动、语言交锋、信息传递",
            "真诚、紧张、轻松、严肃",
            "听觉、视觉（表情和动作）"
    ),

    /**
     * 描写场景 - 环境、景物、氛围描写
     */
    DESCRIPTION(
            "description",
            "描写",
            "环境细节、氛围营造、意境渲染",
            "宁静、壮丽、荒凉、神秘",
            "视觉、听觉、嗅觉"
    ),

    /**
     * 情感场景 - 情感表达、心理活动场景
     */
    EMOTIONAL(
            "emotional",
            "情感",
            "情绪表达、内心独白、情感起伏",
            "温馨、悲伤、愤怒、喜悦、恐惧",
            "视觉（微表情）、内心感受"
    ),

    /**
     * 冲突场景 - 矛盾、对抗、紧张场景
     */
    CONFLICT(
            "conflict",
            "冲突",
            "矛盾激化、对立冲突、紧张对峙",
            "压迫、对抗、紧张、凝重",
            "视觉、听觉、心理感受"
    ),

    /**
     * 过渡场景 - 场景转换、时空过渡
     */
    TRANSITION(
            "transition",
            "过渡",
            "承上启下、时空转换、节奏调整",
            "流畅、自然、连贯",
            "视觉、时间感"
    ),

    /**
     * 高潮场景 - 故事高潮、关键转折
     */
    CLIMAX(
            "climax",
            "高潮",
            "情节顶点、情绪爆发、重大转折",
            "震撼、激动、紧张、释放",
            "全感官（视觉、听觉、触觉、心理）"
    ),

    /**
     * 开场场景 - 章节或故事的开场
     */
    OPENING(
            "opening",
            "开场",
            "引入情境、设定基调、吸引注意",
            "吸引人、设定氛围",
            "视觉、听觉、氛围感"
    ),

    /**
     * 结尾场景 - 章节或故事的结尾
     */
    ENDING(
            "ending",
            "结尾",
            "收束情节、余韵留存、情感升华",
            "回味、余韵、完结感、期待",
            "情感、意境"
    ),

    /**
     * 日常场景 - 日常生活、平淡场景
     */
    DAILY(
            "daily",
            "日常",
            "生活细节、日常互动、平淡真实",
            "平和、真实、温馨、琐碎",
            "视觉、生活细节"
    );

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

    /**
     * 氛围关键词
     */
    private final String atmosphereKeywords;

    /**
     * 感官侧重点
     */
    private final String sensoryFocus;

    SceneType(String code, String displayName, String features,
              String atmosphereKeywords, String sensoryFocus) {
        this.code = code;
        this.displayName = displayName;
        this.features = features;
        this.atmosphereKeywords = atmosphereKeywords;
        this.sensoryFocus = sensoryFocus;
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

    public String getAtmosphereKeywords() {
        return atmosphereKeywords;
    }

    public String getSensoryFocus() {
        return sensoryFocus;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应类型
     */
    public static SceneType fromCode(String code) {
        for (SceneType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown scene type code: " + code);
    }

    /**
     * 获取场景类型的生成要求
     *
     * @return 该类型的具体生成要求
     */
    public String getGenerationRequirements() {
        return switch (this) {
            case ACTION -> """
                    - 快节奏的动作描写，使用短句增强紧张感
                    - 详细描写动作细节（招式、移动、碰撞）
                    - 突出视觉和触觉感受（速度感、冲击感）
                    - 使用动态动词，避免静态描写
                    - 注意动作的连贯性和合理性
                    - 适当加入环境互动（利用地形、物品）
                    """;
            case DIALOGUE -> """
                    - 对话内容符合角色性格和说话方式
                    - 包含潜台词和隐含信息
                    - 适当加入动作和表情描写（避免纯对话）
                    - 对话要推进情节或揭示信息
                    - 注意对话节奏和张力变化
                    - 体现角色间的关系和立场
                    """;
            case DESCRIPTION -> """
                    - 丰富的感官细节（视觉、听觉、嗅觉）
                    - 使用比喻和意象增强画面感
                    - 营造整体氛围和情绪基调
                    - 避免单调的罗列，注重层次和重点
                    - 结合环境与情节的关联
                    - 适度的诗意表达，不过度修辞
                    """;
            case EMOTIONAL -> """
                    - 细腻的情感描写和心理活动
                    - 通过细节展现情绪（微表情、动作、生理反应）
                    - 内心独白要真实自然
                    - 情感转变要有合理的触发点
                    - 避免直白的情感宣泄，注重暗示
                    - 结合环境烘托情绪
                    """;
            case CONFLICT -> """
                    - 明确的对立双方和矛盾焦点
                    - 逐步升级的紧张氛围
                    - 通过对话、动作、心理展现冲突
                    - 注意冲突的合理性和必然性
                    - 环境细节强化压迫感
                    - 留有余地，不要一次性爆发完
                    """;
            case TRANSITION -> """
                    - 流畅的场景或时间过渡
                    - 简洁明了，不拖沓
                    - 可以包含简短的环境描写或心理活动
                    - 为下一场景做好铺垫
                    - 保持叙事节奏的连贯性
                    - 必要时标注时间或地点变化
                    """;
            case CLIMAX -> """
                    - 高密度的情节和情感冲击
                    - 全方位的感官刺激（视觉、听觉、触觉、心理）
                    - 情绪达到顶点，制造震撼效果
                    - 快节奏的叙述，增强紧迫感
                    - 重大转折或揭示，超出预期
                    - 为后续发展埋下伏笔
                    """;
            case OPENING -> """
                    - 吸引注意力的开场（悬念、冲突、奇景）
                    - 快速建立场景和氛围
                    - 简洁介绍关键信息（人物、地点、情境）
                    - 设定整体基调和风格
                    - 引发读者好奇和期待
                    - 避免冗长的背景说明
                    """;
            case ENDING -> """
                    - 收束本段情节，留有余韵
                    - 情感或情节的升华
                    - 可以是开放式结尾或闭合式结尾
                    - 呼应前文的伏笔或主题
                    - 适度的意境渲染
                    - 为下一阶段留下悬念或期待（如果不是最终结尾）
                    """;
            case DAILY -> """
                    - 真实的生活细节和日常互动
                    - 平实自然的语言风格
                    - 通过日常展现角色性格和关系
                    - 适度的温馨或趣味元素
                    - 避免过于平淡，要有小的波澜或趣味点
                    - 可以为后续情节埋下伏笔
                    """;
        };
    }

    /**
     * 获取感官描写指导
     *
     * @return 该场景类型的感官描写建议
     */
    public String getSensoryGuidance() {
        return switch (this) {
            case ACTION -> """
                    视觉: 动作轨迹、身体姿态、武器光影、环境破坏
                    听觉: 兵器碰撞、呼吸喘息、脚步声、环境响动
                    触觉: 冲击力、疼痛感、肌肉紧绷、风压
                    """;
            case DIALOGUE -> """
                    听觉: 语调、音量、语气变化、说话节奏
                    视觉: 表情变化、眼神交流、肢体语言、动作细节
                    心理: 对方话语引发的内心反应
                    """;
            case DESCRIPTION -> """
                    视觉: 色彩、光影、形状、远近层次、细节纹理
                    听觉: 自然声音、环境音、寂静的质感
                    嗅觉: 空气中的气味、植物香气、特殊气息
                    触觉: 温度、湿度、质感（如需接触）
                    """;
            case EMOTIONAL -> """
                    视觉: 微表情、眼神、细微动作、身体姿态
                    心理: 内心独白、情绪波动、记忆闪回
                    生理: 心跳、呼吸、颤抖、面部发热等身体反应
                    """;
            case CONFLICT -> """
                    视觉: 对峙姿态、紧张表情、环境压迫感
                    听觉: 沉默的压力、激烈的语言交锋、环境杂音
                    心理: 对抗心理、压力感、警觉性
                    触觉: 空气中的紧张感、汗水、肌肉紧绷
                    """;
            case TRANSITION -> """
                    视觉: 场景变化、光线变化、环境转换
                    时间感: 时间流逝的标志（日升日落、季节变化）
                    心理: 角色心境的过渡
                    """;
            case CLIMAX -> """
                    全感官冲击: 同时调动视觉、听觉、触觉、心理
                    视觉: 震撼性画面、极端环境、强烈对比
                    听觉: 巨响、震耳欲聋、或极端寂静
                    触觉: 强烈的物理感受、疼痛、冲击
                    心理: 情绪的极端体验、认知的颠覆
                    """;
            case OPENING -> """
                    视觉: 第一印象的画面、吸引眼球的场景
                    听觉: 特殊的声音引入
                    氛围: 通过感官快速建立基调
                    """;
            case ENDING -> """
                    视觉: 象征性画面、意境性描写
                    心理: 情感的沉淀、思考的空间
                    氛围: 余韵的营造、情绪的收束
                    """;
            case DAILY -> """
                    视觉: 日常生活的真实细节
                    听觉: 生活中的常见声音
                    嗅觉: 生活气息（食物、环境）
                    触觉: 日常接触的物品质感
                    心理: 平常心态下的小感受
                    """;
        };
    }

    /**
     * 获取写作风格建议
     *
     * @return 该场景类型的写作风格建议
     */
    public String getStyleGuidance() {
        return switch (this) {
            case ACTION -> "短句为主，节奏紧凑，动词丰富，避免冗长描写";
            case DIALOGUE -> "对话自然，符合角色性格，适当加入动作和表情，避免纯对话";
            case DESCRIPTION -> "细腻、诗意，注重层次和意境，使用比喻和意象";
            case EMOTIONAL -> "细腻、真实，注重内心活动和细节，避免直白宣泄";
            case CONFLICT -> "紧张、有张力，逐步升级，注重对立和压迫感";
            case TRANSITION -> "简洁、流畅，承上启下，不拖沓";
            case CLIMAX -> "高密度、高强度，全方位感官冲击，制造震撼";
            case OPENING -> "吸引人、简洁有力，快速建立基调，引发好奇";
            case ENDING -> "余韵悠长、升华主题，可开放可闭合，呼应前文";
            case DAILY -> "平实、自然、真实，有生活气息，不过于平淡";
        };
    }
}
