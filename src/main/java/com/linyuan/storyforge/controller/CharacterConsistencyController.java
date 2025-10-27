package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.validator.CharacterConsistencyValidator;
import com.linyuan.storyforge.validator.ConsistencyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CharacterConsistencyController - 角色一致性验证
 * 提供角色设定与生成内容的一致性检查
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/character-consistency")
@RequiredArgsConstructor
public class CharacterConsistencyController {

    private final CharacterConsistencyValidator consistencyValidator;

    /**
     * 验证内容是否符合角色设定
     * POST /api/character-consistency/validate
     * Body: {
     *   "characterId": "uuid",
     *   "generatedContent": "...",
     *   "contentType": "DIALOGUE"
     * }
     */
    @PostMapping("/validate")
    public ApiResponse<ConsistencyResult> validateContent(
            @RequestBody Map<String, Object> request) {

        UUID characterId = UUID.fromString((String) request.get("characterId"));
        String generatedContent = (String) request.get("generatedContent");
        String contentTypeStr = (String) request.get("contentType");
        ContentType contentType = ContentType.fromCode(contentTypeStr);

        log.info("POST /api/character-consistency/validate - 验证角色 {} 的内容一致性, 类型: {}",
                characterId, contentType);

        ConsistencyResult result = consistencyValidator.validateContent(
                characterId,
                generatedContent,
                contentType
        );

        String message = result.getPassed()
                ? String.format("验证通过 - 得分: %.2f (%s)", result.getOverallScore(), result.getValidationLevel())
                : String.format("验证未通过 - 得分: %.2f,发现 %d 个问题",
                        result.getOverallScore(), result.getViolations().size());

        return ApiResponse.success(result, message);
    }

    /**
     * 快速验证对话内容
     * POST /api/character-consistency/validate-dialogue/{characterId}
     */
    @PostMapping("/validate-dialogue/{characterId}")
    public ApiResponse<ConsistencyResult> validateDialogue(
            @PathVariable UUID characterId,
            @RequestBody Map<String, String> request) {

        String dialogue = request.get("dialogue");

        log.info("POST /api/character-consistency/validate-dialogue/{} - 快速验证对话",
                characterId);

        ConsistencyResult result = consistencyValidator.validateContent(
                characterId,
                dialogue,
                ContentType.DIALOGUE
        );

        return ApiResponse.success(result, "对话验证完成");
    }

    /**
     * 快速验证叙述内容
     * POST /api/character-consistency/validate-narrative/{characterId}
     */
    @PostMapping("/validate-narrative/{characterId}")
    public ApiResponse<ConsistencyResult> validateNarrative(
            @PathVariable UUID characterId,
            @RequestBody Map<String, String> request) {

        String narrative = request.get("narrative");

        log.info("POST /api/character-consistency/validate-narrative/{} - 快速验证叙述",
                characterId);

        ConsistencyResult result = consistencyValidator.validateContent(
                characterId,
                narrative,
                ContentType.NARRATIVE
        );

        return ApiResponse.success(result, "叙述验证完成");
    }

    /**
     * 批量验证多段内容
     * POST /api/character-consistency/batch-validate/{characterId}
     * Body: {
     *   "contents": [
     *     { "content": "...", "type": "DIALOGUE" },
     *     { "content": "...", "type": "NARRATIVE" }
     *   ]
     * }
     */
    @PostMapping("/batch-validate/{characterId}")
    public ApiResponse<Map<String, Object>> batchValidate(
            @PathVariable UUID characterId,
            @RequestBody Map<String, Object> request) {

        log.info("POST /api/character-consistency/batch-validate/{} - 批量验证内容",
                characterId);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> contents = (List<Map<String, String>>) request.get("contents");

        List<ConsistencyResult> results = new ArrayList<>();
        int passedCount = 0;

        for (Map<String, String> item : contents) {
            String content = item.get("content");
            String typeStr = item.get("type");
            ContentType type = ContentType.fromCode(typeStr);

            ConsistencyResult result = consistencyValidator.validateContent(
                    characterId,
                    content,
                    type
            );

            results.add(result);
            if (result.getPassed()) {
                passedCount++;
            }
        }

        Map<String, Object> response = Map.of(
                "totalCount", results.size(),
                "passedCount", passedCount,
                "failedCount", results.size() - passedCount,
                "results", results
        );

        return ApiResponse.success(response,
                String.format("批量验证完成: %d/%d 通过", passedCount, results.size()));
    }
}
