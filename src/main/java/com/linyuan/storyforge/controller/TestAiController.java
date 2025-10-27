package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.service.AiGenerationService;
import com.linyuan.storyforge.service.QianfanDirectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for AI service
 * 用于测试 AI 服务连接和配置
 */
@Slf4j
@RestController
@RequestMapping("/api/test/ai")
@RequiredArgsConstructor
public class TestAiController {

    private final AiGenerationService aiGenerationService;
    private final QianfanDirectService qianfanDirectService;

    /**
     * 测试简单对话
     * GET /api/test/ai/hello
     */
    @GetMapping("/hello")
    public ApiResponse<Map<String, Object>> testHello() {
        log.info("GET /api/test/ai/hello - Testing AI connection");

        try {
            long startTime = System.currentTimeMillis();
            String response = aiGenerationService.chat("你好，请用一句话介绍你自己");
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("responseTime", (endTime - startTime) + "ms");

            return ApiResponse.success(result, "AI service is working");

        } catch (Exception e) {
            log.error("AI service test failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());

            return ApiResponse.error("AI service failed: " + e.getMessage(), error);
        }
    }

    /**
     * 测试 AI 服务是否可用
     * GET /api/test/ai/status
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> checkStatus() {
        log.info("GET /api/test/ai/status - Checking AI service status");

        Map<String, Object> status = new HashMap<>();

        try {
            boolean available = aiGenerationService.isAvailable();
            status.put("available", available);
            status.put("message", available ? "AI service is available" : "AI service is not available");

            return available
                ? ApiResponse.success(status)
                : ApiResponse.error(503, "AI service not available", status);

        } catch (Exception e) {
            log.error("Failed to check AI service status", e);
            status.put("available", false);
            status.put("error", e.getMessage());
            return ApiResponse.error(500, "Failed to check status", status);
        }
    }

    /**
     * 测试自定义消息
     * POST /api/test/ai/chat
     * Body: { "message": "your message here" }
     */
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> testChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("POST /api/test/ai/chat - Message: {}", message);

        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error(400, "Message is required");
        }

        try {
            long startTime = System.currentTimeMillis();
            String response = aiGenerationService.chat(message);
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", message);
            result.put("response", response);
            result.put("responseTime", (endTime - startTime) + "ms");

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("AI chat test failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", message);
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace()[0].toString());

            return ApiResponse.error(500, "AI chat failed", error);
        }
    }

    /**
     * 使用直接调用服务测试（不经过 Spring AI）
     * GET /api/test/ai/direct-hello
     */
    @GetMapping("/direct-hello")
    public ApiResponse<Map<String, Object>> testDirectHello() {
        log.info("GET /api/test/ai/direct-hello - Testing direct API call");

        try {
            long startTime = System.currentTimeMillis();
            String response = qianfanDirectService.chat("你好，请用一句话介绍你自己");
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("responseTime", (endTime - startTime) + "ms");
            result.put("method", "Direct HTTP Call (not via Spring AI)");

            return ApiResponse.success(result, "Direct API call successful");

        } catch (Exception e) {
            log.error("Direct API call failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());

            return ApiResponse.error("Direct API call failed: " + e.getMessage(), error);
        }
    }

    /**
     * 测试直接连接
     * GET /api/test/ai/direct-test
     */
    @GetMapping("/direct-test")
    public ApiResponse<Map<String, Object>> testDirectConnection() {
        log.info("GET /api/test/ai/direct-test - Testing direct connection");

        try {
            Map<String, Object> result = qianfanDirectService.testConnection();
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Direct connection test failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());

            return ApiResponse.error("Connection test failed", error);
        }
    }

    /**
     * 使用直接调用服务的自定义测试
     * POST /api/test/ai/direct-chat
     * Body: { "message": "your message here" }
     */
    @PostMapping("/direct-chat")
    public ApiResponse<Map<String, Object>> testDirectChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("POST /api/test/ai/direct-chat - Message: {}", message);

        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error(400, "Message is required");
        }

        try {
            long startTime = System.currentTimeMillis();
            String response = qianfanDirectService.chat(message);
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", message);
            result.put("response", response);
            result.put("responseTime", (endTime - startTime) + "ms");
            result.put("method", "Direct HTTP Call");

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Direct chat failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", message);
            error.put("error", e.getMessage());

            return ApiResponse.error("Direct chat failed", error);
        }
    }
}
