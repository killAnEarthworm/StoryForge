# StoryForge 故事生成系统 - 开发计划

## 📊 项目现状分析

### ✅ 已完成功能
1. **基础架构**
   - Spring Boot 3.3.5 后端框架
   - JAVA 21
   - PostgreSQL 数据库（11个表已设计）
   - Vue 3 + Vite 前端框架
   - 项目、角色、世界观、场景、章节的基础CRUD

2. **AI集成**
   - 百度千帆V2 API集成（OpenAI兼容格式）
   - Character AI生成功能已实现
   - 提示词模板配置系统

3. **核心模块**
   - Project管理（创建、编辑、删除）
   - Character基础管理和AI生成
   - 统一的API响应格式（ApiResponse）

### ⚠️ 待完成功能
1. 角色记忆系统
2. 世界观一致性验证
3. 时间线管理和悖论检测
4. 场景AI生成
5. 故事章节生成
6. 对话生成系统
7. 质量评估系统
8. 前后端完整对接

---

## 🎯 开发计划概览（6周完成MVP）

| 阶段 | 时间 | 核心任务 | 预期成果 |
|------|------|----------|----------|
| Phase 1 | 第1周 | 完善数据层和核心服务 | 所有实体的完整CRUD |
| Phase 2 | 第2周 | AI生成功能扩展 | 世界观、场景AI生成 |
| Phase 3 | 第3周 | 记忆和一致性系统 | 角色记忆、世界观验证 |
| Phase 4 | 第4周 | 故事生成核心 | 章节、对话生成 |
| Phase 5 | 第5周 | 前端完善 | 完整的用户界面 |
| Phase 6 | 第6周 | 测试和优化 | 系统调优、Bug修复 |

---

## 📋 详细开发任务

## Phase 1: 数据层完善（第1周）

### 1.1 补充缺失的Entity和Repository

**任务清单：**
```java
// 需要创建的实体类
- CharacterMemory.java        // 角色记忆
- CharacterRelationship.java  // 角色关系
- Timeline.java               // 时间线
- Dialogue.java              // 对话
- GenerationHistory.java     // 生成历史
- PromptTemplate.java        // 提示词模板（数据库管理）
```

**技术要点 - CharacterMemory实现：**
```java
@Entity
@Table(name = "character_memories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterMemory {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private Timeline timeline;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private MemoryType memoryType; // EVENT, KNOWLEDGE, EMOTION, SKILL
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String memoryContent;
    
    @Column(name = "emotional_weight")
    private Float emotionalWeight = 0.5f;
    
    @Type(type = "string-array")
    @Column(columnDefinition = "text[]")
    private String[] keywords;
    
    @Column(columnDefinition = "uuid[]")
    private UUID[] relatedCharacters;
    
    @Column(columnDefinition = "uuid[]")
    private UUID[] relatedLocations;
    
    @Column(name = "accessibility")
    private Float accessibility = 1.0f; // 遗忘曲线
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "access_count")
    private Integer accessCount = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 遗忘曲线计算
    public void updateAccessibility() {
        if (lastAccessed != null) {
            long daysSinceAccess = ChronoUnit.DAYS.between(lastAccessed, LocalDateTime.now());
            // 艾宾浩斯遗忘曲线公式
            this.accessibility = (float) Math.exp(-daysSinceAccess / 7.0) * emotionalWeight;
        }
    }
}
```

### 1.2 创建Service层扩展

**CharacterMemoryService.java：**
```java
@Service
@Transactional
@Slf4j
public class CharacterMemoryService {
    
    @Autowired
    private CharacterMemoryRepository memoryRepository;
    
    /**
     * 智能记忆检索
     * 根据场景上下文返回最相关的记忆
     */
    public List<CharacterMemory> retrieveRelevantMemories(
            UUID characterId, 
            String sceneContext,
            String currentEmotion,
            int maxResults) {
        
        // 1. 获取所有可访问的记忆
        List<CharacterMemory> allMemories = memoryRepository
            .findByCharacterIdAndAccessibilityGreaterThan(characterId, 0.3f);
        
        // 2. 更新遗忘曲线
        allMemories.forEach(CharacterMemory::updateAccessibility);
        
        // 3. 计算相关性分数
        Map<CharacterMemory, Double> relevanceScores = new HashMap<>();
        for (CharacterMemory memory : allMemories) {
            double score = calculateRelevanceScore(memory, sceneContext, currentEmotion);
            relevanceScores.put(memory, score);
        }
        
        // 4. 按相关性排序并返回
        return relevanceScores.entrySet().stream()
            .sorted(Map.Entry.<CharacterMemory, Double>comparingByValue().reversed())
            .limit(maxResults)
            .map(Map.Entry::getKey)
            .peek(memory -> {
                // 更新访问记录
                memory.setLastAccessed(LocalDateTime.now());
                memory.setAccessCount(memory.getAccessCount() + 1);
                memoryRepository.save(memory);
            })
            .collect(Collectors.toList());
    }
    
    private double calculateRelevanceScore(
            CharacterMemory memory, 
            String sceneContext, 
            String emotion) {
        
        double score = 0.0;
        
        // 关键词匹配（30%）
        score += calculateKeywordMatch(memory.getKeywords(), sceneContext) * 0.3;
        
        // 情感共鸣（20%）
        if (emotion != null && memory.getMemoryType() == MemoryType.EMOTION) {
            score += memory.getEmotionalWeight() * 0.2;
        }
        
        // 记忆可访问性（30%）
        score += memory.getAccessibility() * 0.3;
        
        // 重要性权重（20%）
        score += memory.getEmotionalWeight() * 0.2;
        
        return score;
    }
}
```

---

## Phase 2: AI生成功能扩展（第2周）

### 2.1 世界观AI生成

**WorldviewGenerationService.java：**
```java
@Service
@Slf4j
public class WorldviewGenerationService {
    
    @Autowired
    private AiGenerationService aiService;
    
    @Autowired
    private WorldviewService worldviewService;
    
    /**
     * 生成完整的世界观设定
     */
    public WorldviewDTO generateWorldview(UUID projectId, String keywords, String genre) {
        log.info("生成世界观 - 项目: {}, 关键词: {}, 类型: {}", projectId, keywords, genre);
        
        // 构建提示词
        Map<String, Object> variables = new HashMap<>();
        variables.put("keywords", keywords);
        variables.put("genre", genre);
        variables.put("requirements", getWorldviewRequirements(genre));
        
        String prompt = buildWorldviewPrompt(variables);
        
        // 调用AI
        String response = aiService.chatWithOptions(prompt, 0.8, 3000);
        
        // 解析响应
        WorldviewDTO worldview = parseWorldviewResponse(response);
        worldview.setProjectId(projectId);
        
        // 保存并返回
        return worldviewService.createWorldview(worldview);
    }
    
    private String buildWorldviewPrompt(Map<String, Object> variables) {
        return """
            # 世界观生成任务
            
            ## 输入信息
            - 关键词: %s
            - 故事类型: %s
            - 特殊要求: %s
            
            ## 生成要求
            请创建一个完整的世界观设定，包含以下方面：
            
            ### 1. 宇宙法则
            - 物理规律（是否与现实相同）
            - 超自然力量（魔法、异能、科技等）
            - 能量体系（如何获取和使用力量）
            
            ### 2. 社会结构
            - 政治体系（国家、组织、权力结构）
            - 经济模式（货币、贸易、资源分配）
            - 文化特征（价值观、习俗、禁忌）
            - 社会阶层（等级制度、流动性）
            
            ### 3. 地理环境
            - 世界规模（星球、大陆、城市）
            - 主要地区（名称、特征、重要性）
            - 气候环境（季节、天气、自然灾害）
            - 特殊地点（圣地、禁区、遗迹）
            
            ### 4. 历史背景
            - 创世神话或起源
            - 重大历史事件（至少3个）
            - 传说和预言
            - 当前时代特征
            
            ### 5. 规则约束
            - 世界运行的基本规则（5-10条）
            - 绝对禁忌和限制
            - 平衡机制（防止力量失控）
            
            ## 输出格式
            请以JSON格式输出，确保可以直接解析。
            
            示例结构：
            {
                "name": "世界观名称",
                "summary": "一段200字的概述",
                "universeLaws": {
                    "physics": "物理规律描述",
                    "magic": "魔法系统描述",
                    "energy": "能量体系描述"
                },
                "socialStructure": {
                    "politics": "政治体系",
                    "economy": "经济模式",
                    "culture": "文化特征",
                    "hierarchy": "社会阶层"
                },
                "geography": {
                    "scale": "世界规模",
                    "regions": [{"name": "", "description": ""}],
                    "climate": "气候环境",
                    "specialLocations": []
                },
                "history": {
                    "origin": "起源故事",
                    "majorEvents": [],
                    "legends": [],
                    "currentEra": "当前时代"
                },
                "rules": ["规则1", "规则2"],
                "constraints": ["约束1", "约束2"],
                "terminology": {
                    "term1": "解释",
                    "term2": "解释"
                }
            }
            """.formatted(
            variables.get("keywords"),
            variables.get("genre"),
            variables.get("requirements")
        );
    }
}
```

### 2.2 场景AI生成

**给Claude Code的指导：场景生成要点**
```markdown
场景生成需要考虑：
1. 与世界观的一致性
2. 氛围和情绪基调
3. 感官细节（视觉、听觉、嗅觉、触觉）
4. 可交互元素
5. 场景的叙事功能
```

---

## Phase 3: 记忆和一致性系统（第3周）

### 3.1 角色一致性验证器

**CharacterConsistencyValidator.java：**
```java
@Component
@Slf4j
public class CharacterConsistencyValidator {
    
    @Autowired
    private CharacterRepository characterRepository;
    
    @Autowired
    private AiGenerationService aiService;
    
    /**
     * 验证生成内容是否符合角色设定
     */
    public ConsistencyResult validateContent(
            UUID characterId, 
            String generatedContent,
            ContentType contentType) {
        
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new ResourceNotFoundException("Character not found"));
        
        ConsistencyResult result = new ConsistencyResult();
        
        // 1. 性格向量验证
        if (character.getPersonalityVector() != null) {
            double vectorScore = validatePersonalityVector(
                character.getPersonalityVector(), 
                generatedContent
            );
            result.setVectorScore(vectorScore);
        }
        
        // 2. 语言模式验证
        if (character.getSpeechPattern() != null) {
            boolean speechValid = validateSpeechPattern(
                character.getSpeechPattern(),
                generatedContent,
                contentType
            );
            result.setSpeechPatternValid(speechValid);
        }
        
        // 3. 行为习惯验证
        List<String> violations = checkBehaviorViolations(
            character,
            generatedContent
        );
        result.setViolations(violations);
        
        // 4. AI深度验证（可选，消耗token）
        if (result.needsDeepValidation()) {
            String aiValidation = performAIValidation(character, generatedContent);
            result.setAiSuggestions(aiValidation);
        }
        
        result.setOverallScore(calculateOverallScore(result));
        return result;
    }
    
    /**
     * 使用AI进行深度验证
     */
    private String performAIValidation(Character character, String content) {
        String prompt = String.format("""
            请验证以下内容是否符合角色设定：
            
            ## 角色设定
            - 姓名：%s
            - 性格特征：%s
            - 说话方式：%s
            - 行为习惯：%s
            
            ## 待验证内容
            %s
            
            ## 验证要求
            1. 判断内容是否符合角色性格
            2. 语言风格是否一致
            3. 行为是否合理
            4. 如有问题，提供修改建议
            
            请简洁地输出验证结果和建议。
            """,
            character.getName(),
            Arrays.toString(character.getPersonalityTraits()),
            character.getSpeechPattern(),
            Arrays.toString(character.getBehavioralHabits()),
            content
        );
        
        return aiService.chatWithOptions(prompt, 0.3, 500);
    }
}
```

### 3.2 世界观一致性引擎

**给Claude Code的实现指导：**
```markdown
世界观一致性检查需要：
1. 解析生成内容中的实体（人物、地点、物品、概念）
2. 检查是否违反世界观规则
3. 验证专有名词使用
4. 检查时代背景一致性
5. 提供自动修正建议
```

---

## Phase 4: 故事生成核心（第4周）

### 4.1 章节生成系统

**StoryGenerationService.java核心逻辑：**
```java
@Service
@Slf4j
public class StoryGenerationService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private CharacterService characterService;
    
    @Autowired
    private WorldviewService worldviewService;
    
    @Autowired
    private CharacterMemoryService memoryService;
    
    @Autowired
    private AiGenerationService aiService;
    
    @Autowired
    private CharacterConsistencyValidator consistencyValidator;
    
    /**
     * 生成故事章节
     */
    public StoryChapterDTO generateChapter(ChapterGenerationRequest request) {
        log.info("开始生成章节 - 项目: {}, 章节号: {}", 
            request.getProjectId(), request.getChapterNumber());
        
        // 1. 收集上下文数据
        GenerationContext context = buildGenerationContext(request);
        
        // 2. 构建提示词
        String prompt = buildChapterPrompt(context);
        
        // 3. AI生成初稿
        String draft = aiService.chatWithOptions(prompt, 0.8, 4000);
        
        // 4. 一致性验证和修正
        String refined = refineWithConsistency(draft, context);
        
        // 5. 更新角色记忆
        updateCharacterMemories(refined, context);
        
        // 6. 保存并返回
        return saveChapter(refined, request);
    }
    
    /**
     * 构建生成上下文
     */
    private GenerationContext buildGenerationContext(ChapterGenerationRequest request) {
        GenerationContext context = new GenerationContext();
        
        // 加载项目信息
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        context.setProject(project);
        
        // 加载世界观
        if (request.getWorldviewId() != null) {
            WorldviewDTO worldview = worldviewService.getWorldviewById(request.getWorldviewId());
            context.setWorldview(worldview);
        }
        
        // 加载参与角色
        List<CharacterDTO> characters = request.getCharacterIds().stream()
            .map(id -> characterService.getCharacterById(id))
            .collect(Collectors.toList());
        context.setCharacters(characters);
        
        // 加载角色记忆
        Map<UUID, List<CharacterMemory>> memoriesMap = new HashMap<>();
        for (CharacterDTO character : characters) {
            List<CharacterMemory> memories = memoryService.retrieveRelevantMemories(
                character.getId(),
                request.getSceneDescription(),
                request.getEmotionalTone(),
                5
            );
            memoriesMap.put(character.getId(), memories);
        }
        context.setCharacterMemories(memoriesMap);
        
        // 加载前文内容（如果有）
        if (request.getChapterNumber() > 1) {
            String previousContent = loadPreviousChapters(
                request.getProjectId(), 
                request.getChapterNumber() - 1
            );
            context.setPreviousContent(previousContent);
        }
        
        return context;
    }
}
```

### 4.2 对话生成系统

**给Claude Code的实现要点：**
```markdown
对话生成注意事项：
1. 每个角色的对话必须符合其说话方式
2. 考虑角色间的关系（敌对、友好、陌生）
3. 反映角色当前的情绪状态
4. 推进剧情发展
5. 包含潜台词和内心活动
```

---

## Phase 5: 前端完善（第5周）

### 5.1 核心页面实现清单

**需要完成的页面：**
```javascript
// 1. 角色管理页面
/front/src/views/character/Index.vue
- 角色列表展示（卡片视图）
- 角色详情弹窗
- AI生成表单
- 角色关系图（可视化）

// 2. 世界观设定页面
/front/src/views/worldview/Index.vue
- 世界观编辑器（富文本）
- 规则管理
- 知识图谱可视化

// 3. 时间线管理页面
/front/src/views/timeline/Index.vue
- 时间轴组件
- 事件编辑器
- 悖论检测提示

// 4. 故事生成页面
/front/src/views/story/Index.vue
- 生成参数设置
- 实时生成展示（流式）
- 质量评估结果
- 导出功能
```

### 5.2 状态管理优化

**Vuex Store结构：**
```javascript
// /front/src/store/modules/generation.js
const generation = {
  namespaced: true,
  
  state: {
    // 当前生成任务
    currentTask: null,
    // 生成进度
    progress: 0,
    // 生成历史
    history: [],
    // 实时生成的内容（流式）
    streamContent: '',
    // 生成状态
    status: 'idle', // idle, generating, completed, error
  },
  
  mutations: {
    SET_STREAM_CONTENT(state, content) {
      state.streamContent += content;
    },
    
    CLEAR_STREAM(state) {
      state.streamContent = '';
    },
    
    SET_STATUS(state, status) {
      state.status = status;
    }
  },
  
  actions: {
    async generateStory({ commit, dispatch }, params) {
      commit('SET_STATUS', 'generating');
      commit('CLEAR_STREAM');
      
      try {
        // 使用EventSource接收流式响应
        const eventSource = new EventSource(
          `/api/story/generate/stream?${new URLSearchParams(params)}`
        );
        
        eventSource.onmessage = (event) => {
          const data = JSON.parse(event.data);
          commit('SET_STREAM_CONTENT', data.content);
        };
        
        eventSource.onerror = (error) => {
          commit('SET_STATUS', 'error');
          eventSource.close();
        };
        
        eventSource.addEventListener('done', () => {
          commit('SET_STATUS', 'completed');
          eventSource.close();
        });
        
      } catch (error) {
        commit('SET_STATUS', 'error');
        throw error;
      }
    }
  }
};
```

---

## Phase 6: 测试和优化（第6周）

### 6.1 测试清单

**单元测试：**
```java
// 需要测试的核心功能
- CharacterMemoryService.retrieveRelevantMemories()
- CharacterConsistencyValidator.validateContent()
- WorldviewConsistencyEngine.validateAgainstWorldview()
- StoryGenerationService.generateChapter()
```

**集成测试：**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class StoryGenerationIntegrationTest {
    
    @Test
    public void testCompleteStoryGeneration() {
        // 1. 创建项目
        // 2. 创建世界观
        // 3. 生成角色
        // 4. 创建场景
        // 5. 生成章节
        // 6. 验证一致性
    }
}
```

### 6.2 性能优化要点

**给Claude Code的优化建议：**
```markdown
性能优化关键点：

1. 数据库优化
   - 为character_memories.keywords创建GIN索引
   - 使用数据库连接池（HikariCP）
   - 批量操作使用batch insert/update

2. AI调用优化
   - 实现提示词缓存机制
   - 并行处理多个AI请求
   - 使用更快的模型（ERNIE-Speed-8K）进行初稿

3. 记忆检索优化
   - 实现记忆索引（使用Elasticsearch或向量数据库）
   - 定期清理低相关性记忆
   - 使用布隆过滤器快速过滤

4. 前端优化
   - 虚拟滚动处理大量数据
   - 懒加载组件
   - 使用Web Worker处理复杂计算
```

---

## 🚀 给Claude Code的开发指南

### 1. 项目结构理解
```
后端核心路径：
/src/main/java/com/linyuan/storyforge/
  - controller/  (API接口)
  - service/     (业务逻辑)
  - entity/      (数据实体)
  - repository/  (数据访问)
  - dto/         (数据传输对象)

前端核心路径：
/front/src/
  - views/       (页面组件)
  - store/       (状态管理)
  - http/        (API调用)
  - components/  (通用组件)
```

### 2. 开发约定

**后端开发约定：**
- 所有API返回`ApiResponse<T>`格式
- Service层使用`@Transactional`管理事务
- 使用`@Slf4j`记录日志
- DTO和Entity分离，使用MapStruct或手动转换

**前端开发约定：**
- 使用Composition API编写Vue组件
- 状态管理使用Vuex 4
- API调用统一通过`/src/http/api.js`
- UI组件使用Ant Design Vue

### 3. AI集成要点

**调用百度千帆API：**
```java
// 简单对话
String response = aiService.chat("你的问题");

// 使用模板
Map<String, Object> variables = new HashMap<>();
String result = aiService.generateWithTemplate("template-name", variables);

// 自定义参数
String result = aiService.chatWithOptions(prompt, temperature, maxTokens);
```

### 4. 数据库注意事项

**PostgreSQL特殊类型：**
```java
// 数组类型
@Type(type = "string-array")
@Column(columnDefinition = "text[]")
private String[] tags;

// JSON类型
@Type(type = "jsonb")
@Column(columnDefinition = "jsonb")
private Map<String, Object> metadata;

// UUID数组
@Column(columnDefinition = "uuid[]")
private UUID[] relatedIds;
```

### 5. 常见问题处理

**问题1：AI响应解析失败**
```java
// AI响应可能包含Markdown代码块
String cleanJson = response
    .replaceAll("```json\\s*", "")
    .replaceAll("```\\s*$", "")
    .trim();
```

**问题2：遗忘曲线计算**
```java
// 使用艾宾浩斯遗忘曲线
double retention = Math.exp(-daysSinceReview / retentionStrength);
```

**问题3：流式响应处理**
```java
// 后端使用Server-Sent Events
response.setContentType("text/event-stream");
response.setCharacterEncoding("UTF-8");
```

### 6. 测试数据准备

```sql
-- 创建测试项目
INSERT INTO projects (id, name, genre, theme) 
VALUES ('550e8400-e29b-41d4-a716-446655440001', '测试项目', '科幻', '人工智能');

-- 创建测试角色
INSERT INTO characters (id, project_id, name, age) 
VALUES ('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', '测试角色', 25);
```

### 7. 调试技巧

**查看AI请求日志：**
```yaml
logging:
  level:
    com.linyuan.storyforge.service: DEBUG
    org.springframework.web.client: DEBUG
```

**测试AI连接：**
```bash
curl http://localhost:8080/api/test/ai/hello
```

---

## 📌 重要提醒

1. **环境变量必须设置**：`QIANFAN_API_KEY`
2. **数据库必须初始化**：运行`init.sql`脚本
3. **前端依赖必须安装**：`cd front && npm install`
4. **Java版本要求**：JDK 21
5. **API文档**：启动后访问 http://localhost:8080/swagger-ui.html

## 🎯 第一步行动

建议从以下任务开始：

1. **创建CharacterMemory实体和Repository** - 这是记忆系统的基础
2. **实现CharacterMemoryService** - 核心的记忆管理逻辑
3. **扩展AI生成到世界观模块** - 复用现有的Character生成模式
4. **实现简单的一致性验证** - 先做基础验证，后续优化

---

**祝开发顺利！** 如有问题，请参考项目中的`CharacterService.java`作为范例。
