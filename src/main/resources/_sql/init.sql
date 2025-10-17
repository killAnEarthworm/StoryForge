-- 1. 项目表（管理不同的创作项目）
CREATE TABLE projects (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          genre VARCHAR(50), -- 科幻/古风/魔幻/悬疑等
                          theme TEXT, -- 主题：牺牲与救赎等
                          writing_style TEXT, -- 写作风格描述
                          status VARCHAR(20) DEFAULT 'draft',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 世界观设定表
CREATE TABLE worldviews (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                            name VARCHAR(255) NOT NULL,

    -- 世界观层次结构
                            universe_laws JSONB, -- 宇宙法则（物理定律、魔法系统等）
                            social_structure JSONB, -- 社会结构（政治、经济、文化）
                            geography JSONB, -- 地理环境（地图、气候、资源）
                            history_background JSONB, -- 历史背景（重大事件、传说）
                            terminology JSONB, -- 专有名词词典

    -- AI辅助字段
                            summary TEXT, -- 世界观概要，用于提示词
                            rules TEXT[], -- 世界规则列表
                            constraints TEXT[], -- 禁忌和限制

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 角色设定表
CREATE TABLE characters (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                            worldview_id UUID REFERENCES worldviews(id),

    -- 基础信息层
                            name VARCHAR(100) NOT NULL,
                            age INTEGER,
                            appearance TEXT,
                            occupation VARCHAR(100),
                            personality_traits VARCHAR(50)[], -- 核心性格特征数组

    -- 深度设定层
                            background_story TEXT,
                            childhood_experience TEXT,
                            important_experiences JSONB, -- [{time, event, impact}]
                            values_beliefs TEXT,
                            fears TEXT[],
                            desires TEXT[],
                            goals TEXT[],

    -- 行为特征
                            speech_pattern TEXT, -- 说话方式
                            behavioral_habits TEXT[], -- 行为习惯
                            catchphrases TEXT[], -- 口癖

    -- 动态属性
                            emotional_state JSONB, -- 当前情绪状态
                            relationships JSONB, -- 与其他角色的关系

    -- AI辅助向量化
                            personality_vector FLOAT[], -- 性格向量化表示
                            character_summary TEXT, -- 角色概要，用于提示词

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 角色关系表
CREATE TABLE character_relationships (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         character_a_id UUID REFERENCES characters(id) ON DELETE CASCADE,
                                         character_b_id UUID REFERENCES characters(id) ON DELETE CASCADE,
                                         relationship_type VARCHAR(50), -- 父子/朋友/敌人/恋人等
                                         relationship_description TEXT,
                                         tension_points TEXT[], -- 冲突点
                                         shared_history TEXT,
                                         dynamic_state JSONB, -- 关系动态变化
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE(character_a_id, character_b_id)
);

-- 5. 时间线表
CREATE TABLE timelines (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                           character_id UUID REFERENCES characters(id) ON DELETE CASCADE,

                           event_time TIMESTAMP, -- 绝对时间
                           relative_time VARCHAR(100), -- 相对时间描述
                           event_type VARCHAR(50), -- 对话/行动/内心变化/环境变化
                           event_description TEXT,

    -- 事件详情
                           participating_characters UUID[], -- 参与的其他角色ID
                           location_id UUID, -- 关联场景
                           emotional_changes JSONB, -- 情绪变化
                           memory_importance INTEGER CHECK (memory_importance BETWEEN 1 AND 10), -- 记忆重要度

    -- 对后续影响
                           consequences TEXT[],
                           character_growth JSONB, -- 角色成长变化

                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_timeline_character ON timelines(character_id);
CREATE INDEX idx_timeline_time ON timelines(event_time);

-- 6. 场景表
CREATE TABLE scenes (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                        worldview_id UUID REFERENCES worldviews(id),

                        name VARCHAR(255) NOT NULL,
                        location_type VARCHAR(50), -- 室内/室外/虚拟等

    -- 场景设定
                        physical_description TEXT,
                        time_setting VARCHAR(100), -- 时间背景
                        atmosphere TEXT, -- 氛围/情绪基调
                        weather VARCHAR(50),
                        lighting VARCHAR(50),

    -- 环境元素
                        available_props JSONB, -- 可用道具
                        environmental_elements TEXT[], -- 环境元素
                        sensory_details JSONB, -- 感官细节（声音、气味等）

    -- AI辅助
                        scene_summary TEXT, -- 场景概要
                        mood_keywords VARCHAR(30)[], -- 情绪关键词

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 故事章节表
CREATE TABLE story_chapters (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                                chapter_number INTEGER NOT NULL,
                                title VARCHAR(255),

    -- 章节设定
                                outline TEXT, -- 章节大纲
                                main_conflict TEXT, -- 主要冲突
                                participating_characters UUID[], -- 参与角色
                                main_scene_id UUID REFERENCES scenes(id),

    -- 生成参数
                                target_word_count INTEGER,
                                tone VARCHAR(50), -- 基调
                                pacing VARCHAR(30), -- 节奏（快/慢/紧张等）

    -- 生成内容
                                generated_content TEXT,
                                generation_params JSONB, -- 保存生成时的参数
                                version INTEGER DEFAULT 1,

                                status VARCHAR(20) DEFAULT 'outline', -- outline/drafted/revised/final
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 对话记录表
CREATE TABLE dialogues (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           chapter_id UUID REFERENCES story_chapters(id) ON DELETE CASCADE,
                           scene_id UUID REFERENCES scenes(id),

                           speaker_id UUID REFERENCES characters(id),
                           listener_ids UUID[], -- 听众角色

                           dialogue_text TEXT NOT NULL,
                           tone VARCHAR(50), -- 语气
                           emotion VARCHAR(50), -- 情绪

    -- 上下文
                           context_before TEXT,
                           inner_thoughts TEXT, -- 内心想法
                           body_language TEXT, -- 肢体语言

                           sequence_number INTEGER, -- 对话顺序
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. 生成历史表（用于版本控制和优化）
CREATE TABLE generation_history (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                                    generation_type VARCHAR(50), -- chapter/dialogue/scene_description
                                    target_id UUID, -- 关联的章节/对话等ID

                                    prompt_template TEXT, -- 使用的提示词模板
                                    prompt_variables JSONB, -- 填充的变量
                                    full_prompt TEXT, -- 完整提示词

                                    model_name VARCHAR(50),
                                    model_parameters JSONB, -- temperature, max_tokens等

                                    generated_result TEXT,
                                    quality_score FLOAT, -- 质量评分
                                    user_feedback TEXT,

                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 角色记忆表（用于保持一致性）
CREATE TABLE character_memories (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    character_id UUID REFERENCES characters(id) ON DELETE CASCADE,
                                    timeline_id UUID REFERENCES timelines(id),

                                    memory_type VARCHAR(50), -- 事件/知识/情感/技能
                                    memory_content TEXT,
                                    emotional_weight FLOAT, -- 情感权重

    -- 记忆检索
                                    keywords TEXT[], -- 关键词用于检索
                                    related_characters UUID[],
                                    related_locations UUID[],

                                    accessibility FLOAT DEFAULT 1.0, -- 记忆可访问性（遗忘曲线）
                                    last_accessed TIMESTAMP,
                                    access_count INTEGER DEFAULT 0,

                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_memory_keywords ON character_memories USING GIN(keywords);

-- 11. 提示词模板表
CREATE TABLE prompt_templates (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  name VARCHAR(255) NOT NULL,
                                  category VARCHAR(50), -- character/scene/dialogue/chapter

                                  template_content TEXT, -- 模板内容，使用 {{variable}} 占位符
                                  required_variables TEXT[], -- 必需的变量列表
                                  optional_variables TEXT[], -- 可选的变量列表

                                  example_usage TEXT,
                                  effectiveness_score FLOAT, -- 效果评分

                                  is_active BOOLEAN DEFAULT true,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);