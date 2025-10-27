# 百度千帆大模型集成指南

本文档详细说明如何将StoryForge项目从OpenAI切换到百度千帆大模型平台。

## 📋 概述

**百度千帆V2 API完全兼容OpenAI标准**，因此我们可以继续使用Spring AI的OpenAI客户端，只需修改配置和认证方式即可。

### 优势
- ✅ 国内访问快速稳定，无需科学上网
- ✅ 文心大模型对中文理解更优
- ✅ 有免费额度，成本更低
- ✅ 代码改动最小（利用OpenAI兼容性）

---

## 🚀 快速开始

### 第一步：获取API Key

1. 访问百度智能云千帆平台：https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application
2. 注册/登录百度智能云账号
3. 创建应用，获取 **API Key** 和 **Secret Key**

### 第二步：配置环境变量

**Windows (Command Prompt)**:
```cmd
set QIANFAN_API_KEY=your-api-key-here
set QIANFAN_SECRET_KEY=your-secret-key-here
```

**Windows (PowerShell)**:
```powershell
$env:QIANFAN_API_KEY="your-api-key-here"
$env:QIANFAN_SECRET_KEY="your-secret-key-here"
```

**Unix/Linux/Mac**:
```bash
export QIANFAN_API_KEY=your-api-key-here
export QIANFAN_SECRET_KEY=your-secret-key-here
```

### 第三步：修改配置文件

需要手动修改以下文件：

#### 1. `src/main/resources/application.yml`

将AI配置部分改为：

```yaml
# AI 配置 (环境无关)
ai:
  # 百度千帆配置
  qianfan:
    enabled: true
    api-key: ${QIANFAN_API_KEY:}
    secret-key: ${QIANFAN_SECRET_KEY:}

  # OpenAI兼容配置（使用百度千帆V2 API）
  openai:
    base-url: https://qianfan.baidubce.com/v2
    model: ERNIE-3.5-8K
    temperature: 0.7
    max-tokens: 2000

  # Prompt 模板配置（保持不变）
  prompt:
    templates:
      character-creation: |
        你是一位专业的小说角色创作助手...
      # ... 其他模板保持不变
```

**支持的模型列表**:
- `ERNIE-4.0-8K` - 最强性能，适合复杂推理
- `ERNIE-3.5-8K` - 平衡性能和成本（推荐）
- `ERNIE-Speed-8K` - 快速响应
- `ERNIE-Lite-8K` - 低成本
- `ERNIE-Tiny-8K` - 最低成本，适合测试

#### 2. `CharacterService.java` - 添加AI生成方法

由于工具限制无法自动修改，请手动添加以下imports：

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

在类成员变量中添加：

```java
private final AiGenerationService aiGenerationService;
private final ObjectMapper objectMapper = new ObjectMapper();
```

在类中添加AI生成方法（完整代码见附录A）。

#### 3. `CharacterController.java` - 添加生成接口

在 `deleteCharacter` 方法之前添加：

```java
/**
 * Generate character using AI
 * POST /api/characters/generate?projectId=xxx&keywords=xxx
 */
@PostMapping("/generate")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<CharacterDTO> generateCharacter(
        @RequestParam UUID projectId,
        @RequestParam String keywords) {
    log.info("POST /api/characters/generate - Generating character with AI for project: {}, keywords: {}",
            projectId, keywords);

    try {
        CharacterDTO generatedCharacter = characterService.generateCharacterWithAI(projectId, keywords);
        return ApiResponse.success(generatedCharacter, "Character generated successfully using AI");
    } catch (RuntimeException e) {
        log.error("Failed to generate character with AI", e);
        return ApiResponse.error(500, "AI generation failed: " + e.getMessage());
    }
}
```

### 第四步：运行测试

```bash
# 运行所有测试
mvnw.cmd test

# 仅运行千帆API测试
mvnw.cmd test -Dtest=QianfanApiTest
```

### 第五步：测试API端点

```bash
# 创建项目
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"测试项目\",\"genre\":\"武侠\"}"

# 使用AI生成角色
curl -X POST "http://localhost:8080/api/characters/generate?projectId=<项目UUID>&keywords=一个勇敢的年轻剑客" \
  -H "Content-Type: application/json"
```

---

## 📁 已创建的新文件

以下文件已自动创建：

1. ✅ `src/main/java/com/linyuan/storyforge/config/QianfanTokenManager.java`
   - 管理Access Token的自动获取和刷新
   - Token有效期30天，提前1天自动刷新

2. ✅ `src/main/java/com/linyuan/storyforge/config/AiConfiguration.java`
   - 配置Spring AI使用百度千帆API
   - 创建OpenAiChatModel Bean

3. ✅ `src/main/java/com/linyuan/storyforge/service/AiGenerationService.java`
   - 封装AI调用逻辑
   - 提供模板化生成和直接对话两种方式

4. ✅ `src/test/java/com/linyuan/storyforge/QianfanApiTest.java`
   - 测试Token管理器
   - 测试API连接
   - 测试角色生成模板
   - 测试不同温度参数
   - 性能测试

---

## 🔧 需要手动修改的文件

由于工具限制，以下文件需要手动修改：

### 1. CharacterService.java

**文件位置**: `src/main/java/com/linyuan/storyforge/service/CharacterService.java`

**修改内容**:

1. 在imports中添加：
```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

2. 在类成员变量中添加：
```java
private final AiGenerationService aiGenerationService;
private final ObjectMapper objectMapper = new ObjectMapper();
```

3. 添加以下方法（完整代码见文件末尾的附录A）：
   - `generateCharacterWithAI()` - AI生成角色
   - `parseAIResponseToDTO()` - 解析AI响应
   - `getTextValue()`, `getIntValue()`, `getListValue()` - 辅助方法
   - `generateCharacterSummary()` - 生成角色概要

### 2. CharacterController.java

**文件位置**: `src/main/java/com/linyuan/storyforge/controller/CharacterController.java`

**修改内容**:

在 `deleteCharacter` 方法之前添加生成接口（代码见上文"第三步"）。

### 3. application.yml

**文件位置**: `src/main/resources/application.yml`

**修改内容**:

替换 `ai.openai` 部分的配置（完整配置见上文"第三步"）。

---

## 🧪 测试清单

### 本地测试步骤

1. **测试Token获取**
   ```bash
   mvnw.cmd test -Dtest=QianfanApiTest#testTokenManager
   ```
   预期结果：成功获取Access Token，显示剩余有效期

2. **测试AI连接**
   ```bash
   mvnw.cmd test -Dtest=QianfanApiTest#testAiServiceConnection
   ```
   预期结果：AI返回自我介绍

3. **测试角色生成模板**
   ```bash
   mvnw.cmd test -Dtest=QianfanApiTest#testCharacterGenerationTemplate
   ```
   预期结果：生成完整的角色JSON数据

4. **测试API端点**
   使用Postman或curl测试 `/api/characters/generate` 端点

---

## ❗ 常见问题

### Q1: 报错"无法获取百度千帆API访问令牌"

**原因**: API Key或Secret Key配置错误

**解决**:
1. 检查环境变量是否正确设置
2. 确认API Key和Secret Key没有多余的空格
3. 在百度千帆控制台重新生成Key

### Q2: 报错"AI服务不可用"

**原因**: Token管理器未能成功获取Token

**解决**:
1. 查看日志中的详细错误信息
2. 确认网络可以访问 `https://aip.baidubce.com`
3. 检查 `application.yml` 中 `ai.qianfan.enabled` 是否为 `true`

### Q3: AI生成的角色格式不正确

**原因**: AI返回的JSON格式与解析逻辑不匹配

**解决**:
1. 查看日志中AI的原始响应
2. 根据实际响应调整 `parseAIResponseToDTO` 方法
3. 修改prompt模板，明确指定输出格式

### Q4: Token频繁过期

**原因**: Token默认30天有效期，提前1天刷新

**解决**:
- Token管理器会自动刷新，无需手动处理
- 如需立即刷新，调用 `tokenManager.refreshAccessToken()`

### Q5: 响应速度慢

**原因**: 模型选择或网络问题

**解决**:
1. 使用 `ERNIE-Speed-8K` 模型获得更快响应
2. 减少 `max-tokens` 参数
3. 检查网络连接

---

## 📊 性能对比

| 模型 | 响应时间 | Token成本 | 中文能力 | 推荐场景 |
|------|---------|----------|---------|---------|
| ERNIE-4.0-8K | 3-5s | 高 | ⭐⭐⭐⭐⭐ | 复杂推理、长篇创作 |
| ERNIE-3.5-8K | 2-3s | 中 | ⭐⭐⭐⭐ | **通用场景（推荐）** |
| ERNIE-Speed-8K | 1-2s | 低 | ⭐⭐⭐ | 对话生成、快速响应 |
| ERNIE-Lite-8K | 1-2s | 极低 | ⭐⭐ | 简单任务 |

---

## 🔄 回滚到OpenAI

如需切换回OpenAI：

1. 修改 `application.yml`：
```yaml
ai:
  qianfan:
    enabled: false

  openai:
    api-key: ${OPENAI_API_KEY}
    base-url: https://api.openai.com
    model: gpt-4-turbo-preview
```

2. 设置环境变量：
```bash
set OPENAI_API_KEY=sk-...
```

3. 重启应用

---

## 📚 参考资料

- 百度千帆官方文档: https://cloud.baidu.com/doc/WENXINWORKSHOP/
- API列表: https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu
- Spring AI文档: https://docs.spring.io/spring-ai/reference/
- 百度千帆控制台: https://console.bce.baidu.com/qianfan/

---

## 📧 技术支持

如有问题，请：
1. 查看日志文件中的详细错误信息
2. 参考上述常见问题
3. 在项目issue中提问

---

# 附录A: CharacterService完整AI生成方法

由于篇幅限制，请查看项目根目录下的 `CharacterService_AI_Methods.txt` 文件，其中包含完整的AI生成相关方法代码。

或参考已创建的服务类：
- `QianfanTokenManager.java`
- `AiConfiguration.java`
- `AiGenerationService.java`

---

**祝你接入成功！🎉**
