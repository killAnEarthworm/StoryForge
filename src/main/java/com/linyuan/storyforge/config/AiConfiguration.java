package com.linyuan.storyforge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置类
 * 配置百度千帆大模型作为Spring AI的ChatModel实现
 *
 * 百度千帆V2 API完全兼容OpenAI标准
 * 鉴权方式：直接使用API Key，无需获取Access Token
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class AiConfiguration {

    @Value("${ai.qianfan.api-key:}")
    private String apiKey;

    @Value("${ai.qianfan.app-id:}")
    private String appId;

    @Value("${ai.openai.base-url:https://qianfan.baidubce.com/v2}")
    private String baseUrl;

    @Value("${ai.openai.model:ERNIE-3.5-8K}")
    private String model;

    @Value("${ai.openai.temperature:0.7}")
    private Double temperature;

    @Value("${ai.openai.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 创建OpenAI兼容的API客户端
     * 使用百度千帆的API Key直接认证
     *
     * @return OpenAiApi实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.qianfan", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAiApi openAiApi() {
        log.info("初始化百度千帆API客户端");
        log.info("- Base URL: {}", baseUrl);
        log.info("- Model: {}", model);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("百度千帆API Key未配置，AI功能将不可用");
            log.warn("请设置环境变量 QIANFAN_API_KEY");
            throw new IllegalStateException("百度千帆API Key未配置");
        }

        if (appId != null && !appId.isEmpty()) {
            log.info("- App ID: {}", appId);
        }

        // 直接使用API Key创建客户端
        // 百度千帆V2 API兼容OpenAI，API Key可以直接用作Bearer Token
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);

        log.info("✅ 百度千帆API客户端创建成功");
        return api;
    }

    /**
     * 创建ChatModel Bean
     * Spring AI会自动使用这个配置进行对话生成
     *
     * @param openAiApi OpenAI兼容的API客户端
     * @return OpenAiChatModel实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.qianfan", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        log.info("创建OpenAiChatModel Bean");
        log.info("- 模型: {}", model);
        log.info("- 温度: {}", temperature);
        log.info("- 最大Token数: {}", maxTokens);

        // 配置默认选项
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();

        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);

        log.info("✅ OpenAiChatModel创建成功，AI功能已就绪");
        return chatModel;
    }

    /**
     * 提供一个空的ChatModel作为降级方案
     * 当千帆API未配置时使用，避免应用启动失败
     */
    @Bean
    @ConditionalOnProperty(prefix = "ai.qianfan", name = "enabled", havingValue = "false")
    public OpenAiChatModel disabledChatModel() {
        log.warn("❌ 百度千帆API已禁用，AI功能将不可用");
        log.warn("请设置环境变量 QIANFAN_API_KEY 并将 ai.qianfan.enabled 设置为 true");

        // 返回一个不可用的配置
        OpenAiApi dummyApi = new OpenAiApi("", "");
        return new OpenAiChatModel(dummyApi);
    }
}
