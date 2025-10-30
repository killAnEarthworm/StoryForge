package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.ChapterGenerationRequest;
import com.linyuan.storyforge.dto.RegenerateOptions;
import com.linyuan.storyforge.dto.StoryChapterDTO;
import com.linyuan.storyforge.service.GenerationHistoryEnhancedService;
import com.linyuan.storyforge.service.StoryGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * StoryChapterGenerationController - 章节生成控制器
 * 提供章节生成、重新生成、优化等高级功能
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class StoryChapterGenerationController {

    private final StoryGenerationService storyGenerationService;
    private final GenerationHistoryEnhancedService historyService;

    /**
     * 生成新章节
     * POST /api/chapters/generate
     *
     * @param request 章节生成请求
     * @return 生成的章节
     */
    @PostMapping("/generate")
    public ApiResponse<StoryChapterDTO> generateChapter(@RequestBody ChapterGenerationRequest request) {
        log.info("收到章节生成请求 - 项目: {}, 章节号: {}",
                request.getProjectId(), request.getChapterNumber());

        try {
            StoryChapterDTO chapter = storyGenerationService.generateChapter(request);
            return ApiResponse.success(chapter);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("章节生成失败", e);
            return ApiResponse.error(500, "章节生成失败: " + e.getMessage());
        }
    }

    /**
     * 重新生成章节
     * POST /api/chapters/{id}/regenerate
     *
     * @param id      章节ID
     * @param options 重新生成选项
     * @return 重新生成的章节
     */
    @PostMapping("/{id}/regenerate")
    public ApiResponse<StoryChapterDTO> regenerateChapter(
            @PathVariable UUID id,
            @RequestBody RegenerateOptions options) {

        log.info("重新生成章节 - ID: {}", id);

        try {
            StoryChapterDTO chapter = storyGenerationService.regenerateChapter(id, options);
            return ApiResponse.success(chapter);
        } catch (Exception e) {
            log.error("章节重新生成失败", e);
            return ApiResponse.error(500, "章节重新生成失败: " + e.getMessage());
        }
    }

    /**
     * 优化章节（基于用户反馈）
     * POST /api/chapters/{id}/refine
     *
     * @param id           章节ID
     * @param userFeedback 用户反馈
     * @return 优化后的章节
     */
    @PostMapping("/{id}/refine")
    public ApiResponse<StoryChapterDTO> refineChapter(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String userFeedback = body.get("feedback");
        if (userFeedback == null || userFeedback.isEmpty()) {
            return ApiResponse.error(400, "用户反馈不能为空");
        }

        log.info("优化章节 - ID: {}, 反馈: {}", id, userFeedback);

        try {
            StoryChapterDTO chapter = storyGenerationService.refineChapter(id, userFeedback);
            return ApiResponse.success(chapter);
        } catch (Exception e) {
            log.error("章节优化失败", e);
            return ApiResponse.error(500, "章节优化失败: " + e.getMessage());
        }
    }

    /**
     * 仅生成章节大纲
     * POST /api/chapters/generate-outline
     *
     * @param request 章节生成请求（简化版）
     * @return 生成的大纲文本
     */
    @PostMapping("/generate-outline")
    public ApiResponse<String> generateOutline(@RequestBody ChapterGenerationRequest request) {
        log.info("生成章节大纲 - 项目: {}, 章节号: {}",
                request.getProjectId(), request.getChapterNumber());

        try {
            String outline = storyGenerationService.generateChapterOutline(request);
            return ApiResponse.success(outline);
        } catch (Exception e) {
            log.error("大纲生成失败", e);
            return ApiResponse.error(500, "大纲生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取章节的所有版本
     * GET /api/chapters/versions?projectId=xxx&chapterNumber=1
     *
     * @param projectId     项目ID
     * @param chapterNumber 章节编号
     * @return 所有版本列表
     */
    @GetMapping("/versions")
    public ApiResponse<List<StoryChapterDTO>> getChapterVersions(
            @RequestParam UUID projectId,
            @RequestParam int chapterNumber) {

        log.info("获取章节版本 - 项目: {}, 章节号: {}", projectId, chapterNumber);

        try {
            List<StoryChapterDTO> versions = storyGenerationService.getChapterVersions(projectId, chapterNumber);
            return ApiResponse.success(versions);
        } catch (Exception e) {
            log.error("获取版本失败", e);
            return ApiResponse.error(500, "获取版本失败: " + e.getMessage());
        }
    }

    /**
     * 获取前文上下文
     * GET /api/chapters/context?projectId=xxx&upToChapter=5&contextSize=2
     *
     * @param projectId    项目ID
     * @param upToChapter  截止到第几章
     * @param contextSize  上下文大小（默认2章）
     * @return 前文内容
     */
    @GetMapping("/context")
    public ApiResponse<String> getPreviousContext(
            @RequestParam UUID projectId,
            @RequestParam int upToChapter,
            @RequestParam(required = false, defaultValue = "2") int contextSize) {

        log.info("获取前文上下文 - 项目: {}, 截止章节: {}, 大小: {}",
                projectId, upToChapter, contextSize);

        try {
            String context = storyGenerationService.loadPreviousChaptersContext(
                    projectId, upToChapter, contextSize);
            return ApiResponse.success(context != null ? context : "暂无前文");
        } catch (Exception e) {
            log.error("获取前文失败", e);
            return ApiResponse.error(500, "获取前文失败: " + e.getMessage());
        }
    }

    /**
     * 分析最佳生成参数
     * GET /api/chapters/analyze-parameters?projectId=xxx
     *
     * @param projectId 项目ID
     * @return 最佳参数建议
     */
    @GetMapping("/analyze-parameters")
    public ApiResponse<Map<String, Object>> analyzeOptimalParameters(@RequestParam UUID projectId) {
        log.info("分析最佳参数 - 项目: {}", projectId);

        try {
            Map<String, Object> params = historyService.analyzeOptimalParameters(projectId, "chapter");
            return ApiResponse.success(params);
        } catch (Exception e) {
            log.error("参数分析失败", e);
            return ApiResponse.error(500, "参数分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取生成统计信息
     * GET /api/chapters/statistics?projectId=xxx
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(@RequestParam UUID projectId) {
        log.info("获取生成统计 - 项目: {}", projectId);

        try {
            Map<String, Object> stats = historyService.getProjectStatistics(projectId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计失败", e);
            return ApiResponse.error(500, "获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 快速生成测试（简化接口）
     * POST /api/chapters/quick-generate
     *
     * @param body 包含基本参数的Map
     * @return 生成的章节
     */
    @PostMapping("/quick-generate")
    public ApiResponse<StoryChapterDTO> quickGenerate(@RequestBody Map<String, Object> body) {
        try {
            ChapterGenerationRequest request = ChapterGenerationRequest.builder()
                    .projectId(UUID.fromString((String) body.get("projectId")))
                    .chapterNumber((Integer) body.get("chapterNumber"))
                    .title((String) body.get("title"))
                    .characterIds((List<UUID>) body.get("characterIds"))
                    .sceneContext((String) body.get("sceneContext"))
                    .targetWordCount((Integer) body.getOrDefault("targetWordCount", 2000))
                    .enableConsistencyCheck(false) // 快速生成不验证
                    .autoCreateMemory(false) // 快速生成不创建记忆
                    .build();

            StoryChapterDTO chapter = storyGenerationService.generateChapter(request);
            return ApiResponse.success(chapter);
        } catch (Exception e) {
            log.error("快速生成失败", e);
            return ApiResponse.error(500, "快速生成失败: " + e.getMessage());
        }
    }
}
