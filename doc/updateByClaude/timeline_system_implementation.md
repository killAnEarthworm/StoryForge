# 时间线系统实现总结

## ✅ 已完成组件

### 1. TimelineEventType 枚举（209行）
- 17种事件类型（对话、行动、内心变化、环境变化、冲突、发现、决定、转折、成长、失去、获得、相遇、分离、危机、高潮、解决、其他）
- 每种类型包含：代码、显示名称、描述、推荐重要度
- 辅助方法：
  - `isKeyEvent()` - 判断是否为关键事件
  - `isCharacterEvent()` - 判断是否为角色事件
  - `isPlotEvent()` - 判断是否为情节事件
  - `getRecommendedImportance()` - 获取推荐重要度（1-10）

### 2. TimelineDTO 增强
- 添加 `@Builder` 注解支持
- 添加 `getSimpleSummary()` 方法用于简要展示

### 3. TimelineService 增强（~325行）
**新增项目级别时间线管理方法**：
- `getCompleteProjectTimeline()` - 获取项目完整时间线
- `getKeyEvents()` - 获取关键事件（可设置重要度阈值）
- `getEventsByType()` - 按类型获取事件
- `batchCreateTimelines()` - 批量创建事件
- `addEventFromScene()` - 从场景描述创建事件

### 4. TimelineController 增强（~390行）
**原有基础端点**：
- GET `/api/timelines` - 获取所有时间线
- GET `/api/timelines/{id}` - 获取单个事件
- GET `/api/timelines/project/{projectId}` - 获取项目时间线
- GET `/api/timelines/character/{characterId}` - 获取角色时间线
- POST `/api/timelines` - 创建时间线事件
- PUT `/api/timelines/{id}` - 更新事件
- DELETE `/api/timelines/{id}` - 删除事件

**新增项目管理端点**：
- GET `/api/timelines/project/{projectId}/complete` - 完整项目时间线
- GET `/api/timelines/project/{projectId}/key-events?threshold=7` - 关键事件
- GET `/api/timelines/project/{projectId}/by-type/{eventType}` - 按类型筛选
- POST `/api/timelines/batch` - 批量创建（最多50个）
- POST `/api/timelines/from-scene` - 从场景描述创建事件

**新增辅助端点**：
- GET `/api/timelines/event-types` - 获取所有事件类型
- GET `/api/timelines/event-types/{code}` - 获取事件类型详情
- GET `/api/timelines/suggestions?eventType=turning_point` - 获取创建建议

### 5. 场景与时间线集成（SceneGenerationService ~260行新增）
**自动集成功能**：
- `extractAndCreateTimelineEvents()` - 场景生成时自动提取事件
- 场景类型到事件类型的智能映射
- 基于多维度的记忆重要度计算
- 为每个参与角色自动创建事件

**手动提取功能**：
- `extractTimelineEventFromExistingScene()` - 为已存在场景提取事件
- 从场景内容推断事件类型
- 从场景情绪推断记忆重要度

**新增API端点（SceneGenerationController）**：
- POST `/api/scenes/{sceneId}/extract-timeline-event` - 手动提取事件

---

## 🎯 核心架构设计

### 时间线模型
- **一个项目 = 一条时间线**
- Timeline 实体的每条记录 = 时间线上的一个事件
- 事件按 `eventTime` 排序构成完整时间线

### 事件类型层次
```
17种事件类型
├─ 关键事件 (5种): 转折、高潮、危机、决定、发现
├─ 角色事件 (6种): 成长、内心变化、相遇、分离、失去、获得
└─ 情节事件 (5种): 行动、冲突、发现、决定、解决
```

### 场景→时间线映射
| 场景类型 | 事件类型 | 推荐重要度 |
|---------|---------|-----------|
| ACTION | action | 6 |
| DIALOGUE | dialogue | 6 |
| EMOTIONAL | inner_change | 7 |
| CONFLICT | conflict | 8 |
| CLIMAX | climax | 9 |
| TRANSITION | environment_change | 5 |
| OPENING | encounter | 7 |
| ENDING | resolution | 7 |

---

## 📊 代码统计

| 组件 | 文件 | 新增/修改行数 |
|------|------|-------------|
| TimelineEventType | TimelineEventType.java | 209（新增）|
| TimelineDTO | TimelineDTO.java | +10（修改）|
| TimelineService | TimelineService.java | +105（新增方法）|
| TimelineController | TimelineController.java | +245（新增端点）|
| SceneGenerationService | SceneGenerationService.java | +260（集成逻辑）|
| SceneGenerationController | SceneGenerationController.java | +30（新端点）|
| **总计** | **6个文件** | **~860行** |

---

## 🔌 REST API 使用示例

### 1. 获取项目完整时间线
```bash
GET /api/timelines/project/{projectId}/complete

响应：
{
  "code": 200,
  "message": "成功获取项目时间线，共 15 个事件",
  "data": [
    {
      "id": "...",
      "projectId": "...",
      "characterId": "...",
      "eventTime": "2024-01-15T10:30:00",
      "relativeTime": "三年前",
      "eventType": "encounter",
      "eventDescription": "在咖啡馆的午后，首次相遇",
      "memoryImportance": 7,
      ...
    },
    ...
  ]
}
```

### 2. 获取关键事件
```bash
GET /api/timelines/project/{projectId}/key-events?threshold=8

响应：返回重要度≥8的关键事件
```

### 3. 批量创建事件
```bash
POST /api/timelines/batch
Content-Type: application/json

[
  {
    "projectId": "...",
    "characterId": "...",
    "eventType": "conflict",
    "eventDescription": "与守卫发生冲突",
    "memoryImportance": 8
  },
  {
    "projectId": "...",
    "characterId": "...",
    "eventType": "discovery",
    "eventDescription": "发现了密室",
    "memoryImportance": 9
  }
]
```

### 4. 从场景自动创建事件
```bash
POST /api/timelines/from-scene
Content-Type: application/json

{
  "projectId": "...",
  "characterId": "...",
  "sceneDescription": "在废弃工厂的深夜，主角发现了真相",
  "eventType": "discovery",
  "memoryImportance": 9
}
```

### 5. 场景生成时自动提取事件
```bash
POST /api/scenes/generate
Content-Type: application/json

{
  "projectId": "...",
  "sceneType": "CONFLICT",
  "location": "废弃工厂",
  "mood": "紧张",
  "characterIds": ["char-id-1", "char-id-2"],
  "extractTimelineEvents": true,  // 默认为true
  ...
}

# 场景生成完成后，自动为每个参与角色创建时间线事件
```

### 6. 为已有场景手动提取事件
```bash
POST /api/scenes/{sceneId}/extract-timeline-event
Content-Type: application/json

{
  "characterId": "...",
  "eventType": "conflict"  // 可选，不提供则自动推断
}
```

### 7. 获取事件类型列表
```bash
GET /api/timelines/event-types

响应：
{
  "code": 200,
  "data": [
    {
      "code": "turning_point",
      "displayName": "转折",
      "description": "故事发展方向的重大转变",
      "isKeyEvent": true,
      "isCharacterEvent": false,
      "isPlotEvent": false,
      "recommendedImportance": 9
    },
    ...
  ]
}
```

### 8. 获取创建建议
```bash
GET /api/timelines/suggestions?eventType=climax

响应：
{
  "code": 200,
  "data": {
    "eventType": "高潮",
    "description": "故事情节的最高峰",
    "recommendedImportance": 10,
    "tips": [
      "这是关键事件，建议设置较高的记忆重要度（7-10）",
      "关键事件应该对故事发展产生重大影响"
    ],
    "generalTips": [
      "关键事件（转折、高潮、危机等）重要度建议8-10",
      "角色成长、内心变化等事件重要度建议4-7",
      ...
    ]
  }
}
```

---

## 🔧 核心特性

### 1. 智能事件提取
- **自动映射**：场景类型 → 事件类型
- **多维度计算**：基于场景类型、情感强度、关键事件自动计算重要度
- **批量处理**：为每个参与角色自动创建事件

### 2. 灵活的重要度系统
```
记忆重要度计算公式：
基础分(5)
+ 场景类型加分(0-4)
+ 情感强度加分(0-1)
+ 关键事件加分(0-1)
+ 冲突点加分(0-1)
= 最终重要度(1-10)
```

### 3. 完整的项目时间线视图
- 合并所有角色事件
- 按时间排序
- 支持筛选（类型、重要度）

### 4. 事件类型分类
- **关键事件**：影响故事走向
- **角色事件**：影响角色成长
- **情节事件**：推进故事情节

---

## ✅ SQL变更

**无需SQL变更** ✓
使用现有Timeline实体结构，每条记录代表一个事件。

---

## 🚀 使用建议

### 事件重要度参考
| 事件类型 | 推荐重要度 | 说明 |
|---------|-----------|------|
| 高潮 | 10 | 故事最高峰 |
| 转折、危机 | 9 | 重大转变 |
| 决定、发现、失去 | 8 | 关键事件 |
| 成长、获得 | 7 | 角色发展 |
| 冲突、解决 | 6 | 情节推进 |
| 相遇、分离 | 5 | 关系变化 |
| 内心变化 | 4 | 情感波动 |
| 行动、对话 | 3 | 日常互动 |
| 环境变化 | 2 | 背景描述 |
| 其他 | 1 | 琐碎事件 |

### 场景生成最佳实践
1. **启用自动提取**：设置 `extractTimelineEvents: true`
2. **指定参与角色**：确保 `characterIds` 包含所有关键角色
3. **提供场景目的**：设置 `scenePurpose` 帮助生成更准确的事件描述
4. **关键事件列表**：通过 `keyEvents` 明确重要事件

### 时间线管理建议
- 关键事件阈值建议设为7-8
- 批量创建限制在50个以内
- 为重要场景手动补充时间线事件
- 定期检查项目完整时间线确保连贯性

---

## 🔗 集成关系

```
项目时间线系统集成架构：

SceneGenerationService
    ↓ (自动提取)
TimelineService
    ↓ (创建事件)
Timeline Entity
    ↓ (关联)
Character & Project

查询流程：
TimelineController
    ↓
TimelineService
    ├─ 完整时间线查询
    ├─ 关键事件筛选
    ├─ 类型筛选
    └─ 记忆检索支持
```

---

## 📝 下一步扩展（可选）

1. **AI辅助事件提取**：使用AI分析场景内容，智能提取多个细粒度事件
2. **事件关联分析**：分析事件间的因果关系
3. **时间线可视化**：生成时间线图表
4. **冲突检测**：检测时间线上的逻辑冲突

---

**时间线系统实现完成** ✅
**无需SQL变更** ✓
**已完全集成场景生成系统** ✓
