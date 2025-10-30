# Phase 3: 记忆和一致性系统 - 使用指南

## 📋 概述

Phase 3 实现了完整的记忆系统与一致性验证流程，将所有功能整合到统一的生成管道中。

### ✅ 已实现的核心功能

1. **MemoryIntegrationService** - 记忆集成服务
   - 自动检索相关记忆
   - 将记忆注入到生成提示词
   - 从生成内容中提取并创建新记忆

2. **GenerationPipeline** - 统一生成管道
   - AI内容生成
   - 角色一致性验证
   - 世界观一致性验证
   - 自动重试机制（验证失败时）

3. **StoryGenerationController** - REST API
   - 统一的生成接口
   - 多种内容类型支持（章节、对话、场景、内心独白）

---

## 🚀 快速开始

### 1. API 端点

**基础URL**: `http://localhost:8080/api/generation`

**主要端点**:
- `POST /execute` - 执行完整生成流程（推荐）
- `POST /chapter` - 生成章节（简化接口）
- `POST /dialogue` - 生成对话
- `POST /scene` - 生成场景
- `POST /monologue` - 生成内心独白
- `POST /quick-test` - 快速测试（不验证、不创建记忆）

---

## 📖 使用示例

### 示例 1: 生成完整章节（带记忆和验证）

```bash
curl -X POST http://localhost:8080/api/generation/chapter \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "projectId=550e8400-e29b-41d4-a716-446655440001" \
  -d "characterIds=550e8400-e29b-41d4-a716-446655440002" \
  -d "characterIds=550e8400-e29b-41d4-a716-446655440003" \
  -d "worldviewId=550e8400-e29b-41d4-a716-446655440004" \
  -d "sceneContext=两位主角在废弃工厂中遭遇突袭" \
  -d "generationGoal=紧张刺激,展现角色性格"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "generatedContent": "生成的章节内容...",
    "success": true,
    "generatedAt": "2025-10-29T10:30:00",
    "passedAllValidation": true,
    "retryCount": 0,
    "memoriesUsed": 8,
    "newMemoryIds": [
      "uuid-1",
      "uuid-2"
    ],
    "characterConsistencyResults": [
      {
        "overallScore": 0.85,
        "passed": true,
        "violations": []
      }
    ],
    "worldviewConsistencyResult": {
      "overallScore": 0.90,
      "passed": true,
      "violations": []
    },
    "durationMs": 3500,
    "logs": [
      "[2025-10-29T10:30:00] 开始生成流程",
      "[2025-10-29T10:30:01] 检索到 8 条相关记忆",
      "[2025-10-29T10:30:03] 生成内容长度: 1200 字符",
      "[2025-10-29T10:30:03] 角色 张三 验证通过 (得分: 0.85)",
      "[2025-10-29T10:30:03] 世界观验证通过 (得分: 0.90)",
      "[2025-10-29T10:30:03] 成功创建 2 条新记忆",
      "[2025-10-29T10:30:03] 生成流程完成，总耗时: 3500ms"
    ]
  }
}
```

---

### 示例 2: 使用完整 GenerationRequest

```bash
curl -X POST http://localhost:8080/api/generation/execute \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "worldviewId": "550e8400-e29b-41d4-a716-446655440004",
    "characterIds": [
      "550e8400-e29b-41d4-a716-446655440002",
      "550e8400-e29b-41d4-a716-446655440003"
    ],
    "contentType": "DIALOGUE",
    "sceneContext": "夜晚，废弃工厂的天台上，两人对峙",
    "emotionalTone": "紧张、对抗",
    "generationGoal": "两人因立场不同产生激烈争执，但内心都不想伤害对方",
    "enableMemory": true,
    "memoryCount": 5,
    "enableConsistencyCheck": true,
    "maxRetries": 2,
    "temperature": 0.9,
    "maxTokens": 1500,
    "autoCreateMemory": true
  }'
```

---

### 示例 3: 快速测试（不验证）

用于快速查看生成效果，跳过验证和记忆创建：

```bash
curl -X POST http://localhost:8080/api/generation/quick-test \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "projectId=550e8400-e29b-41d4-a716-446655440001" \
  -d "characterIds=550e8400-e29b-41d4-a716-446655440002" \
  -d "prompt=描写主角在清晨醒来时的心情"
```

---

## 🔧 核心参数说明

### GenerationRequest 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| projectId | UUID | ✅ | - | 项目ID |
| characterIds | List<UUID> | ✅ | - | 参与角色ID列表 |
| contentType | ContentType | ✅ | - | 内容类型 |
| worldviewId | UUID | ❌ | null | 世界观ID |
| sceneContext | String | ❌ | - | 场景描述/上下文 |
| emotionalTone | String | ❌ | - | 情感基调 |
| generationGoal | String | ❌ | - | 生成目标/要求 |
| previousContent | String | ❌ | - | 前文内容 |
| enableMemory | Boolean | ❌ | true | 是否启用记忆检索 |
| memoryCount | Integer | ❌ | 5 | 每个角色检索的记忆数量 |
| enableConsistencyCheck | Boolean | ❌ | true | 是否进行一致性验证 |
| maxRetries | Integer | ❌ | 2 | 最大重试次数 |
| temperature | Double | ❌ | 0.8 | AI温度参数（创意性） |
| maxTokens | Integer | ❌ | 2000 | 最大生成token数 |
| autoCreateMemory | Boolean | ❌ | true | 是否自动创建记忆 |
| timelineId | UUID | ❌ | null | 时间线ID |

### ContentType 枚举

- `CHAPTER` - 章节
- `DIALOGUE` - 对话
- `SCENE` - 场景描写
- `INNER_MONOLOGUE` - 内心独白
- `NARRATIVE` - 叙述
- `ACTION` - 行动
- `CHARACTER` - 角色设定
- `WORLDVIEW` - 世界观

---

## 🧠 记忆系统工作流程

### 1. 记忆检索流程

```
用户请求生成
    ↓
检索每个角色的相关记忆
    ↓
根据场景上下文、情感基调计算相关性
    ↓
选择前N条最相关的记忆
    ↓
更新记忆的访问记录（遗忘曲线）
    ↓
注入到生成提示词
```

### 2. 记忆创建流程

```
AI生成内容
    ↓
使用AI分析内容，提取关键事件
    ↓
解析提取结果（角色|类型|内容|权重|关键词）
    ↓
为每个角色创建CharacterMemory
    ↓
保存到数据库
    ↓
返回新记忆ID列表
```

### 3. 记忆相关性计算

```
相关性得分 =
  关键词匹配度 × 30% +
  情感共鸣度 × 20% +
  记忆可访问性 × 30% +
  重要性权重 × 20%
```

---

## ✅ 一致性验证流程

### 验证管道

```
生成内容
    ↓
角色一致性验证
    ├─ 性格向量匹配
    ├─ 语言模式验证
    ├─ 行为习惯检查
    └─ 性格特征检查
    ↓
世界观一致性验证
    ├─ 规则遵守检查
    ├─ 约束条件验证
    ├─ 物理规律验证
    └─ 术语使用检查
    ↓
所有验证通过？
    ├─ 是 → 返回内容
    └─ 否 → 注入反馈 → 重新生成（最多N次）
```

### 验证结果示例

```json
{
  "characterConsistencyResults": [
    {
      "overallScore": 0.75,
      "vectorScore": 0.80,
      "speechPatternValid": true,
      "behaviorPatternValid": true,
      "violations": [
        "勇敢的角色不应表现出过度胆怯"
      ],
      "passed": false,
      "aiSuggestions": "建议修改..."
    }
  ],
  "worldviewConsistencyResult": {
    "overallScore": 0.90,
    "violations": [],
    "passed": true
  }
}
```

---

## 📊 完整流程示意图

```
┌─────────────────────────────────────────────────────────────┐
│                    GenerationPipeline                        │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  1. 构建生成上下文（MemoryIntegrationService）              │
│     ├─ 加载项目、角色、世界观                                │
│     ├─ 检索相关记忆（CharacterMemoryEnhancedService）       │
│     └─ 构建完整上下文对象                                     │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  2. 构建增强提示词                                           │
│     ├─ 基础提示词（根据内容类型）                            │
│     ├─ 注入世界观信息                                        │
│     ├─ 注入角色信息和记忆                                    │
│     └─ 注入场景和情感基调                                    │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  3. AI生成内容（AiGenerationService）                        │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  4. 一致性验证                                               │
│     ├─ 角色验证（CharacterConsistencyValidator）            │
│     └─ 世界观验证（WorldviewConsistencyValidator）          │
└─────────────────────────────────────────────────────────────┘
                             │
                    验证通过？
                   ╱          ╲
                 是            否
                 │              │
                 │              ▼
                 │    ┌──────────────────────┐
                 │    │ 注入验证反馈         │
                 │    │ retryCount++         │
                 │    └──────────────────────┘
                 │              │
                 │    retryCount > maxRetries？
                 │        ╱          ╲
                 │      否            是
                 │      │             │
                 │      └─────┐       │
                 │            │       │
                 └────────────┴───────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  5. 提取并创建记忆（如果启用）                               │
│     ├─ AI分析内容，提取关键事件                             │
│     ├─ 解析提取结果                                          │
│     └─ 创建CharacterMemory并保存                            │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│  6. 返回GenerationResult                                     │
│     ├─ 生成内容                                              │
│     ├─ 验证结果                                              │
│     ├─ 新建记忆ID                                            │
│     ├─ 统计信息                                              │
│     └─ 完整日志                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 最佳实践

### 1. 记忆管理

**为角色添加初始记忆**:
```bash
# 使用现有的 CharacterMemory API
POST /api/character-memories
{
  "characterId": "uuid",
  "memoryType": "核心记忆",
  "memoryContent": "从小在孤儿院长大，渴望家庭温暖",
  "emotionalWeight": 0.9,
  "keywords": ["孤儿院", "童年", "孤独", "渴望"]
}
```

**定期检索重要记忆**:
```bash
GET /api/character-memories/important/{characterId}?limit=10
```

### 2. 生成策略

**章节生成推荐配置**:
- temperature: 0.8 (平衡创意和连贯性)
- maxTokens: 2000-3000
- memoryCount: 5-8
- enableConsistencyCheck: true
- maxRetries: 2

**对话生成推荐配置**:
- temperature: 0.9 (更自然的对话)
- maxTokens: 1000-1500
- memoryCount: 3-5
- enableConsistencyCheck: true

**场景描写推荐配置**:
- temperature: 0.85
- maxTokens: 1500
- enableMemory: false (场景通常不需要记忆)
- enableConsistencyCheck: true (主要验证世界观)

### 3. 一致性验证

**验证阈值**:
- 0.9+ : 优秀，完全符合设定
- 0.7-0.9 : 良好，可以接受
- 0.5-0.7 : 一般，建议重试
- <0.5 : 较差，必须重试

**处理验证失败**:
1. 检查 `violations` 列表，了解具体问题
2. 查看 `aiSuggestions` 获取AI的修改建议
3. 如果是系统性问题，考虑调整角色设定或世界观规则
4. 增加 `maxRetries` 次数

### 4. 性能优化

**减少延迟**:
- 快速测试时关闭验证和记忆
- 减少 `memoryCount` 参数
- 降低 `maxTokens`

**提高质量**:
- 为角色添加详细的初始记忆
- 完善角色的 `speechPattern` 和 `behavioralHabits`
- 丰富世界观的 `rules` 和 `constraints`

---

## 🐛 故障排查

### 问题 1: 生成内容不符合角色性格

**可能原因**:
- 角色设定不够详细
- 相关记忆不足
- AI温度参数过高

**解决方案**:
1. 完善角色的 `personalityTraits`、`speechPattern`
2. 为角色添加更多初始记忆
3. 降低 `temperature` 参数（0.7-0.8）
4. 增加 `maxRetries` 确保验证通过

### 问题 2: 验证总是失败

**可能原因**:
- 验证规则过于严格
- 角色设定矛盾
- 世界观规则冲突

**解决方案**:
1. 检查 `violations` 列表
2. 查看 `aiSuggestions` 了解AI建议
3. 调整角色设定或世界观规则
4. 如果是边缘情况，可以降低验证阈值

### 问题 3: 记忆创建失败

**可能原因**:
- AI响应格式不符合预期
- 场景描述不够具体
- 角色信息缺失

**解决方案**:
1. 查看日志中的错误信息
2. 系统会自动使用降级方案（创建简单记忆）
3. 手动创建记忆补充

### 问题 4: 生成速度慢

**可能原因**:
- 检索记忆过多
- Token数量过大
- 多次重试

**解决方案**:
1. 减少 `memoryCount` 参数
2. 降低 `maxTokens`
3. 快速测试时关闭验证（`enableConsistencyCheck: false`）

---

## 📚 相关文档

- [API_STRUCTURE.md](./API_STRUCTURE.md) - API详细说明
- [enhanced_feature_modules.md](./enhanced_feature_modules.md) - 功能模块设计
- [spring_ai_prompt_engineering.md](./spring_ai_prompt_engineering.md) - AI集成指南
- [CLAUDE.md](../CLAUDE.md) - 项目总体说明

---

## 🔄 下一步计划

### Phase 4: 故事生成核心（第4周）
- 章节生成系统
- 对话生成系统
- 质量评估系统
- 导出功能

### Phase 5: 前端完善（第5周）
- Vue组件开发
- 状态管理优化
- 实时生成展示
- 记忆可视化

---

**更新日期**: 2025-10-29
**版本**: 1.0.0
**作者**: StoryForge Team
