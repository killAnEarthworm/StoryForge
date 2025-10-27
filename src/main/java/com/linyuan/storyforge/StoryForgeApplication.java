package com.linyuan.storyforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;

/**
 * StoryForge 应用主入口
 *
 * 注意：排除了 Spring AI 的 OpenAI 自动配置，因为我们手动配置了百度千帆API
 * 排除了 PgVector 自动配置，向量存储功能暂未启用
 */
@SpringBootApplication(exclude = {
        OpenAiAutoConfiguration.class,
        org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration.class
})
public class StoryForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoryForgeApplication.class, args);
    }

}
