# Phase 4 阶段1 快速参考

## 🚀 快速开始

### 1. 生成第一章

```bash
POST /api/chapters/generate
{
  "projectId": "your-project-id",
  "chapterNumber": 1,
  "title": "第一章：开端",
  "characterIds": ["char-id-1"],
  "sceneContext": "故事背景描述",
  "targetWordCount": 2000
}
```

### 2. 生成后续章节（自动加载前文）

```bash
POST /api/chapters/generate
{
  "projectId": "your-project-id",
  "chapterNumber": 2,
  "characterIds": ["char-id-1"],
  "loadPreviousContext": true
}
```

### 3. 重新生成（保留原版本）

```bash
POST /api/chapters/{chapter-id}/regenerate
{
  "changeInstructions": "增加动作描写",
  "keepOriginal": true
}
```

---

## 📋 API 端点速查

| 功能 | 端点 | 方法 |
|------|------|------|
| 生成章节 | `/api/chapters/generate` | POST |
| 重新生成 | `/api/chapters/{id}/regenerate` | POST |
| 优化章节 | `/api/chapters/{id}/refine` | POST |
| 生成大纲 | `/api/chapters/generate-outline` | POST |
| 版本列表 | `/api/chapters/versions?projectId=xx&chapterNumber=1` | GET |
| 前文上下文 | `/api/chapters/context?projectId=xx&upToChapter=5` | GET |
| 最佳参数 | `/api/chapters/analyze-parameters?projectId=xx` | GET |
| 生成统计 | `/api/chapters/statistics?projectId=xx` | GET |

---

## 🎛️ 参数速查

### 必填参数
- `projectId` - 项目ID
- `chapterNumber` - 章节编号
- `characterIds` - 角色ID列表

### 常用可选参数
- `title` - 章节标题
- `outline` - 章节大纲
- `targetWordCount` (默认2000) - 目标字数
- `tone` - 基调（如：紧张、温馨）
- `temperature` (默认0.8) - 创意性
- `loadPreviousContext` (默认true) - 加载前文
- `enableMemory` (默认true) - 启用记忆
- `enableConsistencyCheck` (默认true) - 一致性验证

---

## 💡 常见场景

### 场景1: 快速测试
```json
{
  "projectId": "xxx",
  "chapterNumber": 1,
  "characterIds": ["xxx"],
  "sceneContext": "测试场景",
  "enableConsistencyCheck": false,
  "autoCreateMemory": false
}
```

### 场景2: 高质量生成
```json
{
  "projectId": "xxx",
  "chapterNumber": 1,
  "title": "精心设计的标题",
  "outline": "详细的大纲",
  "characterIds": ["xxx"],
  "worldviewId": "xxx",
  "targetWordCount": 3000,
  "tone": "史诗、震撼",
  "temperature": 0.85,
  "enableMemory": true,
  "memoryCount": 8,
  "maxRetries": 3
}
```

### 场景3: 重新生成（更有创意）
```json
{
  "changeInstructions": "增加情感深度",
  "keepOriginal": true,
  "moreCreative": true
}
```

### 场景4: 重新生成（更保守）
```json
{
  "changeInstructions": "减少幻想元素",
  "keepOriginal": true,
  "moreConservative": true
}
```

---

## 🔧 故障排查速查

| 问题 | 解决方案 |
|------|----------|
| 章节已存在 | 使用 `/regenerate` 或指定 `saveAsVersion` |
| 生成太短 | 增加 `targetWordCount` |
| 生成太长 | 减少 `targetWordCount` 或设置 `maxTokens` |
| 不连贯 | 确保 `loadPreviousContext: true` |
| 质量低 | 查看 `violations`，调整设定 |
| 生成慢 | 减少 `targetWordCount`、关闭验证 |

---

## 📊 响应数据结构

```json
{
  "code": 200,
  "data": {
    "id": "chapter-uuid",
    "chapterNumber": 1,
    "title": "第一章",
    "generatedContent": "章节内容...",
    "version": 1,
    "status": "drafted",
    "generationParams": {
      "qualityScore": 0.85,
      "memoriesUsed": 5,
      "passedValidation": true
    },
    "generationResult": {
      "success": true,
      "durationMs": 4500,
      "retryCount": 0,
      "logs": ["..."]
    }
  }
}
```

---

## ⚡ 性能参考

- 快速生成: 1-2秒
- 标准生成(2000字): 3-5秒
- 长章节(5000字): 8-12秒

---

**完整文档**: `phase4_stage1_chapter_generation_guide.md`
