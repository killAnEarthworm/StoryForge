# 百度千帆 V2 API 集成指南

本文档说明如何使用百度千帆 V2 API（兼容 OpenAI 格式）进行 AI 调用。

## 📋 概述

**百度千帆 V2 API 完全兼容 OpenAI 标准**，使用简单的 API Key 鉴权方式，无需 Secret Key 或 Access Token。

### 优势
- ✅ 国内访问快速稳定，无需科学上网
- ✅ 文心大模型对中文理解更优
- ✅ 有免费额度，成本更低
- ✅ 简单的 API Key 鉴权，无需 Token 管理
- ✅ 完全兼容 OpenAI API 格式

---

## 🔑 API 认证方式

### 获取 API Key

1. 访问百度智能云千帆平台：https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application
2. 注册/登录百度智能云账号
3. 创建应用，获取 **API Key**
4. API Key 格式：`bce-v3/ALTAK-xxxxx/xxxxxx`

### ⚠️ 重要说明

**V2 API 只需要 API Key，不需要 Secret Key！**

- ✅ 使用：`Authorization: Bearer {API_KEY}`
- ❌ 不需要：Secret Key
- ❌ 不需要：获取 Access Token
- ❌ 不需要：Token 管理和刷新

---

## 🚀 快速开始

### 第一步：设置环境变量

**Windows (Command Prompt)**:
```cmd
set QIANFAN_API_KEY=bce-v3/ALTAK-xxxxx/xxxxxx
```

**Windows (PowerShell)**:
```powershell
$env:QIANFAN_API_KEY="bce-v3/ALTAK-xxxxx/xxxxxx"
```

**Unix/Linux/Mac**:
```bash
export QIANFAN_API_KEY=bce-v3/ALTAK-xxxxx/xxxxxx
```

### 第二步：配置文件已就绪

项目中的 `application.yml` 已经配置好：

```yaml
ai:
  # 百度千帆配置
  # V2 API 只需要 API Key，无需 secret-key
  # 鉴权方式：Authorization: Bearer {API_KEY}
  qianfan:
    enabled: true
    api-key: ${QIANFAN_API_KEY:}

  # OpenAI兼容配置（使用百度千帆V2 API）
  openai:
    base-url: https://qianfan.baidubce.com/v2
    model: ERNIE-3.5-8K
    temperature: 0.7
    max-tokens: 2000
```

### 第三步：运行应用

```bash
# 启动后端
mvnw.cmd spring-boot:run

# 启动前端
cd front
npm run dev
```

---

## 📡 API 调用方式

### HTTP 请求格式

```http
POST https://qianfan.baidubce.com/v2/chat/completions
Authorization: Bearer bce-v3/ALTAK-xxxxx/xxxxxx
Content-Type: application/json

{
  "model": "ERNIE-3.5-8K",
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

### 支持的模型

| 模型 | 说明 | 推荐场景 |
|------|------|----------|
| `ERNIE-4.0-8K` | 最强性能 | 复杂推理、长篇创作 |
| `ERNIE-3.5-8K` | 平衡性能和成本 | **通用场景（推荐）** |
| `ERNIE-Speed-8K` | 快速响应 | 对话生成、快速响应 |
| `ERNIE-Lite-8K` | 低成本 | 简单任务 |

更多模型请查看：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu

### 支持的参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `model` | string | 模型 ID（必填） |
| `messages` | array | 对话消息列表（必填） |
| `temperature` | number | 0.0-2.0，控制创意性 |
| `max_tokens` | integer | 最大输出 token 数 |
| `top_p` | number | 核采样参数 |
| `frequency_penalty` | number | 频率惩罚 |
| `presence_penalty` | number | 存在惩罚 |
| `stream` | boolean | 是否流式返回 |

---

## 💻 代码示例

### Java (Spring AI)

项目已经集成，直接使用：

```java
@Autowired
private AiGenerationService aiGenerationService;

// 直接对话
String response = aiGenerationService.chat("你好，请介绍一下你自己");

// 使用模板生成
Map<String, Object> variables = Map.of(
    "input", "一个勇敢的年轻剑客"
);
String character = aiGenerationService.generateWithTemplate(
    "character-creation",
    variables
);

// 自定义参数
String response = aiGenerationService.chatWithOptions(
    "生成一个故事大纲",
    0.8,  // temperature
    1500  // maxTokens
);
```

### cURL 测试

```bash
curl -X POST https://qianfan.baidubce.com/v2/chat/completions \
  -H "Authorization: Bearer bce-v3/ALTAK-xxxxx/xxxxxx" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "ERNIE-3.5-8K",
    "messages": [
      {"role": "user", "content": "你好"}
    ]
  }'
```

---

## 🧪 测试 AI 功能

### 1. 测试 AI 服务连接

```bash
# 运行测试（如果有）
mvnw.cmd test -Dtest=AiGenerationServiceTest

# 或直接启动应用测试
mvnw.cmd spring-boot:run
```

### 2. 通过 API 测试

```bash
# 测试简单对话（需要先创建项目）
curl -X POST http://localhost:8080/api/test/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好"}'
```

---

## ❗ 常见问题

### Q1: 报错"百度千帆 API Key 未配置"

**解决方案**:
1. 确认环境变量 `QIANFAN_API_KEY` 已设置
2. 检查 API Key 格式是否正确（`bce-v3/ALTAK-xxxxx/xxxxxx`）
3. 重启应用

### Q2: 报错"401 Unauthorized"

**原因**: API Key 无效或过期

**解决方案**:
1. 在百度千帆控制台检查 API Key 是否有效
2. 重新生成 API Key
3. 检查是否有多余的空格或换行符

### Q3: 响应速度慢

**解决方案**:
1. 使用 `ERNIE-Speed-8K` 模型
2. 减少 `max-tokens` 参数
3. 检查网络连接

### Q4: 返回内容被截断

**解决方案**:
1. 增加 `max-tokens` 参数
2. 调整 prompt 模板，要求更简洁的输出

---

## 📚 参考资料

- 千帆 V2 API 文档: https://cloud.baidu.com/doc/WENXINWORKSHOP/s/flfmc9do2
- 模型列表: https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu
- 百度千帆控制台: https://console.bce.baidu.com/qianfan/
- Spring AI 文档: https://docs.spring.io/spring-ai/reference/

---

## 🔄 与 V1 API 的区别

| 特性 | V1 API | V2 API (当前使用) |
|------|--------|-------------------|
| 鉴权方式 | API Key + Secret Key → Access Token | 直接使用 API Key |
| Token 管理 | 需要定期刷新 | 无需管理 |
| API 格式 | 百度专有格式 | OpenAI 兼容格式 |
| 复杂度 | 高（需要 Token 管理器） | 低（直接认证） |
| 推荐程度 | ❌ 已过时 | ✅ 推荐使用 |

---

**配置完成！开始使用 AI 功能吧！🎉**
