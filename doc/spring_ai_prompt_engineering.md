# Spring AI 故事生成系统 - 提示词工程设计方案

## 一、Spring AI 提示词架构设计

### 1. 提示词模板管理系统

```java
// 1. 提示词模板枚举
public enum PromptTemplateType {
    CHARACTER_CREATION("角色创建"),
    CHARACTER_DIALOGUE("角色对话"),
    SCENE_DESCRIPTION("场景描写"),
    CHAPTER_GENERATION("章节生成"),
    CONFLICT_RESOLUTION("冲突处理"),
    WORLDVIEW_EXPANSION("世界观扩展");
    
    private final String description;
}

// 2. 基础提示词配置
@Component
@ConfigurationProperties(prefix = "ai.prompt")
public class PromptConfiguration {
    private Map<String, String> templates;
    private Map<String, PromptSettings> settings;
    
    @Data
    public static class PromptSettings {
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
        private Double topP = 0.95;
        private Double frequencyPenalty = 0.3;
        private Double presencePenalty = 0.3;
    }
}
```

### 2. 核心提示词模板设计

#### A. 角色创建与补全模板

```yaml
# application.yml 中的提示词配置
ai:
  prompt:
    templates:
      character-creation: |
        你是一位专业的角色设计师，擅长创造立体丰满的故事角色。
        
        ## 任务
        基于用户提供的关键词，创建一个完整的角色设定。
        
        ## 输入信息
        - 关键词：{keywords}
        - 世界观背景：{worldview_context}
        - 故事题材：{genre}
        - 已存在角色（避免重复）：{existing_characters}
        
        ## 输出要求
        请生成以下完整的角色设定：
        
        ### 基础信息
        - 姓名：[符合世界观的独特姓名]
        - 年龄：[具体年龄]
        - 性别：[性别]
        - 职业：[职业或身份]
        - 外貌特征：[详细的外貌描述，包括身高、体型、发色、瞳色、服装风格等]
        
        ### 性格特征
        - 核心性格：[5-7个关键词]
        - MBTI类型：[如INTJ等]
        - 性格详述：[200字左右的性格描述]
        
        ### 深层设定
        - 背景故事：[包括童年经历、成长环境、重要事件]
        - 价值观：[核心信念和价值体系]
        - 内心恐惧：[最深层的恐惧]
        - 欲望动机：[驱动其行动的欲望]
        - 人生目标：[短期和长期目标]
        
        ### 行为特征
        - 说话方式：[语气、用词习惯、语速]
        - 口癖：[3-5个常用语句或词汇]
        - 肢体语言：[习惯性动作]
        - 行为模式：[面对不同情况的反应模式]
        
        ### 能力与弱点
        - 特殊能力：[技能或天赋]
        - 致命弱点：[性格或能力上的缺陷]
        - 成长潜力：[可能的发展方向]
        
        ## 注意事项
        1. 角色设定必须符合{genre}题材特点
        2. 性格要立体，避免完美或单一
        3. 背景故事要与世界观契合
        4. 确保与已有角色有差异化
        
      character-dialogue: |
        你是一位对话撰写专家，精通角色语言的个性化表达。
        
        ## 场景信息
        {scene_context}
        
        ## 参与角色
        {character_profiles}
        
        ## 对话背景
        - 前情提要：{previous_context}
        - 当前冲突：{current_conflict}
        - 情绪氛围：{emotional_atmosphere}
        
        ## 角色状态
        {character_states}
        
        ## 对话要求
        生成一段{turn_count}轮的对话，要求：
        
        1. **角色一致性**
           - 每个角色的语言必须符合其设定的说话方式
           - 体现各自的口癖和语言习惯
           - 反映当前的情绪状态
        
        2. **关系体现**
           - 展现角色间的关系张力
           - 体现权力动态或亲密程度
           - 适当的称呼和敬语使用
        
        3. **信息推进**
           - 每轮对话都要推进情节或透露信息
           - 避免无意义的寒暄
           - 设置适当的悬念或转折
        
        4. **非语言描写**
           - 包含必要的动作描写：[动作]
           - 情绪和表情变化：（内心想法）
           - 环境互动：*环境声音或变化*
        
        ## 输出格式
        角色名："对话内容" [动作描写]
        （内心独白或想法）
        
        ## 特殊要求
        {special_requirements}
```

#### B. 场景生成模板

```java
@Component
public class ScenePromptTemplate {
    
    private static final String SCENE_GENERATION_TEMPLATE = """
        # 场景描写任务
        
        ## 世界观背景
        %s
        
        ## 场景基础设定
        - 地点类型：%s
        - 具体位置：%s
        - 时间：%s
        - 季节/天气：%s
        
        ## 感官层次要求
        请按以下层次描写场景：
        
        1. **视觉层**（第一印象）
           - 整体布局和空间感
           - 光影效果
           - 色彩基调
           - 显著的视觉元素
        
        2. **听觉层**（环境音）
           - 背景声音
           - 特征声响
           - 声音的远近层次
        
        3. **其他感官**（沉浸感）
           - 气味（如果有特征性的）
           - 温度和湿度
           - 空气流动
        
        4. **情绪渲染**
           - 整体氛围：%s
           - 心理感受
           - 潜在的不安或期待
        
        ## 功能性要求
        
        ### 可交互元素
        %s
        
        ### 伏笔设置
        在描写中自然地植入以下元素，为后续情节服务：
        %s
        
        ## 文风要求
        - 风格基调：%s
        - 描写节奏：%s
        - 字数要求：%d字左右
        
        ## 禁止事项
        - 不要使用"似乎"、"好像"、"仿佛"等模糊词汇超过2次
        - 避免堆砌形容词
        - 不要脱离世界观设定
        """;
    
    public String buildScenePrompt(SceneGenerationRequest request) {
        return String.format(SCENE_GENERATION_TEMPLATE,
            request.getWorldviewContext(),
            request.getLocationType(),
            request.getSpecificLocation(),
            request.getTimeString(),
            request.getWeatherSeason(),
            request.getAtmosphere(),
            formatInteractiveElements(request.getInteractiveElements()),
            formatForeshadowing(request.getForeshadowing()),
            request.getStyleTone(),
            request.getPacing(),
            request.getWordCount()
        );
    }
}
```

#### C. 章节生成的链式提示词

```java
@Service
public class ChapterGenerationPromptChain {
    
    // 第一步：生成章节大纲
    private static final String OUTLINE_TEMPLATE = """
        # 章节大纲生成
        
        ## 故事背景
        - 前情提要：{previous_summary}
        - 本章位置：第{chapter_number}章（共{total_chapters}章）
        - 故事主题：{theme}
        
        ## 本章核心任务
        - 主要冲突：{main_conflict}
        - 需要解决：{problems_to_solve}
        - 角色成长：{character_development}
        
        ## 参与要素
        - 主要角色：{main_characters}
        - 场景列表：{available_scenes}
        
        ## 生成要求
        创建一个包含5-7个场景的章节大纲，每个场景包括：
        1. 场景编号和标题
        2. 参与角色
        3. 核心事件
        4. 情绪转折点
        5. 与下一场景的连接
        
        确保：
        - 节奏张弛有度
        - 有明确的开端-发展-高潮-结尾
        - 每个场景都推进主线剧情
        """;
    
    // 第二步：场景扩写
    private static final String SCENE_EXPANSION_TEMPLATE = """
        # 场景扩写
        
        ## 场景定位
        章节：第{chapter_number}章
        场景：{scene_number} - {scene_title}
        
        ## 场景大纲
        {scene_outline}
        
        ## 角色状态
        {character_current_states}
        
        ## 上文衔接
        {previous_scene_ending}
        
        ## 扩写要求
        将场景大纲扩展为{word_count}字的完整场景：
        
        1. 开场（20%）
           - 场景转换的自然过渡
           - 环境氛围的建立
        
        2. 发展（50%）
           - 角色互动的详细描写
           - 冲突的逐步展开
           - 对话与动作的平衡
        
        3. 转折（20%）
           - 情绪或局势的转变
           - 新信息的揭露
        
        4. 收尾（10%）
           - 悬念设置或情绪留白
           - 与下一场景的钩子
        
        ## 写作技巧提醒
        - 展示而非告知（Show, Don't Tell）
        - 保持视角一致性
        - 适当的留白和想象空间
        """;
    
    // 第三步：对话优化
    private static final String DIALOGUE_REFINEMENT_TEMPLATE = """
        # 对话优化任务
        
        ## 原始场景文本
        {scene_text}
        
        ## 角色语言特征
        {character_speech_patterns}
        
        ## 优化要求
        请优化场景中的对话部分：
        
        1. **个性化语言**
           - 为每个角色注入独特的说话方式
           - 加入合适的口癖和语言习惯
           - 体现教育背景和社会地位
        
        2. **潜台词处理**
           - 识别可以使用潜台词的地方
           - 将直白的表达转为含蓄暗示
           - 增加对话的层次感
        
        3. **节奏控制**
           - 调整对话长度的变化
           - 加入适当的停顿和沉默
           - 平衡对话与动作描写
        
        4. **情绪递进**
           - 确保情绪变化的合理性
           - 通过语气词体现情绪
           - 冲突升级的语言体现
        
        输出优化后的完整场景文本。
        """;
}
```

### 3. Spring AI 集成实现

```java
@Service
@Slf4j
public class AIStoryGenerationService {
    
    private final ChatClient chatClient;
    private final PromptTemplateRepository templateRepository;
    private final CharacterMemoryService memoryService;
    
    // 1. 动态上下文构建器
    @Component
    public class DynamicContextBuilder {
        
        public Map<String, Object> buildCharacterContext(UUID characterId, UUID sceneId) {
            Map<String, Object> context = new HashMap<>();
            
            // 基础信息
            Character character = characterRepository.findById(characterId);
            context.put("name", character.getName());
            context.put("age", character.getAge());
            context.put("personality_traits", character.getPersonalityTraits());
            context.put("speech_pattern", character.getSpeechPattern());
            
            // 动态记忆检索
            List<CharacterMemory> relevantMemories = memoryService
                .retrieveRelevantMemories(characterId, sceneId, 5);
            context.put("recent_memories", formatMemories(relevantMemories));
            
            // 当前状态
            CharacterState currentState = getCharacterCurrentState(characterId);
            context.put("emotional_state", currentState.getEmotionalState());
            context.put("current_goals", currentState.getCurrentGoals());
            
            // 关系网络
            List<CharacterRelationship> relationships = 
                relationshipRepository.findActiveRelationships(characterId);
            context.put("relationships", formatRelationships(relationships));
            
            return context;
        }
        
        public String buildCharacterPromptContext(UUID characterId) {
            // 构建角色的完整提示词上下文
            StringBuilder context = new StringBuilder();
            
            Character character = characterRepository.findById(characterId);
            
            // 分层构建上下文
            context.append("【角色定位】\n");
            context.append(String.format("姓名：%s，%d岁，%s\n", 
                character.getName(), 
                character.getAge(), 
                character.getOccupation()));
            
            context.append("\n【性格特征】\n");
            context.append("核心特质：")
                   .append(String.join("、", character.getPersonalityTraits()))
                   .append("\n");
            
            context.append("\n【说话方式】\n");
            context.append(character.getSpeechPattern()).append("\n");
            context.append("口癖：")
                   .append(String.join("、", character.getCatchphrases()))
                   .append("\n");
            
            context.append("\n【当前状态】\n");
            CharacterState state = getCharacterCurrentState(characterId);
            context.append("情绪：").append(state.getEmotionalState()).append("\n");
            context.append("目标：").append(state.getCurrentGoals()).append("\n");
            
            // 只添加最相关的3-5条记忆
            context.append("\n【重要记忆】\n");
            List<CharacterMemory> memories = memoryService
                .retrieveRelevantMemories(characterId, null, 3);
            memories.forEach(memory -> {
                context.append("- ").append(memory.getMemoryContent()).append("\n");
            });
            
            return context.toString();
        }
    }
    
    // 2. 提示词优化器
    @Component
    public class PromptOptimizer {
        
        private static final int MAX_CONTEXT_TOKENS = 3000;
        
        public String optimizePrompt(String basePrompt, Map<String, Object> variables) {
            // Token 计数和裁剪
            String optimizedPrompt = basePrompt;
            
            // 1. 变量替换
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = formatValue(entry.getValue());
                optimizedPrompt = optimizedPrompt.replace(key, value);
            }
            
            // 2. 上下文长度控制
            if (estimateTokens(optimizedPrompt) > MAX_CONTEXT_TOKENS) {
                optimizedPrompt = truncateContext(optimizedPrompt);
            }
            
            // 3. 添加思维链引导
            optimizedPrompt = addChainOfThought(optimizedPrompt);
            
            return optimizedPrompt;
        }
        
        private String addChainOfThought(String prompt) {
            return prompt + "\n\n请按以下步骤思考：\n" +
                   "1. 首先理解角色的核心性格和当前状态\n" +
                   "2. 考虑场景氛围和其他角色的影响\n" +
                   "3. 基于记忆和经历做出符合逻辑的反应\n" +
                   "4. 生成符合角色特征的内容";
        }
    }
    
    // 3. 生成管道
    @Service
    public class GenerationPipeline {
        
        @Transactional
        public ChapterContent generateChapter(UUID chapterId) {
            StoryChapter chapter = chapterRepository.findById(chapterId);
            
            // 阶段1：准备上下文
            GenerationContext context = prepareContext(chapter);
            
            // 阶段2：生成大纲
            String outline = generateOutline(context);
            
            // 阶段3：场景生成链
            List<SceneContent> scenes = new ArrayList<>();
            for (SceneOutline sceneOutline : parseOutline(outline)) {
                SceneContent scene = generateScene(sceneOutline, context);
                scenes.add(scene);
                
                // 更新上下文（累积变化）
                context.updateAfterScene(scene);
            }
            
            // 阶段4：对话优化
            scenes = optimizeDialogues(scenes, context);
            
            // 阶段5：章节整合
            ChapterContent chapterContent = assembleChapter(scenes, context);
            
            // 阶段6：一致性检查
            validateConsistency(chapterContent, context);
            
            // 阶段7：保存和更新状态
            saveChapterContent(chapterContent);
            updateCharacterStates(chapterContent);
            
            return chapterContent;
        }
        
        private SceneContent generateScene(SceneOutline outline, GenerationContext context) {
            // 构建场景提示词
            PromptTemplate promptTemplate = new PromptTemplate(
                sceneGenerationTemplate,
                Map.of(
                    "scene_outline", outline,
                    "characters", context.getSceneCharacters(outline),
                    "worldview", context.getWorldviewSummary(),
                    "previous_context", context.getPreviousSceneEnding()
                )
            );
            
            // 调用AI生成
            ChatResponse response = chatClient.call(
                promptTemplate.create(),
                ChatOptionsBuilder.builder()
                    .withTemperature(0.8)
                    .withMaxTokens(1500)
                    .build()
            );
            
            return parseSceneContent(response.getResult().getOutput().getContent());
        }
    }
}
```

### 4. 高级提示词策略

#### A. 角色一致性保障策略

```java
@Component
public class CharacterConsistencyStrategy {
    
    // 性格锚点提示词
    private static final String PERSONALITY_ANCHOR_TEMPLATE = """
        【角色一致性锚点】
        
        核心性格锚点（绝不能违背）：
        1. %s
        2. %s
        3. %s
        
        行为边界：
        - 绝对不会做：%s
        - 一定会做：%s
        
        语言特征锚点：
        - 必须包含的语言特征：%s
        - 禁止使用的表达：%s
        
        情绪反应模式：
        - 面对冲突：%s
        - 面对压力：%s
        - 面对亲密：%s
        """;
    
    public String generatePersonalityAnchor(Character character) {
        // 提取角色的核心特征作为锚点
        List<String> coreTraits = extractCoreTraits(character);
        
        return String.format(PERSONALITY_ANCHOR_TEMPLATE,
            coreTraits.get(0),
            coreTraits.get(1), 
            coreTraits.get(2),
            character.getNeverBehaviors(),
            character.getAlwaysBehaviors(),
            character.getMustHaveLanguageFeatures(),
            character.getForbiddenExpressions(),
            character.getConflictReaction(),
            character.getPressureReaction(),
            character.getIntimacyReaction()
        );
    }
}
```

#### B. 记忆权重计算策略

```java
@Service
public class MemoryRelevanceCalculator {
    
    public List<CharacterMemory> calculateRelevantMemories(
            UUID characterId, 
            UUID sceneId, 
            String currentContext) {
        
        // SQL查询，计算综合相关性分数
        String sql = """
            WITH memory_scores AS (
                SELECT 
                    m.*,
                    -- 时间衰减因子（越近的记忆权重越高）
                    EXP(-EXTRACT(EPOCH FROM (NOW() - m.created_at)) / (86400 * 30)) AS time_decay,
                    
                    -- 情感权重（情感越强烈的记忆越重要）
                    m.emotional_weight AS emotion_score,
                    
                    -- 场景相关性（相同场景的记忆更相关）
                    CASE WHEN :sceneId = ANY(m.related_locations) 
                         THEN 1.0 ELSE 0.3 END AS scene_relevance,
                    
                    -- 关键词匹配度（使用全文搜索）
                    TS_RANK(to_tsvector('simple', m.memory_content), 
                           plainto_tsquery('simple', :keywords)) AS keyword_relevance,
                    
                    -- 访问频率因子
                    LOG(m.access_count + 1) / 10.0 AS frequency_factor
                    
                FROM character_memories m
                WHERE m.character_id = :characterId
            )
            SELECT 
                *,
                (0.3 * time_decay + 
                 0.25 * emotion_score + 
                 0.2 * scene_relevance + 
                 0.15 * keyword_relevance + 
                 0.1 * frequency_factor) AS total_relevance
            FROM memory_scores
            ORDER BY total_relevance DESC
            LIMIT :limit
            """;
        
        return jdbcTemplate.query(sql, 
            Map.of(
                "characterId", characterId,
                "sceneId", sceneId,
                "keywords", extractKeywords(currentContext),
                "limit", 5
            ),
            new CharacterMemoryRowMapper()
        );
    }
}
```

#### C. 多角色协同提示词

```java
public class MultiCharacterPromptBuilder {
    
    private static final String MULTI_CHARACTER_TEMPLATE = """
        # 多角色互动场景
        
        ## 角色阵容
        %s
        
        ## 关系网络
        %s
        
        ## 互动规则
        
        1. **权力动态**
           %s
        
        2. **对话顺序逻辑**
           - 主导者先发言
           - 被动者响应
           - 观察者最后评论
        
        3. **冲突升级路径**
           起始张力：%d/10
           目标张力：%d/10
           升级节奏：%s
        
        4. **信息揭露控制**
           每个角色知道的信息：
           %s
           
           本场景可以揭露的信息：
           %s
        
        ## 生成要求
        创建一个%d轮的多角色互动，确保：
        - 每个角色至少发言%d次
        - 体现各自不同的立场
        - 通过对话推进剧情
        - 在第%d轮达到冲突高潮
        """;
    
    public String buildMultiCharacterPrompt(SceneContext scene) {
        return String.format(MULTI_CHARACTER_TEMPLATE,
            formatCharacterRoster(scene.getCharacters()),
            formatRelationshipNetwork(scene.getRelationships()),
            formatPowerDynamics(scene.getCharacters()),
            scene.getStartingTension(),
            scene.getTargetTension(),
            scene.getEscalationPace(),
            formatCharacterKnowledge(scene.getCharacterKnowledge()),
            formatRevealableInfo(scene.getRevealableInfo()),
            scene.getTotalRounds(),
            scene.getMinSpeakingTimes(),
            scene.getClimaxRound()
        );
    }
}
```

### 5. 提示词版本管理和优化

```java
@Service
public class PromptVersioningService {
    
    @Autowired
    private PromptTemplateRepository repository;
    
    // A/B测试不同版本的提示词
    public PromptTemplate selectOptimalTemplate(String category) {
        List<PromptTemplate> activeTemplates = repository
            .findByCategoryAndIsActive(category, true);
        
        if (activeTemplates.size() == 1) {
            return activeTemplates.get(0);
        }
        
        // 基于效果评分的加权随机选择
        return weightedRandomSelection(activeTemplates);
    }
    
    // 基于反馈自动优化提示词
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void optimizePrompts() {
        List<GenerationHistory> recentHistory = historyRepository
            .findRecent(7); // 最近7天
        
        Map<UUID, List<GenerationHistory>> byTemplate = recentHistory.stream()
            .collect(Collectors.groupingBy(GenerationHistory::getTemplateId));
        
        byTemplate.forEach((templateId, histories) -> {
            double avgScore = histories.stream()
                .mapToDouble(GenerationHistory::getQualityScore)
                .average()
                .orElse(0.0);
            
            PromptTemplate template = repository.findById(templateId);
            template.setEffectivenessScore(avgScore);
            
            // 如果评分过低，生成改进建议
            if (avgScore < 0.6) {
                String improvements = generateImprovements(histories);
                template.setNeedsRevision(true);
                template.setImprovementSuggestions(improvements);
            }
            
            repository.save(template);
        });
    }
}
```

## 二、实施建议

### 1. 提示词管理最佳实践

1. **版本控制**：所有提示词模板都应该有版本号，便于回滚和对比
2. **A/B测试**：同时运行多个版本，基于效果自动选择最优
3. **缓存策略**：频繁使用的提示词组合应该缓存
4. **监控指标**：跟踪生成质量、一致性得分、用户满意度

### 2. 性能优化建议

```java
@Configuration
public class PromptCacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("prompts", "contexts");
    }
    
    @Cacheable(value = "prompts", key = "#templateId + '_' + #variables.hashCode()")
    public String getCachedPrompt(String templateId, Map<String, Object> variables) {
        // 缓存组装好的提示词
    }
}
```

### 3. 错误处理和降级

```java
@Component
public class PromptFallbackHandler {
    
    @CircuitBreaker(name = "ai-generation", fallbackMethod = "fallbackGeneration")
    public String generateContent(String prompt) {
        // 主要生成逻辑
    }
    
    public String fallbackGeneration(String prompt, Exception ex) {
        log.error("Generation failed, using fallback", ex);
        // 使用简化的提示词或返回模板内容
        return useSimplifiedPrompt(prompt);
    }
}
```

### 4. 质量保证体系

1. **自动评分**：使用另一个AI模型评估生成质量
2. **一致性检查**：向量相似度 + 规则检查
3. **人工审核**：标记低分内容供人工审核
4. **持续学习**：收集优秀案例作为few-shot示例

这套提示词工程方案充分利用了Spring AI的特性，确保生成内容的质量和一致性！