package com.linyuan.storyforge.service;

import com.linyuan.storyforge.config.PromptConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI生成服务
 * 封装与百度千帆大模型的交互逻辑
 *
 * 提供两种调用方式：
 * 1. 基于模板的生成：使用预定义的prompt模板
 * 2. 直接对话：直接发送消息给AI
 *
 * 百度千帆鉴权方式：
 * - 直接使用API Key作为Bearer Token
 * - 无需额外的Access Token获取流程
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationService {

    private final OpenAiChatModel chatModel;
    private final PromptConfiguration promptConfig;

    /**
     * 根据模板生成内容
     *
     * @param templateKey 模板键名（如 "character-creation"）
     * @param variables 模板变量，用于替换模板中的占位符
     * @return AI生成的内容
     * @throws IllegalArgumentException 如果模板不存在
     * @throws RuntimeException 如果AI调用失败
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
        log.debug("生成的prompt长度: {} 字符", prompt.length());

        // 获取模板配置
        PromptConfiguration.PromptSettings settings =
                promptConfig.getSettingsOrDefault(templateKey);

        // 调用AI
        return callAI(prompt, settings);
    }

    /**
     * 直接调用AI进行对话
     * 使用默认配置
     *
     * @param userMessage 用户消息
     * @return AI响应
     * @throws RuntimeException 如果AI调用失败
     */
    public String chat(String userMessage) {
        log.debug("发送消息到AI: {}", userMessage);

        try {
            ChatResponse response = chatModel.call(
                    new Prompt(new UserMessage(userMessage))
            );

            String result = response.getResult().getOutput().getContent();
            log.debug("AI响应长度: {} 字符", result.length());
            return result;

        } catch (Exception e) {
            log.error("调用百度千帆API失败", e);
            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用自定义配置调用AI
     *
     * @param userMessage 用户消息
     * @param temperature 温度参数（0.0-2.0），控制创意性
     * @param maxTokens 最大token数
     * @return AI响应
     */
    public String chatWithOptions(String userMessage, Double temperature, Integer maxTokens) {
        log.debug("发送消息到AI (temperature={}, maxTokens={}): {}",
                temperature, maxTokens, userMessage);

        try {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withTemperature(temperature)
                    .withMaxTokens(maxTokens)
                    .build();

            ChatResponse response = chatModel.call(
                    new Prompt(new UserMessage(userMessage), options)
            );

            String result = response.getResult().getOutput().getContent();
            log.debug("AI响应长度: {} 字符", result.length());
            return result;

        } catch (Exception e) {
            log.error("调用百度千帆API失败", e);
            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用完整配置调用AI
     *
     * @param prompt 提示词
     * @param settings 生成参数配置
     * @return AI响应
     */
    private String callAI(String prompt, PromptConfiguration.PromptSettings settings) {
        log.debug("调用AI，temperature={}, maxTokens={}",
                settings.getTemperature(), settings.getMaxTokens());

        try {
            // 构建选项
            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .withTemperature(settings.getTemperature())
                    .withMaxTokens(settings.getMaxTokens())
                    .withTopP(settings.getTopP());

            // 百度千帆支持的参数
            if (settings.getFrequencyPenalty() != null) {
                optionsBuilder.withFrequencyPenalty(settings.getFrequencyPenalty());
            }
            if (settings.getPresencePenalty() != null) {
                optionsBuilder.withPresencePenalty(settings.getPresencePenalty());
            }

            OpenAiChatOptions options = optionsBuilder.build();

            // 调用AI
            long startTime = System.currentTimeMillis();
            ChatResponse response = chatModel.call(
                    new Prompt(new UserMessage(prompt), options)
            );
            long endTime = System.currentTimeMillis();

            String result = response.getResult().getOutput().getContent();

            log.info("AI生成成功，耗时: {}ms, 输出长度: {} 字符",
                    (endTime - startTime), result.length());

            // 记录token使用情况
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                var usage = response.getMetadata().getUsage();
                log.debug("Token使用: 输入={}, 输出={}, 总计={}",
                        usage.getPromptTokens(),
                        usage.getGenerationTokens(),
                        usage.getTotalTokens());
            }

            return result;

        } catch (Exception e) {
            log.error("调用百度千帆API失败", e);
            log.error("失败的prompt前100字符: {}", prompt.substring(0, Math.min(100, prompt.length())));
            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 替换模板中的变量
     * 简单的字符串替换，格式: {variableName}
     *
     * @param template 模板字符串
     * @param variables 变量映射
     * @return 替换后的字符串
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
            log.warn("模板中存在未替换的占位符，请检查variables参数");
        }

        return result;
    }

    /**
     * 检查AI服务是否可用
     * 简单检查，尝试调用AI服务
     *
     * @return true如果AI服务可用
     */
    public boolean isAvailable() {
        try {
            // 尝试一个简单的调用
            String response = chat("hi");
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.warn("AI服务不可用: {}", e.getMessage());
            return false;
        }
    }
}
