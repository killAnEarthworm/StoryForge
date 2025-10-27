package com.linyuan.storyforge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置类
 *
 * 注意：不再使用 Spring AI 的 OpenAI 兼容层
 * 现在直接通过 QianfanDirectService 调用百度千帆 V2 API
 *
 * 原因：
 * - Spring AI OpenAI 客户端的 URL 路径拼接与百度千帆不兼容
 * - 直接调用提供更好的控制和调试能力
 * - 避免不必要的抽象层开销
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class AiConfiguration {

    public AiConfiguration() {
        log.info("=".repeat(80));
        log.info("AI 配置已加载");
        log.info("使用: 百度千帆 V2 API 直接调用（QianfanDirectService）");
        log.info("不使用: Spring AI OpenAI 兼容层");
        log.info("=".repeat(80));
    }

    // 所有 Spring AI OpenAI 相关的 Bean 配置已移除
    // AI 服务现在通过 QianfanDirectService 直接调用百度千帆 API
}
