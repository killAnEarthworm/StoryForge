# Week 2 剩余开发计划（Phase 2 & Phase 3 前置准备）

## 📅 时间范围
第二周剩余时间（预计3-4个工作日）

## 📊 当前状态

### ✅ 已完成
- **第一周**: 角色记忆系统 + 角色一致性验证系统
- **第二周 Phase 1**: 世界观AI生成系统（12种类型、批量生成、一致性验证）

### 🎯 本周剩余目标
完成 Phase 2 的场景AI生成系统，并为第三周的时间线系统做好准备

---

## 🚀 Phase 2-2: 场景AI生成系统（优先级：最高）

### 目标
实现智能场景生成，支持多种场景类型，与世界观和角色系统深度集成

### 核心功能清单

#### 1. 场景类型枚举（SceneType.java）
**位置**: `src/main/java/com/linyuan/storyforge/enums/SceneType.java`

**需要定义的场景类型**:
```java
public enum SceneType {
    ACTION("动作", "战斗、追逐、冒险等动作场景"),
    DIALOGUE("对话", "角色间的对话交流场景"),
    DESCRIPTION("描写", "环境、景物、氛围描写"),
    EMOTIONAL("情感", "情感表达、心理活动场景"),
    CONFLICT("冲突", "矛盾、对抗、紧张场景"),
    TRANSITION("过渡", "场景转换、时空过渡"),
    CLIMAX("高潮", "故事高潮、关键转折"),
    OPENING("开场", "章节或故事的开场"),
    ENDING("结尾", "章节或故事的结尾"),
    DAILY("日常", "日常生活、平淡场景");

    // 每种类型应包含：
    // - 生成要求（如动作场景需要"节奏紧凑、动作描写细致"）
    // - 氛围关键词
    // - 建议的感官侧重点
}
```

**预计工作量**: 150行代码，0.5天

---

#### 2. 场景生成请求DTO（SceneGenerationRequest.java）
**位置**: `src/main/java/com/linyuan/storyforge/dto/SceneGenerationRequest.java`

**核心字段设计**:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneGenerationRequest {
    // 基础信息
    private UUID projectId;              // 所属项目
    private UUID worldviewId;            // 所属世界观（可选）
    private UUID chapterId;              // 所属章节（可选）

    // 场景类型和定位
    private SceneType sceneType;         // 场景类型（必填）
    private String scenePurpose;         // 场景目的（如"展示角色成长"）

    // 场景设定
    private String location;             // 地点（"城堡大厅"、"森林深处"）
    private String timeOfDay;            // 时间（"黄昏"、"午夜"）
    private String weather;              // 天气（"暴雨"、"晴朗"）

    // 氛围和情绪
    private String mood;                 // 情绪基调（"紧张"、"温馨"、"诡异"）
    private String atmosphere;           // 氛围描述（"压抑的"、"轻松的"）

    // 参与角色
    private List<UUID> characterIds;     // 参与角色ID列表
    private String characterRelations;   // 角色关系描述

    // 情节要素
    private String plotContext;          // 情节上下文（"主角刚发现真相"）
    private List<String> keyEvents;      // 关键事件列表
    private String conflict;             // 冲突点（可选）

    // 感官细节要求
    private Boolean includeVisualDetails;   // 视觉细节（默认true）
    private Boolean includeAuditoryDetails; // 听觉细节（默认true）
    private Boolean includeOlfactory;       // 嗅觉细节（默认false）
    private Boolean includeTactile;         // 触觉细节（默认false）

    // 长度和风格
    private Integer targetWordCount;     // 目标字数（默认500-800）
    private String writingStyle;         // 写作风格（"细腻"、"简洁"、"诗意"）

    // AI参数
    private Double creativity;           // 创意度（0.0-1.0，默认0.75）

    // 约束条件
    private List<String> mustInclude;    // 必须包含的元素
    private List<String> mustAvoid;      // 必须避免的元素
}
```

**预计工作量**: 120行代码，0.5天

---

#### 3. 场景生成服务（SceneGenerationService.java）
**位置**: `src/main/java/com/linyuan/storyforge/service/SceneGenerationService.java`

**核心方法设计**:

##### 3.1 生成单个场景
```java
@Transactional
public SceneDTO generateScene(SceneGenerationRequest request)
```

**流程**:
1. 验证项目存在
2. 加载世界观信息（如果提供）
3. 加载角色信息和记忆
4. 构建场景生成提示词
5. 调用AI生成
6. 解析响应并提取感官细节
7. 验证与世界观一致性
8. 保存到数据库
9. 返回SceneDTO

##### 3.2 构建提示词
```java
private String buildScenePrompt(SceneGenerationRequest request,
                                 WorldviewDTO worldview,
                                 List<CharacterDTO> characters,
                                 Map<UUID, List<CharacterMemory>> memories)
```

**提示词结构**:
```
# 场景生成任务

## 角色定位
你是一位专业的场景描写大师，擅长通过细腻的感官描写营造氛围。

## 世界观背景
{如果有世界观，插入世界观的核心规则、地理、文化特征}

## 场景基本信息
- 场景类型: {sceneType}
- 地点: {location}
- 时间: {timeOfDay}
- 天气: {weather}
- 情绪基调: {mood}
- 氛围: {atmosphere}

## 参与角色
{遍历每个角色}
- {角色名}: {性格特征}、{当前状态}
  相关记忆: {最相关的2-3条记忆}

## 情节上下文
{plotContext}

关键事件:
{keyEvents列表}

## 场景目的
{scenePurpose}

## 生成要求

### 1. 感官细节
- 视觉: {如果includeVisualDetails=true} 详细描写光影、色彩、动作
- 听觉: {如果includeAuditoryDetails=true} 描写声音、节奏
- 嗅觉: {如果includeOlfactory=true} 描写气味
- 触觉: {如果includeTactile=true} 描写质感、温度

### 2. 角色表现
- 每个角色的行为必须符合其性格设定和记忆
- 对话要符合说话方式
- 反映角色间的关系

### 3. 氛围营造
- 通过环境细节营造{mood}的情绪
- 使用{writingStyle}的写作风格

### 4. 情节推进
- 包含以下关键事件: {keyEvents}
- {如果有冲突} 展现冲突: {conflict}

### 5. 约束条件
必须包含: {mustInclude}
必须避免: {mustAvoid}
{如果有worldview} 遵守世界观规则: {worldview.rules}

## 输出格式
请以JSON格式输出：

{
  "sceneTitle": "场景标题",
  "content": "场景正文内容（{targetWordCount}字左右）",
  "sensoryDetails": {
    "visual": ["视觉细节1", "视觉细节2"],
    "auditory": ["听觉细节1"],
    "olfactory": ["嗅觉细节"],
    "tactile": ["触觉细节"]
  },
  "emotionalTone": "情感基调总结",
  "keyMoments": ["关键时刻1", "关键时刻2"],
  "characterStates": {
    "角色名": "该角色在场景结束时的状态"
  }
}

## 质量要求
1. 细节真实可感
2. 氛围连贯统一
3. 符合角色设定
4. 推进情节发展
5. 避免陈词滥调
```

##### 3.3 解析场景响应
```java
private SceneDTO parseSceneResponse(String aiResponse, SceneGenerationRequest request)
```

**解析内容**:
- 提取场景标题和正文
- 提取感官细节（存入JSONB字段）
- 提取角色状态（用于更新角色记忆）
- 提取关键时刻（用于时间线）

##### 3.4 批量生成场景方案
```java
@Transactional
public List<SceneDTO> generateMultipleScenes(SceneGenerationRequest request, int count)
```

**策略**: 每次生成时调整氛围和风格参数，生成不同版本供用户选择

##### 3.5 场景扩展生成
```java
public SceneDTO expandScene(UUID sceneId, String expansionPoint, int additionalWords)
```

**功能**: 对已有场景的某个部分进行扩展细化

**预计工作量**: 450行代码，1.5天

---

#### 4. 场景一致性验证（SceneConsistencyValidator.java）
**位置**: `src/main/java/com/linyuan/storyforge/validator/SceneConsistencyValidator.java`

**验证维度**:
```java
public SceneValidationResult validateScene(UUID sceneId) {
    // 1. 世界观一致性（如果有关联世界观）
    //    - 检查是否违反世界观规则
    //    - 检查专有名词使用

    // 2. 角色一致性（如果有参与角色）
    //    - 检查角色行为是否符合性格
    //    - 检查对话是否符合说话方式

    // 3. 感官细节完整性
    //    - 检查是否包含足够的感官描写
    //    - 检查感官细节是否冲突（如"漆黑的夜晚"又"阳光明媚"）

    // 4. 情绪一致性
    //    - 检查氛围是否统一
    //    - 检查是否有突兀的情绪转变

    // 5. 情节逻辑性
    //    - 检查关键事件是否都有体现
    //    - 检查事件顺序是否合理

    return SceneValidationResult.builder()
        .worldviewConsistency(score1)
        .characterConsistency(score2)
        .sensoryCompleteness(score3)
        .emotionalCoherence(score4)
        .plotLogic(score5)
        .overallScore(average)
        .build();
}
```

**预计工作量**: 280行代码，1天

---

#### 5. 场景生成控制器（SceneGenerationController.java）
**位置**: `src/main/java/com/linyuan/storyforge/controller/SceneGenerationController.java`

**API端点设计**:

| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/scenes/generate` | POST | 生成单个场景 |
| `/api/scenes/generate/batch?count=3` | POST | 批量生成场景方案 |
| `/api/scenes/{sceneId}/expand` | POST | 扩展场景细节 |
| `/api/scenes/{sceneId}/validate` | POST | 验证场景一致性 |
| `/api/scenes/types` | GET | 获取所有场景类型 |
| `/api/scenes/types/{typeCode}/requirements` | GET | 获取类型生成要求 |
| `/api/scenes/{sceneId}/extract-events` | GET | 提取场景中的关键事件（用于时间线） |

**预计工作量**: 200行代码，0.5天

---

### 场景生成系统总结

**总代码量**: 约1200行
**总工作量**: 4天
**优先级**: 最高

**关键技术点**:
- 多感官细节提取和存储（JSONB）
- 与世界观、角色系统的深度集成
- 记忆驱动的场景生成
- 多维度一致性验证

---

## 🔄 Phase 3 前置准备: 时间线系统基础（优先级：中）

### 背景
第三周的重点是记忆和一致性系统，但时间线（Timeline）是角色记忆的重要载体。建议在第二周末完成时间线的基础实体和CRUD，为第三周做准备。

### 任务清单

#### 1. 时间线事件枚举（TimelineEventType.java）
**位置**: `src/main/java/com/linyuan/storyforge/enums/TimelineEventType.java`

```java
public enum TimelineEventType {
    BIRTH("出生", "角色诞生"),
    CHILDHOOD("童年", "童年经历"),
    MAJOR_DECISION("重大决策", "改变人生的决定"),
    RELATIONSHIP_CHANGE("关系变化", "与其他角色关系的变化"),
    SKILL_ACQUIRED("技能习得", "学会新技能"),
    TRAUMA("创伤", "创伤性事件"),
    ACHIEVEMENT("成就", "重要成就"),
    LOSS("失去", "失去重要的人或物"),
    DISCOVERY("发现", "发现重要信息或真相"),
    BATTLE("战斗", "重要战斗"),
    JOURNEY("旅程", "重要旅程开始/结束"),
    TRANSFORMATION("转变", "性格或能力的重大转变"),
    CUSTOM("自定义", "用户自定义事件");
}
```

**预计工作量**: 100行，0.25天

---

#### 2. 完善Timeline和TimelineEvent实体
**位置**: 检查并完善现有实体

**Timeline实体需要的字段**:
```java
@Entity
@Table(name = "timelines")
public class Timeline extends BaseEntity {
    private UUID projectId;           // 所属项目
    private String name;               // 时间线名称（如"主线剧情"）
    private String description;        // 描述
    private LocalDateTime startTime;   // 起始时间点
    private LocalDateTime endTime;     // 结束时间点（可选）
    private Boolean isMainTimeline;    // 是否为主时间线
    private String timeScale;          // 时间尺度（如"年"、"月"、"日"）
}
```

**TimelineEvent实体需要的字段**:
```java
@Entity
@Table(name = "timeline_events")
public class TimelineEvent extends BaseEntity {
    private UUID timelineId;           // 所属时间线
    private TimelineEventType eventType;  // 事件类型
    private String eventName;          // 事件名称
    private String eventDescription;   // 事件描述
    private LocalDateTime eventTime;   // 事件发生时间
    private Integer sequenceOrder;     // 序号（同一时间的事件排序）

    // 关联实体
    @Column(columnDefinition = "uuid[]")
    private UUID[] involvedCharacterIds;  // 涉及的角色
    @Column(columnDefinition = "uuid[]")
    private UUID[] causedByEventIds;      // 因果关系：由哪些事件导致
    @Column(columnDefinition = "uuid[]")
    private UUID[] leadToEventIds;        // 因果关系：导致了哪些事件

    // 影响
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> characterImpacts;  // 对角色的影响

    private String importance;         // 重要性（CRITICAL/MAJOR/MINOR）
    private Boolean isTurningPoint;    // 是否为转折点
}
```

**预计工作量**: 检查并完善，0.5天

---

#### 3. 创建TimelineService和TimelineController
**位置**:
- `src/main/java/com/linyuan/storyforge/service/TimelineService.java`
- `src/main/java/com/linyuan/storyforge/controller/TimelineController.java`

**基础CRUD功能**:
- 创建/更新/删除时间线
- 添加/更新/删除时间线事件
- 按时间顺序获取事件列表
- 获取角色相关的所有事件
- 获取事件的因果链

**API端点**:
```
POST   /api/timelines                           # 创建时间线
GET    /api/timelines/{id}                      # 获取时间线
GET    /api/timelines/project/{projectId}      # 获取项目的所有时间线
POST   /api/timelines/{id}/events              # 添加事件
GET    /api/timelines/{id}/events              # 获取时间线的所有事件
GET    /api/timelines/events/character/{characterId}  # 获取角色相关事件
```

**预计工作量**: 300行（Service 150 + Controller 150），1天

---

#### 4. 场景与时间线集成
**功能**: 场景生成时，自动提取关键事件并添加到时间线

**在SceneGenerationService中添加**:
```java
private void extractAndSaveTimelineEvents(SceneDTO scene,
                                           List<String> keyMoments,
                                           UUID timelineId) {
    // 将场景中的关键时刻转换为时间线事件
    // 关联到相关角色
    // 设置因果关系
}
```

**预计工作量**: 100行，0.5天

---

### 时间线系统基础总结

**总代码量**: 约500行
**总工作量**: 2天
**优先级**: 中（可选，但强烈建议完成）

---

## 🎨 可选增强功能（优先级：低）

### 1. 场景组合生成
**功能**: 根据章节大纲，一次性生成多个连续场景

**API端点**: `POST /api/scenes/generate/sequence`

**预计工作量**: 200行，0.5天

---

### 2. 场景模板系统
**功能**: 预定义常见场景模板（如"初次相遇"、"最终对决"），加速生成

**预计工作量**: 150行 + 10个模板，0.5天

---

### 3. 场景可视化数据
**功能**: 为前端提供场景的可视化数据（如情绪曲线、节奏图）

**预计工作量**: 100行，0.25天

---

## 📅 推荐开发顺序

### Day 1-2: 场景生成核心
- ✅ SceneType枚举
- ✅ SceneGenerationRequest DTO
- ✅ SceneGenerationService（核心方法）
- ✅ 提示词构建和响应解析
- 🧪 测试基础生成功能

### Day 3: 场景验证和API
- ✅ SceneConsistencyValidator
- ✅ SceneGenerationController
- ✅ API测试
- 📝 API文档

### Day 4: 时间线基础
- ✅ TimelineEventType枚举
- ✅ 完善Timeline和TimelineEvent实体
- ✅ TimelineService基础CRUD
- ✅ TimelineController基础API
- ✅ 场景与时间线集成

### Day 5（缓冲/可选）:
- 🎨 可选增强功能
- 🧪 集成测试
- 📚 完善文档
- 🔧 Bug修复

---

## 📝 文档规范

完成开发后，需要创建以下文档：

### 1. week2_phase2_scene_generation.md
**内容**:
- 场景生成系统架构
- API文档和使用示例
- 提示词设计说明
- 验证机制说明
- 测试建议

### 2. week2_implementation_summary.md
**内容**:
- 第二周完成的所有功能总结
- 代码统计
- API清单
- 下一步计划（指向第三周）

---

## 🧪 测试清单

### 单元测试
- [ ] SceneGenerationService.buildScenePrompt()
- [ ] SceneGenerationService.parseSceneResponse()
- [ ] SceneConsistencyValidator.validateScene()
- [ ] TimelineService.createEvent()

### 集成测试
- [ ] 完整场景生成流程（请求 → AI → 解析 → 保存）
- [ ] 场景与世界观一致性验证
- [ ] 场景与角色记忆集成
- [ ] 场景事件提取到时间线

### API测试
- [ ] 所有端点的正常响应
- [ ] 错误处理（缺少必填字段、无效UUID等）
- [ ] 批量生成测试

---

## 📊 预期成果

### 代码产出
- **场景生成系统**: 约1200行
- **时间线基础**: 约500行
- **总计**: 约1700行高质量代码

### 功能产出
- ✅ 10种场景类型支持
- ✅ 智能多感官场景生成
- ✅ 场景一致性验证
- ✅ 7个场景生成相关API
- ✅ 时间线基础CRUD
- ✅ 场景与时间线自动关联

### 文档产出
- ✅ 场景生成系统文档
- ✅ 第二周开发总结
- ✅ API使用示例

---

## 🎯 成功标准

完成本周开发后，应能实现：

1. **场景生成**:
   - 用户提供场景类型、地点、氛围等参数
   - 系统自动生成符合世界观和角色设定的场景
   - 场景包含丰富的感官细节

2. **场景验证**:
   - 自动检查场景与世界观的一致性
   - 验证角色行为是否符合性格
   - 提供改进建议

3. **时间线集成**:
   - 场景中的关键事件自动提取到时间线
   - 事件按时间顺序排列
   - 支持查询角色的所有相关事件

4. **系统集成**:
   - 场景生成使用角色记忆系统（第一周）
   - 场景验证使用世界观验证器（第二周Phase 1）
   - 场景事件自动添加到时间线

---

## 💡 开发提示

### 提示词工程
- 参考WorldviewGenerationService的提示词结构
- 场景提示词需要更注重感官细节和氛围营造
- 使用Chain of Thought让AI先分析场景目的，再生成内容

### 性能优化
- 场景生成可能耗时20-40秒，考虑添加进度反馈
- 批量生成时使用并发调用（注意API限流）
- 大段场景可以分段生成

### 数据结构
- 感官细节使用JSONB存储，方便扩展
- 场景可以关联多个角色、时间线事件
- 保留AI生成的原始响应，便于调试

---

**准备开始开发了吗？建议从SceneType枚举开始，逐步实现！**
