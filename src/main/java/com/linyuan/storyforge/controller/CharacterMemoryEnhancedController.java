package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.entity.CharacterMemory;
import com.linyuan.storyforge.service.CharacterMemoryEnhancedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CharacterMemoryEnhancedController - 增强的角色记忆管理
 * 提供智能检索和高级记忆管理功能
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/character-memories/enhanced")
@RequiredArgsConstructor
public class CharacterMemoryEnhancedController {

    private final CharacterMemoryEnhancedService enhancedService;

    /**
     * 智能记忆检索
     * POST /api/character-memories/enhanced/retrieve
     * Body: { "characterId": "uuid", "sceneContext": "...", "currentEmotion": "...", "maxResults": 5 }
     */
    @PostMapping("/retrieve")
    public ApiResponse<List<CharacterMemory>> retrieveRelevantMemories(
            @RequestBody Map<String, Object> request) {

        UUID characterId = UUID.fromString((String) request.get("characterId"));
        String sceneContext = (String) request.get("sceneContext");
        String currentEmotion = (String) request.getOrDefault("currentEmotion", null);
        int maxResults = (int) request.getOrDefault("maxResults", 5);

        log.info("POST /api/character-memories/enhanced/retrieve - 智能检索记忆, 角色: {}", characterId);

        List<CharacterMemory> memories = enhancedService.retrieveRelevantMemories(
                characterId,
                sceneContext,
                currentEmotion,
                maxResults
        );

        return ApiResponse.success(memories,
                String.format("成功检索到 %d 条相关记忆", memories.size()));
    }

    /**
     * 更新角色所有记忆的可访问性
     * POST /api/character-memories/enhanced/update-accessibility/{characterId}
     */
    @PostMapping("/update-accessibility/{characterId}")
    public ApiResponse<Map<String, Object>> updateMemoriesAccessibility(
            @PathVariable UUID characterId) {

        log.info("POST /api/character-memories/enhanced/update-accessibility/{} - 更新记忆可访问性",
                characterId);

        int updatedCount = enhancedService.updateMemoriesAccessibility(characterId);

        Map<String, Object> result = Map.of(
                "characterId", characterId,
                "updatedCount", updatedCount
        );

        return ApiResponse.success(result,
                String.format("成功更新 %d 条记忆的可访问性", updatedCount));
    }

    /**
     * 获取角色最重要的记忆
     * GET /api/character-memories/enhanced/top-important/{characterId}?limit=10
     */
    @GetMapping("/top-important/{characterId}")
    public ApiResponse<List<CharacterMemory>> getTopImportantMemories(
            @PathVariable UUID characterId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/character-memories/enhanced/top-important/{} - 获取最重要的记忆, 数量: {}",
                characterId, limit);

        List<CharacterMemory> memories = enhancedService.getTopImportantMemories(characterId, limit);

        return ApiResponse.success(memories,
                String.format("成功获取 %d 条最重要的记忆", memories.size()));
    }

    /**
     * 获取与特定角色相关的记忆
     * GET /api/character-memories/enhanced/about-character/{characterId}/{relatedCharacterId}
     */
    @GetMapping("/about-character/{characterId}/{relatedCharacterId}")
    public ApiResponse<List<CharacterMemory>> getMemoriesAboutCharacter(
            @PathVariable UUID characterId,
            @PathVariable UUID relatedCharacterId) {

        log.info("GET /api/character-memories/enhanced/about-character/{}/{} - 获取关于特定角色的记忆",
                characterId, relatedCharacterId);

        List<CharacterMemory> memories = enhancedService.getMemoriesAboutCharacter(
                characterId,
                relatedCharacterId
        );

        return ApiResponse.success(memories,
                String.format("成功获取 %d 条相关记忆", memories.size()));
    }

    /**
     * 按关键词搜索记忆
     * GET /api/character-memories/enhanced/search/{characterId}?keyword=xxx
     */
    @GetMapping("/search/{characterId}")
    public ApiResponse<List<CharacterMemory>> findMemoriesByKeyword(
            @PathVariable UUID characterId,
            @RequestParam String keyword) {

        log.info("GET /api/character-memories/enhanced/search/{} - 搜索关键词: {}",
                characterId, keyword);

        List<CharacterMemory> memories = enhancedService.findMemoriesByKeyword(
                characterId,
                keyword
        );

        return ApiResponse.success(memories,
                String.format("找到 %d 条匹配的记忆", memories.size()));
    }

    /**
     * 获取记忆统计信息
     * GET /api/character-memories/enhanced/statistics/{characterId}
     */
    @GetMapping("/statistics/{characterId}")
    public ApiResponse<Map<String, Object>> getMemoryStatistics(
            @PathVariable UUID characterId) {

        log.info("GET /api/character-memories/enhanced/statistics/{} - 获取记忆统计信息",
                characterId);

        Map<String, Object> statistics = enhancedService.getMemoryStatistics(characterId);

        return ApiResponse.success(statistics, "成功获取记忆统计信息");
    }
}
