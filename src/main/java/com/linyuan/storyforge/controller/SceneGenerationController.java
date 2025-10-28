package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.SceneDTO;
import com.linyuan.storyforge.dto.SceneGenerationRequest;
import com.linyuan.storyforge.enums.SceneType;
import com.linyuan.storyforge.service.SceneGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * SceneGenerationController - 场景AI生成控制器
 * 提供场景生成相关的REST API
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneGenerationController {

    private final SceneGenerationService sceneGenerationService;

    /**
     * 生成单个场景
     * POST /api/scenes/generate
     *
     * @param request 场景生成请求
     * @return 生成的场景
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SceneDTO> generateScene(
            @Valid @RequestBody SceneGenerationRequest request) {

        log.info("POST /api/scenes/generate - 生成场景: {}", request.getSimpleDescription());

        // 验证请求
        String validationError = request.validate();
        if (validationError != null) {
            return ApiResponse.error(400, validationError);
        }

        try {
            SceneDTO scene = sceneGenerationService.generateScene(request);
            return ApiResponse.success(scene,
                    String.format("成功生成场景: %s", scene.getName()));
        } catch (IllegalArgumentException e) {
            log.error("场景生成请求参数错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("场景生成失败", e);
            return ApiResponse.error(500, "场景生成失败: " + e.getMessage());
        }
    }

    /**
     * 批量生成多个场景方案
     * POST /api/scenes/generate/batch?count=3
     *
     * @param request 场景生成请求
     * @param count   生成数量（默认3个，最多5个）
     * @return 生成的场景列表
     */
    @PostMapping("/generate/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<SceneDTO>> generateMultipleScenes(
            @Valid @RequestBody SceneGenerationRequest request,
            @RequestParam(defaultValue = "3") int count) {

        log.info("POST /api/scenes/generate/batch - 批量生成 {} 个场景方案", count);

        // 限制数量
        if (count < 1 || count > 5) {
            return ApiResponse.error(400, "生成数量必须在1-5之间");
        }

        // 验证请求
        String validationError = request.validate();
        if (validationError != null) {
            return ApiResponse.error(400, validationError);
        }

        try {
            List<SceneDTO> scenes = sceneGenerationService.generateMultipleScenes(request, count);
            return ApiResponse.success(scenes,
                    String.format("成功生成 %d 个场景方案", scenes.size()));
        } catch (Exception e) {
            log.error("批量场景生成失败", e);
            return ApiResponse.error(500, "批量场景生成失败: " + e.getMessage());
        }
    }

    /**
     * 扩展已有场景
     * POST /api/scenes/{sceneId}/expand
     *
     * @param sceneId 场景ID
     * @param request 扩展请求
     * @return 扩展后的场景
     */
    @PostMapping("/{sceneId}/expand")
    public ApiResponse<SceneDTO> expandScene(
            @PathVariable UUID sceneId,
            @RequestBody Map<String, Object> request) {

        log.info("POST /api/scenes/{}/expand - 扩展场景", sceneId);

        String expansionPoint = (String) request.get("expansionPoint");
        Integer additionalWords = (Integer) request.getOrDefault("additionalWords", 300);

        if (expansionPoint == null || expansionPoint.isBlank()) {
            return ApiResponse.error(400, "扩展点描述不能为空");
        }

        if (additionalWords < 50 || additionalWords > 1000) {
            return ApiResponse.error(400, "额外字数必须在50-1000之间");
        }

        try {
            SceneDTO expandedScene = sceneGenerationService.expandScene(
                    sceneId, expansionPoint, additionalWords);
            return ApiResponse.success(expandedScene, "场景扩展成功");
        } catch (Exception e) {
            log.error("场景扩展失败", e);
            return ApiResponse.error(500, "场景扩展失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有场景类型
     * GET /api/scenes/types
     *
     * @return 场景类型列表
     */
    @GetMapping("/types")
    public ApiResponse<List<Map<String, String>>> getSceneTypes() {
        log.info("GET /api/scenes/types - 获取场景类型列表");

        List<Map<String, String>> types = new ArrayList<>();
        for (SceneType type : SceneType.values()) {
            Map<String, String> typeInfo = new LinkedHashMap<>();
            typeInfo.put("code", type.getCode());
            typeInfo.put("displayName", type.getDisplayName());
            typeInfo.put("features", type.getFeatures());
            typeInfo.put("atmosphereKeywords", type.getAtmosphereKeywords());
            typeInfo.put("sensoryFocus", type.getSensoryFocus());
            types.add(typeInfo);
        }

        return ApiResponse.success(types, "成功获取场景类型列表");
    }

    /**
     * 获取特定场景类型的详细要求
     * GET /api/scenes/types/{code}/requirements
     *
     * @param code 场景类型代码
     * @return 场景类型详细信息
     */
    @GetMapping("/types/{code}/requirements")
    public ApiResponse<Map<String, String>> getSceneTypeRequirements(
            @PathVariable String code) {

        log.info("GET /api/scenes/types/{}/requirements - 获取场景类型要求", code);

        try {
            SceneType type = SceneType.fromCode(code);
            Map<String, String> details = new LinkedHashMap<>();
            details.put("code", type.getCode());
            details.put("displayName", type.getDisplayName());
            details.put("features", type.getFeatures());
            details.put("atmosphereKeywords", type.getAtmosphereKeywords());
            details.put("sensoryFocus", type.getSensoryFocus());
            details.put("requirements", type.getGenerationRequirements());
            details.put("sensoryGuidance", type.getSensoryGuidance());
            details.put("styleGuidance", type.getStyleGuidance());

            return ApiResponse.success(details, "成功获取场景类型要求");
        } catch (IllegalArgumentException e) {
            log.warn("未找到场景类型: {}", code);
            return ApiResponse.error(404, "未找到场景类型: " + code);
        }
    }

    /**
     * 获取场景生成建议
     * GET /api/scenes/suggestions?sceneType=action&mood=tense
     *
     * @param sceneType 场景类型代码（可选）
     * @param mood      情绪基调（可选）
     * @return 生成建议
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getGenerationSuggestions(
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String mood) {

        log.info("GET /api/scenes/suggestions - 获取生成建议, 类型: {}, 情绪: {}", sceneType, mood);

        Map<String, Object> suggestions = new HashMap<>();

        // 根据场景类型提供建议
        if (sceneType != null) {
            try {
                SceneType type = SceneType.fromCode(sceneType);
                suggestions.put("recommendedCreativity", getRecommendedCreativity(type));
                suggestions.put("recommendedWordCount", getRecommendedWordCount(type));
                suggestions.put("sensoryFocus", List.of(type.getSensoryFocus().split("、")));
                suggestions.put("styleGuidance", type.getStyleGuidance());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error(400, "无效的场景类型: " + sceneType);
            }
        }

        // 根据情绪提供建议
        if (mood != null) {
            suggestions.put("atmosphereSuggestions", getAtmosphereSuggestions(mood));
            suggestions.put("weatherSuggestions", getWeatherSuggestions(mood));
        }

        // 通用建议
        suggestions.put("generalTips", List.of(
                "动作场景建议创意度0.6-0.7，注重连贯性",
                "情感场景建议创意度0.7-0.8，需要细腻表达",
                "描写场景建议创意度0.8-0.9，发挥想象力",
                "过渡场景控制在200-400字",
                "高潮场景可以达到1000-1500字"
        ));

        return ApiResponse.success(suggestions, "成功获取生成建议");
    }

    /**
     * 预览场景生成提示词（调试用）
     * POST /api/scenes/preview-prompt
     *
     * @param request 场景生成请求
     * @return 提示词预览
     */
    @PostMapping("/preview-prompt")
    public ApiResponse<Map<String, Object>> previewPrompt(
            @Valid @RequestBody SceneGenerationRequest request) {

        log.info("POST /api/scenes/preview-prompt - 预览提示词");

        // 验证请求
        String validationError = request.validate();
        if (validationError != null) {
            return ApiResponse.error(400, validationError);
        }

        Map<String, Object> preview = new HashMap<>();
        preview.put("sceneDescription", request.getSimpleDescription());
        preview.put("sceneType", request.getSceneType().getDisplayName());
        preview.put("estimatedTokens", calculateEstimatedTokens(request));
        preview.put("estimatedTime", "20-40秒");
        preview.put("warning", getWarnings(request));

        return ApiResponse.success(preview, "提示词预览生成成功");
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取推荐的创意度
     */
    private double getRecommendedCreativity(SceneType type) {
        return switch (type) {
            case ACTION, CONFLICT -> 0.65;
            case DIALOGUE, TRANSITION, DAILY -> 0.60;
            case EMOTIONAL -> 0.75;
            case DESCRIPTION, CLIMAX -> 0.85;
            case OPENING, ENDING -> 0.75;
        };
    }

    /**
     * 获取推荐的字数
     */
    private int getRecommendedWordCount(SceneType type) {
        return switch (type) {
            case TRANSITION -> 300;
            case DAILY -> 500;
            case ACTION, DIALOGUE, EMOTIONAL, DESCRIPTION -> 700;
            case OPENING, ENDING -> 600;
            case CONFLICT -> 800;
            case CLIMAX -> 1200;
        };
    }

    /**
     * 获取氛围建议
     */
    private List<String> getAtmosphereSuggestions(String mood) {
        return switch (mood.toLowerCase()) {
            case "紧张", "tense" -> List.of("压迫的", "凝重的", "危险的");
            case "温馨", "warm" -> List.of("温暖的", "舒适的", "平和的");
            case "诡异", "eerie" -> List.of("神秘的", "阴森的", "诡谲的");
            case "激动", "excited" -> List.of("激昂的", "振奋的", "热烈的");
            case "悲伤", "sad" -> List.of("沉重的", "哀伤的", "低沉的");
            default -> List.of("符合情绪的", "一致的", "协调的");
        };
    }

    /**
     * 获取天气建议
     */
    private List<String> getWeatherSuggestions(String mood) {
        return switch (mood.toLowerCase()) {
            case "紧张", "tense" -> List.of("暴风雨", "阴云密布", "闷热");
            case "温馨", "warm" -> List.of("晴朗", "微风", "温暖的阳光");
            case "诡异", "eerie" -> List.of("浓雾", "阴天", "诡异的寂静");
            case "悲伤", "sad" -> List.of("阴雨", "灰蒙蒙", "细雨");
            default -> List.of("适宜的", "普通的");
        };
    }

    /**
     * 计算预估Token数
     */
    private int calculateEstimatedTokens(SceneGenerationRequest request) {
        int wordCount = request.getTargetWordCount() != null ? request.getTargetWordCount() : 600;
        // 中文约1.5字符=1token，加上提示词和JSON开销
        return (int) (wordCount * 1.5 * 1.3 + 2000);
    }

    /**
     * 获取警告信息
     */
    private List<String> getWarnings(SceneGenerationRequest request) {
        List<String> warnings = new ArrayList<>();

        if (request.getTargetWordCount() != null && request.getTargetWordCount() > 1500) {
            warnings.add("目标字数较大，生成时间可能较长");
        }

        if (request.getCharacterIds() != null && request.getCharacterIds().size() > 5) {
            warnings.add("参与角色较多，可能导致提示词过长");
        }

        if (Boolean.TRUE.equals(request.getIncludeOlfactory())
                && Boolean.TRUE.equals(request.getIncludeTactile())
                && Boolean.TRUE.equals(request.getIncludeTaste())) {
            warnings.add("启用了全部感官细节，生成内容会更详细但也更耗时");
        }

        return warnings;
    }
}
