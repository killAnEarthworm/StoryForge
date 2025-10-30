# Phase 3 测试指南

## 测试准备

### 1. 启动后端服务

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

确保以下环境变量已设置：
- `DB_PASSWORD` - 数据库密码
- `QIANFAN_API_KEY` - 百度千帆API密钥

### 2. 准备测试数据

在测试前，需要创建以下数据：
1. 一个测试项目（Project）
2. 至少两个角色（Character）
3. 一个世界观（Worldview）

可以使用现有的API或直接在数据库中插入测试数据。

---

## 测试场景

### 场景1: 快速测试生成（无验证）

**目的**: 验证基础生成功能是否正常

**测试步骤**:

1. 调用快速测试接口：
```bash
curl -X POST "http://localhost:8080/api/generation/quick-test" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "projectId=YOUR_PROJECT_ID" \
  -d "characterIds=YOUR_CHARACTER_ID" \
  -d "prompt=描述主角在清晨醒来时的心情"
```

2. 检查响应：
- `success` 应为 `true`
- `generatedContent` 应包含生成的内容
- `durationMs` 应合理（通常1-3秒）

**预期结果**:
- 返回200状态码
- 生成内容长度合理（至少100字）
- 无错误信息

---

### 场景2: 带记忆的生成

**目的**: 验证记忆检索和注入功能

**前提**: 角色已有一些记忆数据

**测试步骤**:

1. 为角色添加测试记忆：
```bash
curl -X POST "http://localhost:8080/api/character-memories" \
  -H "Content-Type: application/json" \
  -d '{
    "characterId": "YOUR_CHARACTER_ID",
    "memoryType": "情感记忆",
    "memoryContent": "曾在战场上失去最好的朋友，对战争充满恐惧和憎恨",
    "emotionalWeight": 0.9,
    "keywords": ["战场", "朋友", "失去", "恐惧", "憎恨"]
  }'
```

2. 生成相关场景的内容：
```bash
curl -X POST "http://localhost:8080/api/generation/chapter" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "projectId=YOUR_PROJECT_ID" \
  -d "characterIds=YOUR_CHARACTER_ID" \
  -d "sceneContext=主角再次面对即将到来的战斗" \
  -d "generationGoal=展现角色内心的挣扎"
```

3. 检查响应：
- `memoriesUsed` 应该 > 0（表示检索到了记忆）
- `generatedContent` 应该反映出角色对战争的恐惧
- `newMemoryIds` 应该包含新创建的记忆

**预期结果**:
- 生成内容体现了角色的记忆和情感
- 系统自动创建了新的记忆

---

### 场景3: 一致性验证

**目的**: 验证一致性检查功能

**测试步骤**:

1. 设置一个性格鲜明的角色（如：勇敢、冲动）

2. 生成一段对话，故意要求与性格相反的行为：
```bash
curl -X POST "http://localhost:8080/api/generation/dialogue" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "projectId=YOUR_PROJECT_ID" \
  -d "characterIds=YOUR_CHARACTER_ID" \
  -d "sceneContext=面对敌人时，主角表现得极度胆怯，不敢行动" \
  -d "emotionalTone=恐惧、犹豫"
```

3. 检查响应：
- 如果角色设定为勇敢，`characterConsistencyResults` 应该包含违规项
- `passed` 可能为 `false`
- `retryCount` 可能 > 0（系统尝试重新生成）
- `violations` 列表应该包含类似"勇敢的角色不应表现出过度胆怯"的提示

**预期结果**:
- 系统检测到性格不一致
- 尝试重新生成或给出警告

---

### 场景4: 完整流程测试

**目的**: 测试完整的记忆+生成+验证+记忆创建流程

**测试步骤**:

1. 使用完整的GenerationRequest：
```bash
curl -X POST "http://localhost:8080/api/generation/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "YOUR_PROJECT_ID",
    "worldviewId": "YOUR_WORLDVIEW_ID",
    "characterIds": [
      "CHARACTER_ID_1",
      "CHARACTER_ID_2"
    ],
    "contentType": "DIALOGUE",
    "sceneContext": "两位主角在夜晚的天台上，讨论明天的任务",
    "emotionalTone": "严肃、略带紧张",
    "generationGoal": "展现两人的信任关系，同时透露任务的危险性",
    "enableMemory": true,
    "memoryCount": 5,
    "enableConsistencyCheck": true,
    "maxRetries": 2,
    "temperature": 0.9,
    "maxTokens": 1500,
    "autoCreateMemory": true
  }'
```

2. 详细检查响应的各个部分：

```json
{
  "code": 200,
  "data": {
    "success": true,                    // ✅ 生成成功
    "generatedContent": "...",          // ✅ 有内容
    "memoriesUsed": 8,                  // ✅ 使用了记忆
    "newMemoryIds": ["...", "..."],     // ✅ 创建了新记忆
    "passedAllValidation": true,        // ✅ 通过验证
    "retryCount": 0,                    // ✅ 一次成功
    "characterConsistencyResults": [    // ✅ 角色验证结果
      {
        "overallScore": 0.85,
        "passed": true
      }
    ],
    "worldviewConsistencyResult": {     // ✅ 世界观验证结果
      "overallScore": 0.90,
      "passed": true
    },
    "durationMs": 3500,                 // ✅ 耗时合理
    "logs": [...]                       // ✅ 完整日志
  }
}
```

3. 验证新创建的记忆：
```bash
# 获取角色的所有记忆
curl "http://localhost:8080/api/character-memories/character/YOUR_CHARACTER_ID"
```

检查最新的记忆是否包含对话中的关键信息。

**预期结果**:
- 所有步骤都成功完成
- 生成内容符合要求
- 记忆系统正常工作
- 一致性验证通过

---

## 性能基准测试

### 测试1: 生成速度

**不同配置下的预期耗时**:

| 配置 | 预期耗时 |
|------|----------|
| 快速测试（无记忆、无验证） | 1-2秒 |
| 基础生成（有记忆、无验证） | 2-3秒 |
| 完整流程（有记忆、有验证） | 3-5秒 |
| 需要重试1次 | 5-8秒 |
| 需要重试2次 | 8-12秒 |

### 测试2: 记忆检索效率

```bash
# 获取记忆统计
curl "http://localhost:8080/api/character-memories/statistics/YOUR_CHARACTER_ID"
```

检查：
- `totalCount` - 总记忆数
- `accessibleCount` - 可访问记忆数
- `byType` - 按类型统计

### 测试3: 并发测试

使用工具（如 Apache Bench）进行并发测试：

```bash
ab -n 10 -c 2 -p request.json -T "application/json" \
  http://localhost:8080/api/generation/quick-test
```

---

## 常见问题排查

### 问题1: 生成内容为空

**检查项**:
1. AI服务是否可用：
```bash
curl "http://localhost:8080/api/test/ai/hello"
```

2. 检查 `QIANFAN_API_KEY` 环境变量是否正确设置

3. 查看后端日志是否有错误信息

### 问题2: 记忆未被检索

**检查项**:
1. 确认角色确实有记忆数据：
```bash
curl "http://localhost:8080/api/character-memories/character/YOUR_CHARACTER_ID"
```

2. 检查记忆的 `accessibility` 字段是否 > 0.3

3. 检查记忆的 `keywords` 是否与 `sceneContext` 相关

### 问题3: 一致性验证总是失败

**检查项**:
1. 查看 `violations` 列表，了解具体问题
2. 检查角色设定是否自相矛盾
3. 临时关闭验证测试生成：
```bash
# 添加参数
"enableConsistencyCheck": false
```

### 问题4: 服务响应慢

**检查项**:
1. 数据库连接是否正常
2. AI服务响应时间（百度千帆API可能偶尔较慢）
3. 减少 `maxTokens` 和 `memoryCount` 参数

---

## 测试清单

### 基础功能
- [ ] 快速生成测试通过
- [ ] 章节生成正常
- [ ] 对话生成正常
- [ ] 场景描写正常
- [ ] 内心独白生成正常

### 记忆系统
- [ ] 记忆检索正常（memoriesUsed > 0）
- [ ] 记忆注入到提示词
- [ ] 新记忆自动创建
- [ ] 记忆可访问性更新正常

### 一致性验证
- [ ] 角色一致性验证工作正常
- [ ] 世界观一致性验证工作正常
- [ ] 验证失败时能检测到违规项
- [ ] 重试机制正常工作

### 性能
- [ ] 响应时间在预期范围内
- [ ] 并发请求处理正常
- [ ] 无内存泄漏

### 错误处理
- [ ] 无效请求返回400错误
- [ ] AI服务失败返回500错误
- [ ] 数据库错误有适当提示
- [ ] 日志记录完整

---

## 测试报告模板

```markdown
# Phase 3 测试报告

**测试日期**: 2025-10-29
**测试人员**: [姓名]
**测试环境**: 本地开发环境

## 测试结果汇总

| 测试场景 | 状态 | 耗时 | 备注 |
|---------|------|------|------|
| 快速测试生成 | ✅ | 1.2s | - |
| 带记忆的生成 | ✅ | 3.5s | 检索到5条记忆 |
| 一致性验证 | ✅ | 4.2s | 检测到1个违规项 |
| 完整流程测试 | ✅ | 4.8s | 所有功能正常 |

## 发现的问题

1. [问题描述]
   - 严重程度: [高/中/低]
   - 复现步骤: ...
   - 解决方案: ...

## 性能数据

- 平均响应时间: 3.5s
- 最慢响应: 8.2s (需要2次重试)
- 最快响应: 1.2s (快速测试)

## 建议

- [改进建议]

```

---

**更新日期**: 2025-10-29
**版本**: 1.0.0
