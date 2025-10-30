# Phase 4 阶段1: 章节生成系统 - 使用指南

## 📋 概述

Phase 4 阶段1 实现了完整的章节生成系统，将 Phase 3 的 GenerationPipeline 与 StoryChapter 实体深度整合，提供了生成、重新生成、优化、版本管理等完整功能。

---

## ✅ 已实现功能

### 核心服务

1. **StoryGenerationService** - 章节生成核心服务
   - 生成并保存章节
   - 重新生成章节（支持保留原版本）
   - 优化章节（基于用户反馈）
   - 生成章节大纲
   - 加载前文上下文
   - 版本管理

2. **GenerationHistoryEnhancedService** - 增强的历史记录服务
   - 自动记录每次生成
   - 计算质量得分
   - 分析最佳参数
   - 统计信息

### DTO 类

- **ChapterGenerationRequest** - 章节生成请求（扩展自 GenerationRequest）
- **RegenerateOptions** - 重新生成配置

### REST API

- **StoryChapterGenerationController** - 章节生成控制器
  - 10个端点覆盖所有生成场景

---

## 🚀 快速开始

### API端点列表

**基础URL**: `http://localhost:8080/api/chapters`

| 端点 | 方法 | 说明 |
|------|------|------|
| `/generate` | POST | 生成新章节 |
| `/{id}/regenerate` | POST | 重新生成章节 |
| `/{id}/refine` | POST | 优化章节 |
| `/generate-outline` | POST | 仅生成大纲 |
| `/versions` | GET | 获取所有版本 |
| `/context` | GET | 获取前文上下文 |
| `/analyze-parameters` | GET | 分析最佳参数 |
| `/statistics` | GET | 获取统计信息 |
| `/quick-generate` | POST | 快速生成（测试用） |

---

## 📖 详细使用示例

### 示例 1: 生成完整章节

```bash
curl -X POST http://localhost:8080/api/chapters/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "chapterNumber": 1,
    "title": "第一章：觉醒",
    "outline": "主角在废墟中醒来，发现世界已经发生巨变。他必须面对新的现实，并寻找生存之道。",
    "mainConflict": "主角vs环境，内心的恐惧vs求生的本能",
    "characterIds": [
      "char-uuid-1",
      "char-uuid-2"
    ],
    "worldviewId": "world-uuid-1",
    "targetWordCount": 2000,
    "tone": "紧张、神秘",
    "pacing": "快速",
    "emotionalTone": "恐惧、好奇",
    "sceneContext": "废弃的城市，夜晚，到处是残垣断壁",
    "generationGoal": "建立世界观，塑造主角性格，设置悬念",
    "loadPreviousContext": false,
    "enableMemory": true,
    "enableConsistencyCheck": true,
    "temperature": 0.8,
    "status": "drafted"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "chapter-uuid-1",
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "chapterNumber": 1,
    "title": "第一章：觉醒",
    "outline": "主角在废墟中醒来...",
    "generatedContent": "夜幕降临，废弃的城市笼罩在一片死寂之中...",
    "version": 1,
    "status": "drafted",
    "generationParams": {
      "temperature": 0.8,
      "maxTokens": 4800,
      "memoriesUsed": 0,
      "passedValidation": true,
      "retryCount": 0,
      "qualityScore": 0.85
    },
    "generationResult": {
      "success": true,
      "memoriesUsed": 0,
      "newMemoryIds": ["mem-uuid-1", "mem-uuid-2"],
      "passedAllValidation": true,
      "retryCount": 0,
      "durationMs": 4500,
      "logs": [
        "[2025-10-30T10:00:00] 开始生成流程",
        "[2025-10-30T10:00:04] 生成完成",
        "..."
      ]
    },
    "createdAt": "2025-10-30T10:00:00",
    "updatedAt": "2025-10-30T10:00:00"
  }
}
```

---

### 示例 2: 生成第二章（自动加载前文）

```bash
curl -X POST http://localhost:8080/api/chapters/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "chapterNumber": 2,
    "title": "第二章：初遇",
    "outline": "主角遇到第一个幸存者，两人产生信任与怀疑的博弈",
    "characterIds": ["char-uuid-1", "char-uuid-2"],
    "targetWordCount": 2500,
    "loadPreviousContext": true,
    "previousContextSize": 1,
    "enableMemory": true,
    "enableConsistencyCheck": true
  }'
```

**系统会自动**:
1. 加载第1章的内容作为前文上下文
2. 检索角色的相关记忆（包括第1章创建的记忆）
3. 确保情节连贯性
4. 创建新的记忆

---

### 示例 3: 重新生成章节（保留原版本）

```bash
curl -X POST http://localhost:8080/api/chapters/{chapter-id}/regenerate \
  -H "Content-Type: application/json" \
  -d '{
    "changeInstructions": "增加更多动作描写，减少内心独白，加快节奏",
    "keepOriginal": true,
    "newVersion": 2,
    "moreCreative": true,
    "refreshMemories": true
  }'
```

**效果**:
- 原版本（version=1）保留
- 生成新版本（version=2）
- 温度参数自动提高（更有创意）
- 重新检索记忆

---

### 示例 4: 优化章节（基于用户反馈）

```bash
curl -X POST http://localhost:8080/api/chapters/{chapter-id}/refine \
  -H "Content-Type: application/json" \
  -d '{
    "feedback": "主角的反应太淡定了，应该表现出更多恐惧和困惑。另外，环境描写不够生动。"
  }'
```

**系统会**:
1. 将用户反馈注入到生成目标
2. 保留原版本，创建新版本
3. AI会针对性地改进

---

### 示例 5: 仅生成大纲

```bash
curl -X POST http://localhost:8080/api/chapters/generate-outline \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "chapterNumber": 3,
    "characterIds": ["char-uuid-1"],
    "sceneContext": "主角进入一个废弃的超市寻找补给"
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "第三章大纲：\n\n1. 章节主题：生存与选择\n2. 主要情节点：\n   - 主角发现超市\n   - 遇到其他幸存者团队\n   - 产生冲突\n   - 做出艰难选择\n3. 人物发展：主角开始学会在新世界中生存\n4. 冲突和转折：信任与背叛的考验\n5. 章节结尾：主角做出关键决定，影响后续剧情"
}
```

---

### 示例 6: 获取章节的所有版本

```bash
curl "http://localhost:8080/api/chapters/versions?projectId=xxx&chapterNumber=1"
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "uuid-v1",
      "chapterNumber": 1,
      "version": 1,
      "status": "drafted",
      "generatedContent": "版本1的内容...",
      "generationParams": {"qualityScore": 0.75},
      "createdAt": "2025-10-30T10:00:00"
    },
    {
      "id": "uuid-v2",
      "chapterNumber": 1,
      "version": 2,
      "status": "revised",
      "generatedContent": "版本2的内容（改进后）...",
      "generationParams": {"qualityScore": 0.85},
      "createdAt": "2025-10-30T11:00:00"
    }
  ]
}
```

---

### 示例 7: 分析最佳生成参数

```bash
curl "http://localhost:8080/api/chapters/analyze-parameters?projectId=xxx"
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "temperature": 0.82,
    "maxTokens": 4200,
    "sampleSize": 10,
    "avgQualityScore": 83.5,
    "recommendation": "基于10次高质量生成的分析结果"
  }
}
```

---

## 🎯 核心参数说明

### ChapterGenerationRequest 参数详解

#### 必填参数

| 参数 | 类型 | 说明 |
|------|------|------|
| projectId | UUID | 项目ID |
| chapterNumber | Integer | 章节编号（必须>0） |
| characterIds | List<UUID> | 参与角色ID列表（至少1个） |

#### 章节设定

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | null | 章节标题 |
| outline | String | null | 章节大纲 |
| mainConflict | String | null | 主要冲突 |
| worldviewId | UUID | null | 世界观ID |
| mainSceneId | UUID | null | 主要场景ID |
| timelineId | UUID | null | 时间线ID |

#### 生成控制

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| targetWordCount | Integer | 2000 | 目标字数 |
| tone | String | null | 基调（如：紧张、温馨） |
| pacing | String | null | 节奏（如：快速、缓慢） |
| emotionalTone | String | null | 情感基调 |
| sceneContext | String | null | 场景描述 |
| generationGoal | String | null | 生成目标/要求 |

#### 上下文控制

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| loadPreviousContext | Boolean | true | 是否加载前文 |
| previousContextSize | Integer | 2 | 加载前N章 |

#### 记忆和验证

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enableMemory | Boolean | true | 启用记忆系统 |
| memoryCount | Integer | 5 | 每角色检索记忆数 |
| enableConsistencyCheck | Boolean | true | 一致性验证 |
| maxRetries | Integer | 2 | 最大重试次数 |
| autoCreateMemory | Boolean | true | 自动创建记忆 |

#### AI参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| temperature | Double | 0.8 | 创意性（0.0-2.0） |
| maxTokens | Integer | 自动计算 | 最大token数 |

#### 版本管理

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| saveAsVersion | Integer | 1 | 保存为第几版 |
| status | String | "drafted" | 章节状态 |

---

## 📊 完整生成流程

```
1. 用户提交 ChapterGenerationRequest
   ↓
2. StoryGenerationService.generateChapter()
   ├─ 验证请求参数
   ├─ 检查章节是否已存在
   ├─ 加载前文上下文（如果启用）
   └─ 转换为 GenerationRequest
   ↓
3. GenerationPipeline.execute()
   ├─ MemoryIntegrationService.buildGenerationContext()
   │    ├─ 加载项目、角色、世界观
   │    └─ 检索相关记忆
   ├─ MemoryIntegrationService.buildEnhancedPrompt()
   │    └─ 注入记忆、设定、前文
   ├─ AiGenerationService.chatWithOptions()
   │    └─ 调用百度千帆API
   ├─ 一致性验证（角色+世界观）
   │    ├─ 验证通过 → 继续
   │    └─ 验证失败 → 重试（最多N次）
   └─ MemoryIntegrationService.extractAndCreateMemories()
        └─ 从生成内容提取并创建新记忆
   ↓
4. StoryGenerationService 后处理
   ├─ 创建 StoryChapter 实体
   ├─ 保存到数据库
   └─ GenerationHistoryEnhancedService.recordGeneration()
        └─ 记录生成历史
   ↓
5. 返回 StoryChapterDTO
   ├─ 包含生成的内容
   ├─ 包含生成参数
   └─ 包含 GenerationResult（详细信息）
```

---

## 🔧 高级功能

### 1. 版本管理策略

**场景A: 第一次生成章节**
```json
{
  "chapterNumber": 1,
  "saveAsVersion": 1  // 明确指定，或省略（默认为1）
}
```
结果：创建 version=1 的章节

**场景B: 重新生成（保留原版本）**
```json
{
  "keepOriginal": true,
  "newVersion": 2  // 或省略，自动递增
}
```
结果：
- version=1 保留
- 创建 version=2

**场景C: 重新生成（覆盖原版本）**
```json
{
  "keepOriginal": false
}
```
结果：
- 删除原章节
- 创建新章节（version=1）

### 2. 前文上下文加载

**手动获取前文**:
```bash
curl "http://localhost:8080/api/chapters/context?projectId=xxx&upToChapter=5&contextSize=2"
```

**自动加载**:
在生成请求中设置：
```json
{
  "loadPreviousContext": true,
  "previousContextSize": 2
}
```

系统会：
1. 查找第 N-2 到 N-1 章
2. 优先使用大纲（outline）
3. 如无大纲，截取内容前300字
4. 格式化为Markdown，注入到提示词

### 3. 质量评分机制

**计算公式**:
```
基础分 = 50分
+ 一致性验证通过 = +30分（或 一致性得分 × 30）
- 重试次数 × 5分
+ 使用记忆 = +5分
+ 创建新记忆 = +5分
+ 5秒内完成 = +5分
= 总分（0-100）
```

**质量等级**:
- 90-100: 优秀
- 75-89: 良好
- 60-74: 一般
- <60: 需要改进

### 4. 参数优化建议

系统会分析历史生成数据，找出质量得分最高的参数组合。

**使用建议的参数**:
```bash
# 1. 获取建议
curl "http://localhost:8080/api/chapters/analyze-parameters?projectId=xxx"

# 2. 使用建议的参数生成
{
  "temperature": 0.82,  // 来自分析结果
  "maxTokens": 4200,    // 来自分析结果
  ...
}
```

---

## 🎨 最佳实践

### 1. 章节生成流程推荐

```
Step 1: 生成大纲
  POST /api/chapters/generate-outline

Step 2: 确认大纲，修改（如需要）

Step 3: 生成完整章节
  POST /api/chapters/generate
  （将大纲填入 outline 字段）

Step 4: 如不满意，重新生成或优化
  POST /api/chapters/{id}/regenerate
  或
  POST /api/chapters/{id}/refine
```

### 2. 推荐参数配置

**第一章（世界观建立）**:
```json
{
  "targetWordCount": 2000,
  "tone": "神秘、引人入胜",
  "pacing": "适中",
  "temperature": 0.85,
  "loadPreviousContext": false,
  "enableMemory": true,
  "enableConsistencyCheck": true
}
```

**中间章节（情节推进）**:
```json
{
  "targetWordCount": 2500,
  "tone": "紧张、跌宕",
  "pacing": "快速",
  "temperature": 0.8,
  "loadPreviousContext": true,
  "previousContextSize": 2,
  "enableMemory": true,
  "enableConsistencyCheck": true
}
```

**高潮章节（重要转折）**:
```json
{
  "targetWordCount": 3000,
  "tone": "震撼、激烈",
  "pacing": "极快",
  "temperature": 0.9,
  "loadPreviousContext": true,
  "previousContextSize": 3,
  "enableMemory": true,
  "memoryCount": 8,
  "enableConsistencyCheck": true,
  "maxRetries": 3
}
```

### 3. 处理生成失败

**一致性验证失败**:
```
检查 generationResult.violations
→ 根据违规项调整角色设定或世界观规则
→ 重新生成
```

**生成内容不理想**:
```
1. 使用 refine 端点，提供具体反馈
2. 或使用 regenerate，调整参数：
   - moreCreative: true  → 更有创意
   - moreConservative: true → 更保守
   - 调整 tone/pacing
```

---

## 🐛 故障排查

### 问题 1: 章节已存在错误

**错误**: `章节 1 已存在`

**解决方案**:
1. 使用 `regenerate` 端点而非 `generate`
2. 或在请求中指定 `saveAsVersion: 2`

### 问题 2: 生成内容过短/过长

**原因**:
- `targetWordCount` 设置不合理
- AI token限制

**解决方案**:
1. 调整 `targetWordCount`
2. 手动设置 `maxTokens`（中文：字数 × 2 × 1.2）

### 问题 3: 前文上下文未加载

**检查**:
- `loadPreviousContext` 是否为 true
- 是否是第1章（第1章无前文）
- 前面章节是否已生成

### 问题 4: 质量得分过低

**分析**:
1. 查看 `generationResult.characterConsistencyResults`
2. 查看 `generationResult.worldviewConsistencyResult`
3. 根据 `violations` 调整设定

---

## 📚 相关文档

- [Phase 3 记忆和一致性系统](./phase3_memory_consistency_guide.md)
- [GenerationPipeline 使用指南](./phase3_memory_consistency_guide.md#完整流程示意图)
- [API 总体结构](./API_STRUCTURE.md)

---

## 🔜 下一步

- **阶段 2**: 对话生成系统（DialogueGenerationService）
- **阶段 3**: 质量评估系统（QualityAssessmentService）
- **阶段 4**: 导出功能（ExportService）

---

**更新日期**: 2025-10-30
**版本**: 1.0.0
**作者**: StoryForge Team
