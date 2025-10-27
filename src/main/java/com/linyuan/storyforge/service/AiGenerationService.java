package com.linyuan.storyforge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI生成服务（代理层）
 *
 * 此服务作为 QianfanDirectService 的代理，保持向后兼容性
 * 所有 AI 调用都通过 QianfanDirectService 直接调用百度千帆 API
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationService {

    private final QianfanDirectService qianfanService;

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
        log.debug("代理调用: generateWithTemplate(templateKey={})", templateKey);
        return qianfanService.generateWithTemplate(templateKey, variables);
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
        log.debug("代理调用: chat(message={}...)",
            userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
        return qianfanService.chat(userMessage);
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
        log.debug("代理调用: chatWithOptions(temperature={}, maxTokens={})", temperature, maxTokens);
        return qianfanService.chatWithOptions(userMessage, temperature, maxTokens);
    }

    /**
     * 检查AI服务是否可用
     *
     * @return true如果AI服务可用
     */
    public boolean isAvailable() {
        log.debug("代理调用: isAvailable()");
        return qianfanService.isAvailable();
    }
}
