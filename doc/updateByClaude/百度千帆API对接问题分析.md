# 百度千帆 API 对接问题分析和解决方案

## 问题现象

调用 `http://localhost:8080/api/test/ai/hello` 返回错误：
```json
{
  "success": false,
  "message": "AI service failed: AI生成失败: 404 - {\"message\":\"Resource not found.\",\"code\":\"ResourceNotFound\",\"requestId\":\"...\"}"
}
```

## 根本原因

### Spring AI 的 OpenAI 客户端与百度千帆 API 不兼容

1. **URL 路径拼接问题**
   - **Spring AI OpenAI 客户端**期望的路径：`/v1/chat/completions`
   - **百度千帆 V2 API** 的实际路径：`/v2/chat/completions`

   当配置 `base-url: https://qianfan.baidubce.com/v2` 时，Spring AI 可能会拼接成：
   ```
   https://qianfan.baidubce.com/v2/v1/chat/completions  ❌ 错误
   ```

   或者：
   ```
   https://qianfan.baidubce.com/v1/chat/completions  ❌ 路径错误
   ```

2. **API 格式差异**
   虽然百度千帆声称兼容 OpenAI 格式，但 Spring AI 的 OpenAI 客户端可能有一些特定的请求头或参数格式与百度千帆不完全兼容。

## 解决方案

### 方案 1: 使用原生 HTTP 客户端（推荐）✅

已创建 `QianfanDirectService.java`，直接使用 `RestTemplate` 调用百度千帆 API，完全绕过 Spring AI 的 OpenAI 兼容层。

**优势**：
- ✅ 完全控制请求格式和 URL
- ✅ 避免 Spring AI 的兼容性问题
- ✅ 更容易调试和排查问题
- ✅ 可以完整利用百度千帆 API 的所有功能

**实现**：
```java
@Service
public class QianfanDirectService {
    private static final String QIANFAN_API_URL =
        "https://qianfan.baidubce.com/v2/chat/completions";

    public String chat(String message) {
        // 直接使用 RestTemplate 调用
        // 完全控制请求格式
    }
}
```

### 方案 2: 修改 Spring AI 配置（不推荐）

尝试调整 `base-url` 配置，但可能仍有兼容性问题：

```yaml
# 尝试不同的配置
ai:
  openai:
    base-url: https://qianfan.baidubce.com  # 去掉 /v2
    # 或
    base-url: https://qianfan.baidubce.com/v2/chat  # 包含 /chat
```

❌ **不推荐原因**：
- Spring AI 的内部实现可能会有其他不兼容的地方
- 难以调试和排查问题
- 对百度千帆 API 的控制有限

## 新增的测试端点

### 1. 直接调用测试（推荐使用）

**快速测试**：
```bash
GET http://localhost:8080/api/test/ai/direct-hello
```

**自定义消息测试**：
```bash
POST http://localhost:8080/api/test/ai/direct-chat
Content-Type: application/json

{
  "message": "你好"
}
```

**连接测试**：
```bash
GET http://localhost:8080/api/test/ai/direct-test
```

### 2. Spring AI 测试端点（用于对比）

```bash
# 经过 Spring AI OpenAI 客户端（可能失败）
GET http://localhost:8080/api/test/ai/hello
POST http://localhost:8080/api/test/ai/chat
```

## 验证步骤

### 第一步：重启应用

确保新的服务已加载：

```cmd
# 如果有 Java 21
mvnw.cmd spring-boot:run

# 或者如果应用已在运行，Ctrl+C 停止后重新启动
```

### 第二步：测试直接调用

```bash
# 使用 curl 测试
curl http://localhost:8080/api/test/ai/direct-hello

# 或使用 PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/test/ai/direct-hello" -Method Get
```

**期望响应**：
```json
{
  "code": 200,
  "message": "Direct API call successful",
  "data": {
    "success": true,
    "response": "你好！我是一个AI助手...",
    "responseTime": "1234ms",
    "method": "Direct HTTP Call (not via Spring AI)"
  }
}
```

### 第三步：对比 Spring AI 调用

```bash
# 测试 Spring AI 方式（用于对比）
curl http://localhost:8080/api/test/ai/hello
```

如果这个仍然失败，证明问题确实在 Spring AI 的 OpenAI 兼容层。

## 后续建议

### 1. 替换所有 AI 调用服务

如果直接调用成功，建议：

1. **更新 `AiGenerationService.java`**
   - 替换为使用 `QianfanDirectService`
   - 或者重构为接口，提供两种实现

2. **更新 `CharacterService.java`**
   - 修改注入：`private final QianfanDirectService aiService;`
   - 调用直接服务而不是 Spring AI 服务

### 2. 模型选择

百度千帆 V2 API 支持多种模型，你可以在配置中选择：

```yaml
ai:
  openai:
    model: deepseek-v3.1-250821  # 当前使用
    # 或选择其他模型：
    # model: ERNIE-3.5-8K
    # model: ERNIE-4.0-8K
    # model: ERNIE-Speed-8K
```

**查看可用模型**：
https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu

### 3. 详细日志

在 `application-dev.yml` 中已启用 DEBUG 日志：
```yaml
logging:
  level:
    com.linyuan.storyforge: DEBUG
    org.springframework.ai: DEBUG
```

查看启动日志和调用日志，获取详细的请求信息。

## 技术细节

### 百度千帆 V2 API 正确调用格式

```http
POST https://qianfan.baidubce.com/v2/chat/completions
Authorization: Bearer bce-v3/ALTAK-xxxxx/xxxxxx
Content-Type: application/json

{
  "model": "deepseek-v3.1-250821",
  "messages": [
    {
      "role": "system",
      "content": "You are a helpful assistant."
    },
    {
      "role": "user",
      "content": "你好"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 2000
}
```

### Spring AI OpenAI 客户端的问题

Spring AI 的 `OpenAiApi` 类是为 OpenAI 官方 API 设计的：
- 默认路径：`/v1/chat/completions`
- 默认 base URL：`https://api.openai.com`

虽然可以覆盖 base URL，但路径拼接逻辑可能不适用于百度千帆。

## 常见问题

### Q1: 直接调用也失败，返回 401 Unauthorized

**检查**：
1. API Key 是否正确
2. API Key 格式：`bce-v3/ALTAK-xxxxx/xxxxxx`
3. Authorization header：`Bearer {API_KEY}`

### Q2: 直接调用也返回 404

**检查**：
1. 确认 API URL：`https://qianfan.baidubce.com/v2/chat/completions`
2. 确认模型名称在你的 API Key 权限范围内
3. 在百度千帆控制台查看可用模型列表

### Q3: 模型名称不对

**解决**：
1. 登录百度千帆控制台
2. 查看"在线推理" -> "服务详情"
3. 使用服务详情中显示的"API 名称"作为 model 参数

### Q4: 直接调用成功，但如何替换现有代码？

**方案 A - 最小改动**（推荐）：

修改 `CharacterService.java`:
```java
// 将
private final AiGenerationService aiGenerationService;

// 改为
private final QianfanDirectService aiGenerationService;

// 然后使用相同的方法调用
```

**方案 B - 完全重构**：
创建 `AiService` 接口，提供两种实现。

## 总结

1. ✅ 已创建 `QianfanDirectService` - 原生 HTTP 客户端
2. ✅ 已添加测试端点 `/api/test/ai/direct-*`
3. ⏳ 需要重启应用测试
4. ⏳ 根据测试结果决定是否替换现有服务

**下一步操作**：
1. 重启应用
2. 测试 `http://localhost:8080/api/test/ai/direct-hello`
3. 如果成功，替换现有的 `AiGenerationService` 使用
4. 重新测试角色生成功能

---

**创建时间**: 2025-10-27
**问题**: Spring AI OpenAI 客户端与百度千帆 V2 API 不兼容（404 错误）
**解决方案**: 使用原生 HTTP 客户端绕过 Spring AI 兼容层
