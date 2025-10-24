package com.linyuan.storyforge;

import com.linyuan.storyforge.service.AiGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 百度千帆API集成测试
 *
 * 测试前请确保环境变量已配置:
 * - QIANFAN_API_KEY (格式: bce-v3/ALTAK-xxx/xxx)
 *
 * 注意：新版鉴权方式直接使用API Key，无需Secret Key和Access Token
 */
@Slf4j
@SpringBootTest
public class QianfanApiTest {

    @Autowired(required = false)
    private AiGenerationService aiService;

    /**
     * 测试AI服务基本连接
     */
    @Test
    public void testAiServiceConnection() {
        if (aiService == null) {
            log.warn("AiGenerationService未配置，跳过测试");
            return;
        }

        log.info("=== 测试百度千帆API连接 ===");

        try {
            String response = aiService.chat("你好，请用一句话介绍一下你自己");
            assertNotNull(response, "AI响应不应为null");
            assertTrue(response.length() > 0, "AI响应不应为空");

            log.info("AI响应: {}", response);
            log.info("✅ 百度千帆API连接测试成功");

        } catch (Exception e) {
            log.error("❌ 百度千帆API连接测试失败", e);
            fail("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 测试AI服务是否可用
     */
    @Test
    public void testAiServiceAvailability() {
        if (aiService == null) {
            log.warn("AiGenerationService未配置，跳过测试");
            return;
        }

        log.info("=== 测试AI服务可用性 ===");

        boolean available = aiService.isAvailable();
        log.info("AI服务可用性: {}", available);

        if (available) {
            log.info("✅ AI服务可用");
        } else {
            log.warn("⚠️ AI服务不可用，请检查配置");
        }
    }

    /**
     * 测试角色生成模板
     */
    @Test
    public void testCharacterGenerationTemplate() {
        if (aiService == null || !aiService.isAvailable()) {
            log.warn("AI服务不可用，跳过模板测试");
            return;
        }

        log.info("=== 测试角色生成模板 ===");

        try {
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("input", "一个勇敢的年轻剑客");
            variables.put("genre", "武侠");
            variables.put("worldview_context", "古代中国江湖");
            variables.put("existing_characters", "暂无");

            String response = aiService.generateWithTemplate("character-creation", variables);
            assertNotNull(response, "生成结果不应为null");
            assertTrue(response.length() > 100, "生成结果应该有足够长度");

            log.info("角色生成结果长度: {} 字符", response.length());
            log.info("角色生成结果预览:\n{}", response.substring(0, Math.min(500, response.length())));
            log.info("✅ 角色生成模板测试成功");

        } catch (Exception e) {
            log.error("❌ 角色生成模板测试失败", e);
            fail("角色生成失败: " + e.getMessage());
        }
    }

    /**
     * 测试不同温度参数
     */
    @Test
    public void testDifferentTemperatures() {
        if (aiService == null || !aiService.isAvailable()) {
            log.warn("AI服务不可用，跳过温度测试");
            return;
        }

        log.info("=== 测试不同温度参数 ===");

        String prompt = "用一句话描述春天";

        try {
            // 低温度（更确定性）
            String response1 = aiService.chatWithOptions(prompt, 0.3, 100);
            log.info("温度0.3响应: {}", response1);

            // 中等温度
            String response2 = aiService.chatWithOptions(prompt, 0.7, 100);
            log.info("温度0.7响应: {}", response2);

            // 高温度（更创意性）
            String response3 = aiService.chatWithOptions(prompt, 1.2, 100);
            log.info("温度1.2响应: {}", response3);

            log.info("✅ 温度参数测试成功");

        } catch (Exception e) {
            log.error("❌ 温度参数测试失败", e);
            fail("温度测试失败: " + e.getMessage());
        }
    }

    /**
     * 性能测试：测试响应时间
     */
    @Test
    public void testResponseTime() {
        if (aiService == null || !aiService.isAvailable()) {
            log.warn("AI服务不可用，跳过性能测试");
            return;
        }

        log.info("=== 测试API响应时间 ===");

        try {
            long startTime = System.currentTimeMillis();
            String response = aiService.chat("简单回答：1+1等于几？");
            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;
            log.info("响应时间: {}ms", duration);
            log.info("响应内容: {}", response);

            assertTrue(duration < 30000, "响应时间应该在30秒内");
            log.info("✅ 响应时间测试通过");

        } catch (Exception e) {
            log.error("❌ 响应时间测试失败", e);
            fail("性能测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试错误处理
     */
    @Test
    public void testErrorHandling() {
        if (aiService == null) {
            log.warn("AiGenerationService未配置，跳过错误处理测试");
            return;
        }

        log.info("=== 测试错误处理 ===");

        try {
            // 测试不存在的模板
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                aiService.generateWithTemplate("non-existent-template", new java.util.HashMap<>());
            });

            log.info("成功捕获异常: {}", exception.getMessage());
            assertTrue(exception.getMessage().contains("模板不存在"));
            log.info("✅ 错误处理测试通过");

        } catch (Exception e) {
            log.error("❌ 错误处理测试失败", e);
            fail("错误处理测试失败: " + e.getMessage());
        }
    }
}
