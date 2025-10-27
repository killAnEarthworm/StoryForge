package com.linyuan.storyforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linyuan.storyforge.config.PromptConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 直接调用百度千帆 API 的服务
 * 使用原生 HTTP 客户端，不经过 Spring AI 的 OpenAI 兼容层
 *
 * 功能特性：
 * - 自动重试机制（可配置）
 * - 超时控制
 * - 详细的错误处理和日志
 * - 支持模板化生成
 * - Token 使用统计
 *
 * @author StoryForge Team
 */
@Slf4j
@Service
public class QianfanDirectService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PromptConfiguration promptConfig;

    @Value("${ai.qianfan.api-key}")
    private String apiKey;

    @Value("${ai.openai.model:deepseek-v3.1-250821}")
    private String defaultModel;

    @Value("${ai.openai.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${ai.openai.max-tokens:2000}")
    private Integer defaultMaxTokens;

    @Value("${ai.qianfan.retry-count:3}")
    private Integer retryCount;

    @Value("${ai.qianfan.timeout-seconds:60}")
    private Integer timeoutSeconds;

    // 百度千帆 V2 API 完整 URL
    private static final String QIANFAN_API_URL = "https://qianfan.baidubce.com/v2/chat/completions";

    public QianfanDirectService(PromptConfiguration promptConfig) {
        this.promptConfig = promptConfig;
        this.objectMapper = new ObjectMapper();

        // 配置 RestTemplate 超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 连接超时 10 秒
        factory.setReadTimeout(60000);    // 读取超时 60 秒（默认）

        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 使用模板生成内容（带重试）
     *
     * @param templateKey 模板键名
     * @param variables 模板变量
     * @return AI 生成的内容
     */
    public String generateWithTemplate(String templateKey, Map<String, Object> variables) {
        log.info("使用模板 '{}' 生成内容", templateKey);

        // 获取模板
        String template = promptConfig.getTemplate(templateKey);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateKey);
        }

        // 替换模板变量
        String prompt = replaceVariables(template, variables);
        log.debug("生成的 prompt 长度: {} 字符", prompt.length());

        // 获取模板配置
        PromptConfiguration.PromptSettings settings =
                promptConfig.getSettingsOrDefault(templateKey);

        // 使用模板配置调用 API
        return chatWithOptions(
            prompt,
            null, // systemMessage
            settings.getTemperature(),
            settings.getMaxTokens(),
            settings.getRetryCount()
        );
    }

    /**
     * 简单对话（使用默认配置）
     */
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 对话（带系统提示）
     */
    public String chat(String userMessage, String systemMessage) {
        return chatWithOptions(userMessage, systemMessage, defaultTemperature, defaultMaxTokens, retryCount);
    }

    /**
     * 自定义参数对话
     */
    public String chatWithOptions(String userMessage, Double temperature, Integer maxTokens) {
        return chatWithOptions(userMessage, null, temperature, maxTokens, retryCount);
    }

    /**
     * 完整参数对话（带重试）
     *
     * @param userMessage 用户消息
     * @param systemMessage 系统提示（可选）
     * @param temperature 温度参数
     * @param maxTokens 最大 token 数
     * @param maxRetries 最大重试次数
     * @return AI 响应内容
     */
    public String chatWithOptions(String userMessage, String systemMessage,
                                   Double temperature, Integer maxTokens, Integer maxRetries) {

        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            attempt++;

            try {
                log.debug("尝试调用 API (第 {}/{} 次)", attempt, maxRetries);

                String response = callQianfanApi(userMessage, systemMessage, temperature, maxTokens);

                if (attempt > 1) {
                    log.info("✅ API 调用成功（重试 {} 次后成功）", attempt - 1);
                }

                return response;

            } catch (HttpClientErrorException e) {
                // 4xx 错误通常不需要重试
                log.error("❌ 客户端错误 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new RuntimeException("API 调用失败: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);

            } catch (HttpServerErrorException e) {
                // 5xx 错误可以重试
                lastException = e;
                log.warn("⚠️ 服务器错误 ({}), 尝试 {}/{}. 错误: {}",
                    e.getStatusCode(), attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    sleepBeforeRetry(attempt);
                }

            } catch (ResourceAccessException e) {
                // 网络错误可以重试
                lastException = e;
                log.warn("⚠️ 网络错误, 尝试 {}/{}. 错误: {}",
                    attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    sleepBeforeRetry(attempt);
                }

            } catch (Exception e) {
                // 其他错误
                lastException = e;
                log.error("❌ 未知错误, 尝试 {}/{}. 错误: {}",
                    attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    sleepBeforeRetry(attempt);
                } else {
                    throw new RuntimeException("API 调用失败（已重试 " + maxRetries + " 次）: " + e.getMessage(), e);
                }
            }
        }

        // 所有重试都失败
        String errorMsg = "API 调用失败（已重试 " + maxRetries + " 次）";
        if (lastException != null) {
            errorMsg += ": " + lastException.getMessage();
            throw new RuntimeException(errorMsg, lastException);
        }
        throw new RuntimeException(errorMsg);
    }

    /**
     * 实际调用百度千帆 API
     */
    private String callQianfanApi(String userMessage, String systemMessage,
                                   Double temperature, Integer maxTokens) throws Exception {

        log.debug("调用百度千帆 API");
        log.debug("- URL: {}", QIANFAN_API_URL);
        log.debug("- Model: {}", defaultModel);
        log.debug("- Temperature: {}, MaxTokens: {}", temperature, maxTokens);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", defaultModel);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        // 构建 messages 数组
        ArrayNode messages = objectMapper.createArrayNode();

        // 添加系统消息（如果有）
        if (systemMessage != null && !systemMessage.trim().isEmpty()) {
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
        }

        // 添加用户消息
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestBody.set("messages", messages);

        // 序列化请求
        String requestJson = objectMapper.writeValueAsString(requestBody);

        if (log.isTraceEnabled()) {
            log.trace("请求体:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody));
        }

        // 发送请求
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(
                QIANFAN_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );
        long endTime = System.currentTimeMillis();

        // 解析响应
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isEmpty()) {
            throw new RuntimeException("API 返回空响应");
        }

        JsonNode responseJson = objectMapper.readTree(responseBody);

        if (log.isTraceEnabled()) {
            log.trace("响应体:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseJson));
        }

        // 记录 token 使用情况
        if (responseJson.has("usage")) {
            JsonNode usage = responseJson.get("usage");
            log.info("Token 使用: 输入={}, 输出={}, 总计={}, 耗时={}ms",
                    usage.path("prompt_tokens").asInt(),
                    usage.path("completion_tokens").asInt(),
                    usage.path("total_tokens").asInt(),
                    (endTime - startTime));
        } else {
            log.info("✅ API 调用成功，耗时: {}ms", (endTime - startTime));
        }

        // 提取 AI 生成的内容
        if (responseJson.has("choices") && responseJson.get("choices").isArray()) {
            JsonNode firstChoice = responseJson.get("choices").get(0);
            if (firstChoice.has("message")) {
                JsonNode message = firstChoice.get("message");
                if (message.has("content")) {
                    String content = message.get("content").asText();
                    log.debug("成功获取 AI 响应，长度: {} 字符", content.length());
                    return content;
                }
            }
        }

        // 如果无法提取内容，抛出异常
        throw new RuntimeException("无法从 API 响应中提取内容: " + responseBody);
    }

    /**
     * 重试前的等待（指数退避）
     */
    private void sleepBeforeRetry(int attempt) {
        try {
            // 指数退避: 1s, 2s, 4s, 8s...
            long sleepTime = (long) Math.pow(2, attempt - 1) * 1000;
            sleepTime = Math.min(sleepTime, 10000); // 最多等待 10 秒

            log.debug("等待 {}ms 后重试...", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("等待重试时被中断");
        }
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        String result = template;

        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }

        // 记录未替换的占位符
        if (result.contains("{") && result.contains("}")) {
            log.warn("模板中存在未替换的占位符，请检查 variables 参数");
        }

        return result;
    }

    /**
     * 检查 API 服务是否可用
     */
    public boolean isAvailable() {
        try {
            log.debug("检查 API 服务可用性...");
            String response = chat("hi");
            boolean available = response != null && !response.isEmpty();

            if (available) {
                log.info("✅ API 服务可用");
            } else {
                log.warn("❌ API 服务响应为空");
            }

            return available;

        } catch (Exception e) {
            log.error("❌ API 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试 API 连接
     */
    public Map<String, Object> testConnection() {
        log.info("测试百度千帆 API 连接...");

        try {
            long startTime = System.currentTimeMillis();
            String response = chat("hi");
            long endTime = System.currentTimeMillis();

            return Map.of(
                "success", true,
                "message", "API 连接成功",
                "model", defaultModel,
                "apiUrl", QIANFAN_API_URL,
                "responseTime", (endTime - startTime) + "ms",
                "response", response
            );

        } catch (Exception e) {
            log.error("API 连接测试失败", e);

            return Map.of(
                "success", false,
                "message", "API 连接失败",
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            );
        }
    }
}
