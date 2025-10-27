# AI API 调用问题修复总结

## 问题描述

调用 `/api/characters/generate` 接口时报错：
```
AI generation failed: AI服务不可用，请检查百度千帆API配置
```

但直接调用百度千帆 API 时成功：
- URL: `https://qianfan.baidubce.com/v2/chat/completions`
- 模型: `deepseek-v3.1-250821`
- API Key: `bce-v3/ALTAK-FaKaUC3SaFBIJ2YToxN5G/...`

## 根本原因

### 1. 模型名称不匹配 ✅ 已修复
- **配置文件使用**: `ERNIE-3.5-8K`
- **实际测试使用**: `deepseek-v3.1-250821`
- **修复**: 将 `application.yml` 中的模型改为 `deepseek-v3.1-250821`

### 2. 过于严格的可用性检查 ✅ 已修复
- **问题代码** (`CharacterService.java:157-160`):
  ```java
  if (!aiGenerationService.isAvailable()) {
      throw new RuntimeException("AI服务不可用，请检查百度千帆API配置");
  }
  ```
- **问题**: `isAvailable()` 方法会调用 `chat("hi")` 测试，如果失败会阻止所有 AI 生成请求
- **修复**: 移除这个预检查，让实际的 AI 调用自己处理错误

## 已修改的文件

### 1. `src/main/resources/application.yml`
```yaml
# 修改前
openai:
  model: ERNIE-3.5-8K

# 修改后
openai:
  model: deepseek-v3.1-250821
```

### 2. `src/main/java/com/linyuan/storyforge/service/CharacterService.java`
- **删除**: 第 157-160 行的 `isAvailable()` 检查
- **原因**: 避免预检查失败导致所有请求被阻止

### 3. 新增测试控制器
创建了 `TestAiController.java` 用于测试 AI 服务：

**测试端点**:
1. `GET /api/test/ai/hello` - 快速测试 AI 连接
2. `GET /api/test/ai/status` - 检查 AI 服务状态
3. `POST /api/test/ai/chat` - 自定义消息测试

## 验证步骤

### 1. 重新编译（如果可能）

⚠️ **注意**: 项目需要 Java 21，当前系统是 Java 8

如果有 Java 21：
```cmd
# 设置 JAVA_HOME
set JAVA_HOME=C:\path\to\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# 重新编译
mvnw.cmd clean compile

# 运行应用
mvnw.cmd spring-boot:run
```

### 2. 如果应用已经在运行

直接重启应用即可加载新配置。

### 3. 测试 AI 服务

#### 测试 1: 快速连接测试
```bash
curl http://localhost:8080/api/test/ai/hello
```

期望响应：
```json
{
  "code": 200,
  "message": "AI service is working",
  "data": {
    "success": true,
    "response": "你好！我是...",
    "responseTime": "1234ms"
  }
}
```

#### 测试 2: 生成角色（需要先创建项目）
```bash
# 1. 创建测试项目
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"测试项目","genre":"武侠"}'

# 2. 使用返回的 projectId 生成角色
curl -X POST "http://localhost:8080/api/characters/generate?projectId=<项目ID>&keywords=一个勇敢的年轻剑客" \
  -H "Content-Type: application/json"
```

## 配置说明

当前配置 (`application.yml`):
```yaml
ai:
  # 百度千帆配置
  qianfan:
    enabled: true
    api-key: ${QIANFAN_API_KEY:bce-v3/ALTAK-FaKaUC3SaFBIJ2YToxN5G/...}

  # OpenAI兼容配置
  openai:
    base-url: https://qianfan.baidubce.com/v2
    model: deepseek-v3.1-250821  # ✅ 已修改
    temperature: 0.7
    max-tokens: 2000
```

## 常见问题排查

### 如果还是报错，检查以下内容：

1. **检查应用日志**
   - 启动时是否有 "✅ 百度千帆API客户端创建成功" 日志
   - 是否有 "✅ OpenAiChatModel创建成功" 日志

2. **检查 API Key**
   - 确认 API Key 格式正确：`bce-v3/ALTAK-xxxxx/xxxxxx`
   - 确认 API Key 在百度千帆控制台是有效的

3. **检查网络连接**
   - 确保可以访问 `https://qianfan.baidubce.com`
   ```bash
   curl https://qianfan.baidubce.com/v2/chat/completions
   ```

4. **查看详细错误**
   - 应用日志级别设置为 DEBUG（已在 `application-dev.yml` 配置）
   - 查看完整的堆栈跟踪

5. **使用测试端点**
   ```bash
   # 测试 AI 服务状态
   curl http://localhost:8080/api/test/ai/status

   # 测试简单对话
   curl -X POST http://localhost:8080/api/test/ai/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"你好"}'
   ```

## 后续建议

### 1. 升级 Java 版本
项目配置为 Java 21，建议安装并使用 Java 21 以获得最佳兼容性。

下载地址: https://adoptium.net/temurin/releases/?version=21

### 2. 环境变量管理
建议使用环境变量而不是硬编码 API Key：
```cmd
set QIANFAN_API_KEY=your-real-api-key
```

然后修改 `application.yml`:
```yaml
api-key: ${QIANFAN_API_KEY:}  # 移除默认值
```

### 3. 错误处理改进
考虑在 `AiGenerationService` 中添加更详细的错误处理和重试逻辑。

## 联系支持

如果问题仍未解决：
1. 收集完整的应用日志（特别是启动日志和错误日志）
2. 检查是否有防火墙或代理拦截请求
3. 在百度千帆控制台查看 API 调用记录和配额使用情况

---

**修复完成时间**: 2025-10-27
**修复内容**: 模型名称修正 + 移除预检查
