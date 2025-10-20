package com.linyuan.storyforge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * AI提示词配置类
 * 管理所有提示词模板和生成参数配置
 *
 * 配置来源: application.yml 中的 ai.prompt 节点
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.prompt")
public class PromptConfiguration {

    /**
     * 提示词模板映射
     * key: 模板标识符 (如 "character-creation")
     * value: 完整的提示词模板内容
     */
    private Map<String, String> templates = new HashMap<>();

    /**
     * 每个模板对应的生成参数设置
     * key: 模板标识符
     * value: 该模板的参数配置
     */
    private Map<String, PromptSettings> settings = new HashMap<>();

    /**
     * 提示词生成参数配置
     * 控制AI生成行为的参数
     */
    @Data
    public static class PromptSettings {

        /**
         * 温度参数 (0.0-2.0)
         * 控制输出的随机性:
         * - 接近0: 确定性强,输出更可预测
         * - 接近2: 随机性强,输出更有创意
         * 默认: 0.7 (平衡创意和一致性)
         */
        private Double temperature = 0.7;

        /**
         * 最大生成token数量
         * 控制单次生成的最大长度
         * 默认: 2000 (约1500字中文)
         */
        private Integer maxTokens = 2000;

        /**
         * Top-P采样参数 (0.0-1.0)
         * 核采样:只考虑累积概率达到topP的token
         * 默认: 0.95
         */
        private Double topP = 0.95;

        /**
         * 频率惩罚 (-2.0 到 2.0)
         * 降低重复使用相同token的概率
         * 正值: 减少重复
         * 默认: 0.3
         */
        private Double frequencyPenalty = 0.3;

        /**
         * 存在惩罚 (-2.0 到 2.0)
         * 鼓励谈论新话题
         * 正值: 增加话题多样性
         * 默认: 0.3
         */
        private Double presencePenalty = 0.3;

        /**
         * 是否启用思维链 (Chain of Thought)
         * true: 在提示词中注入分步思考引导
         * 默认: false
         */
        private Boolean enableCoT = false;

        /**
         * 上下文最大token数
         * 控制传入AI的上下文长度限制
         * 默认: 3000
         */
        private Integer maxContextTokens = 3000;

        /**
         * 重试次数
         * 生成失败时的重试次数
         * 默认: 3
         */
        private Integer retryCount = 3;

        /**
         * 超时时间 (秒)
         * API调用的超时限制
         * 默认: 60秒
         */
        private Integer timeoutSeconds = 60;
    }

    /**
     * 获取指定模板的配置
     * 如果配置不存在,返回默认配置
     *
     * @param templateKey 模板标识符
     * @return 提示词设置
     */
    public PromptSettings getSettingsOrDefault(String templateKey) {
        return settings.getOrDefault(templateKey, new PromptSettings());
    }

    /**
     * 获取指定模板的内容
     *
     * @param templateKey 模板标识符
     * @return 模板内容,如果不存在返回null
     */
    public String getTemplate(String templateKey) {
        return templates.get(templateKey);
    }

    /**
     * 检查模板是否存在
     *
     * @param templateKey 模板标识符
     * @return true如果模板存在
     */
    public boolean hasTemplate(String templateKey) {
        return templates.containsKey(templateKey);
    }
}
