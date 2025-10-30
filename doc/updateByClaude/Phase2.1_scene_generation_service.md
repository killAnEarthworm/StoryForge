# SceneGenerationService 实现总结

## 📋 基本信息

- **文件**: `src/main/java/com/linyuan/storyforge/service/SceneGenerationService.java`
- **代码行数**: 约730行
- **完成时间**: 2025-10-28

## ✅ 核心功能

### 1. 主要方法

| 方法 | 功能 | 说明 |
|------|------|------|
| `generateScene()` | 生成单个场景 | 主入口，完整流程 |
| `generateMultipleScenes()` | 批量生成 | 生成1-5个不同版本 |
| `expandScene()` | 场景扩展 | 对已有场景某部分扩展 |
| `buildScenePrompt()` | 构建提示词 | 核心逻辑，集成所有上下文 |
| `parseSceneResponse()` | 解析AI响应 | JSON解析和fallback |

### 2. 生成流程

```
请求验证 → 加载上下文 → 构建提示词 → AI生成 → 解析响应 → 保存数据库
```

### 3. 上下文集成

**GenerationContext** 包含：
- ✅ 世界观设定（规则、约束）
- ✅ 角色信息（性格、说话方式）
- ✅ 角色记忆（每个角色最相关的3条）
- ✅ 前置场景（保持连贯性）

## 🎯 提示词结构（13个部分）

1. **角色定位** - 定义AI的专业角色
2. **任务说明** - 明确场景类型
3. **场景类型指导** - 使用SceneType的详细要求
4. **世界观背景** - 规则和约束
5. **场景设定** - 地点、时间、天气、情绪
6. **参与角色** - 角色信息 + 相关记忆
7. **情节要素** - 目的、背景、关键事件、冲突
8. **前置场景** - 保持连贯性
9. **感官描写要求** - 具体感官指导
10. **写作风格和长度** - 字数、风格、密度、对话比例
11. **约束条件** - 必须包含/避免、语言限制
12. **输出格式** - JSON结构定义
13. **质量标准** - 6条质量要求

## 🔧 关键特性

### 1. 智能记忆集成
- 自动检索每个角色最相关的3条记忆
- 基于场景上下文和情绪状态匹配
- 记忆内容融入提示词，使角色行为更合理

### 2. 世界观一致性
- 自动加载世界观规则和约束
- 在提示词中明确禁止违反规则
- 保证生成内容符合设定

### 3. 多维度感官细节
- 支持5种感官（视觉、听觉、嗅觉、触觉、味觉）
- 根据SceneType自动提供感官指导
- 提取感官细节到JSONB字段

### 4. 灵活的写作控制
- 字数控制（100-3000）
- 对话比例控制（0-100%）
- 描写密度（LOW/MEDIUM/HIGH）
- 创意度调节（0.0-1.0）

### 5. 批量生成策略
- 每次生成调整创意度（+0.05）
- 生成不同风格的版本
- 失败不中断，继续生成其他方案

### 6. 容错机制
- JSON解析失败时返回fallback场景
- 资源不存在时记录警告但继续
- 保存原始AI响应到content字段

## 📊 依赖服务

```java
- AiGenerationService           // AI调用
- SceneService                  // 场景CRUD
- WorldviewService              // 世界观加载
- CharacterService              // 角色信息
- CharacterMemoryEnhancedService // 记忆检索
- ProjectRepository             // 项目验证
- SceneRepository               // 场景保存
```

## 💡 使用示例

### 基础使用
```java
SceneGenerationRequest request = SceneGenerationRequest.builder()
    .projectId(projectId)
    .sceneType(SceneType.ACTION)
    .location("废弃工厂")
    .mood("紧张")
    .characterIds(List.of(char1Id, char2Id))
    .keyEvents(List.of("激烈战斗", "主角险胜"))
    .build();

SceneDTO scene = sceneGenerationService.generateScene(request);
```

### 批量生成
```java
List<SceneDTO> scenes = sceneGenerationService
    .generateMultipleScenes(request, 3);
// 返回3个不同版本供选择
```

### 场景扩展
```java
SceneDTO expanded = sceneGenerationService
    .expandScene(sceneId, "战斗高潮部分", 500);
```

## 🎨 设计亮点

1. **提示词工程**
   - 13部分结构化提示词
   - 充分利用SceneType的元数据
   - 动态组装，只包含必要信息

2. **上下文感知**
   - 智能加载相关记忆
   - 考虑前置场景连贯性
   - 遵守世界观规则

3. **可控性强**
   - 30+参数精细控制
   - 感官、风格、长度、对话比例
   - 约束条件灵活设置

4. **性能优化**
   - Token数量动态计算
   - 避免加载不必要的数据
   - 批量生成失败不中断

## ⚠️ 注意事项

1. **Token消耗**
   - 提示词可能很长（2000-5000字符）
   - 包含角色记忆和世界观后更长
   - 建议监控API成本

2. **生成时间**
   - 单次生成约20-40秒
   - 批量生成时间 × 数量
   - 考虑添加异步处理

3. **记忆检索**
   - 每个角色最多3条记忆
   - 避免提示词过长
   - 可根据需要调整

4. **前置场景**
   - 只使用摘要（最多300字符）
   - 避免上下文窗口溢出

## 🚀 下一步

接下来需要实现：
1. **SceneConsistencyValidator** - 场景一致性验证
2. **SceneGenerationController** - REST API接口
3. 集成测试和文档完善

---

**实现完成** ✅ 核心生成逻辑已就绪，可进行集成测试。
