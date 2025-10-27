package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.WorldviewDTO;
import com.linyuan.storyforge.dto.WorldviewGenerationRequest;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.service.WorldviewGenerationService;
import com.linyuan.storyforge.validator.ConsistencyResult;
import com.linyuan.storyforge.validator.WorldviewConsistencyValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WorldviewGenerationController - 世界观AI生成控制器
 * 提供世界观生成和验证的REST API
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/worldviews")
@RequiredArgsConstructor
public class WorldviewGenerationController {

    private final WorldviewGenerationService generationService;
    private final WorldviewConsistencyValidator consistencyValidator;

    /**
     * 生成世界观
     * POST /api/worldviews/generate
     *
     * @param request 生成请求
     * @return 生成的世界观
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorldviewDTO> generateWorldview(
            @Valid @RequestBody WorldviewGenerationRequest request) {

        log.info("POST /api/worldviews/generate - 生成世界观, 项目: {}, 类型: {}",
                request.getProjectId(), request.getGenre());

        WorldviewDTO worldview = generationService.generateWorldview(request);

        return ApiResponse.success(worldview,
                String.format("成功生成世界观: %s", worldview.getName()));
    }

    /**
     * 批量生成多个世界观方案
     * POST /api/worldviews/generate/batch?count=3
     *
     * @param request 生成请求
     * @param count   生成数量（默认3个）
     * @return 生成的世界观列表
     */
    @PostMapping("/generate/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<WorldviewDTO>> generateMultipleWorldviews(
            @Valid @RequestBody WorldviewGenerationRequest request,
            @RequestParam(defaultValue = "3") int count) {

        log.info("POST /api/worldviews/generate/batch - 批量生成 {} 个世界观方案", count);

        // 限制数量
        if (count < 1 || count > 5) {
            return ApiResponse.error(400, "生成数量必须在1-5之间");
        }

        List<WorldviewDTO> worldviews = generationService.generateMultipleWorldviews(request, count);

        return ApiResponse.success(worldviews,
                String.format("成功生成 %d 个世界观方案", worldviews.size()));
    }

    /**
     * 验证内容是否符合世界观
     * POST /api/worldviews/{worldviewId}/validate
     *
     * @param worldviewId 世界观ID
     * @param request     验证请求
     * @return 验证结果
     */
    @PostMapping("/{worldviewId}/validate")
    public ApiResponse<ConsistencyResult> validateContent(
            @PathVariable UUID worldviewId,
            @RequestBody Map<String, Object> request) {

        log.info("POST /api/worldviews/{}/validate - 验证内容一致性", worldviewId);

        String content = (String) request.get("content");
        String contentTypeStr = (String) request.getOrDefault("contentType", "NARRATIVE");

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "内容不能为空");
        }

        ContentType contentType;
        try {
            contentType = ContentType.fromCode(contentTypeStr);
        } catch (IllegalArgumentException e) {
            contentType = ContentType.NARRATIVE;
        }

        ConsistencyResult result = consistencyValidator.validateContent(
                worldviewId,
                content,
                contentType
        );

        String message = result.getPassed()
                ? String.format("验证通过 - 得分: %.2f (%s)",
                result.getOverallScore(), result.getValidationLevel())
                : String.format("验证未通过 - 得分: %.2f,发现 %d 个问题",
                result.getOverallScore(), result.getViolations().size());

        return ApiResponse.success(result, message);
    }

    /**
     * 快速验证（只检查规则和约束）
     * GET /api/worldviews/{worldviewId}/quick-validate?content=xxx
     *
     * @param worldviewId 世界观ID
     * @param content     要验证的内容
     * @return 是否通过验证
     */
    @GetMapping("/{worldviewId}/quick-validate")
    public ApiResponse<Map<String, Object>> quickValidate(
            @PathVariable UUID worldviewId,
            @RequestParam String content) {

        log.info("GET /api/worldviews/{}/quick-validate - 快速验证", worldviewId);

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "内容不能为空");
        }

        boolean passed = consistencyValidator.quickValidate(worldviewId, content);

        Map<String, Object> result = Map.of(
                "worldviewId", worldviewId,
                "passed", passed,
                "message", passed ? "快速验证通过" : "快速验证未通过，请使用完整验证获取详情"
        );

        return ApiResponse.success(result, passed ? "验证通过" : "验证未通过");
    }

    /**
     * 获取所有支持的世界观类型
     * GET /api/worldviews/genres
     *
     * @return 类型列表
     */
    @GetMapping("/genres")
    public ApiResponse<List<Map<String, String>>> getAvailableGenres() {
        log.info("GET /api/worldviews/genres - 获取所有世界观类型");

        List<Map<String, String>> genres = java.util.Arrays.stream(
                        com.linyuan.storyforge.enums.WorldviewGenre.values())
                .map(genre -> Map.of(
                        "code", genre.getCode(),
                        "displayName", genre.getDisplayName(),
                        "features", genre.getFeatures()
                ))
                .toList();

        return ApiResponse.success(genres, "成功获取类型列表");
    }

    /**
     * 获取特定类型的生成要求
     * GET /api/worldviews/genres/{genreCode}/requirements
     *
     * @param genreCode 类型代码
     * @return 生成要求
     */
    @GetMapping("/genres/{genreCode}/requirements")
    public ApiResponse<Map<String, String>> getGenreRequirements(
            @PathVariable String genreCode) {

        log.info("GET /api/worldviews/genres/{}/requirements - 获取类型要求", genreCode);

        try {
            com.linyuan.storyforge.enums.WorldviewGenre genre =
                    com.linyuan.storyforge.enums.WorldviewGenre.fromCode(genreCode);

            Map<String, String> result = Map.of(
                    "code", genre.getCode(),
                    "displayName", genre.getDisplayName(),
                    "features", genre.getFeatures(),
                    "requirements", genre.getGenerationRequirements()
            );

            return ApiResponse.success(result, "成功获取类型要求");

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, "未知的类型代码: " + genreCode);
        }
    }
}
