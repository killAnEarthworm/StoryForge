package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.GenerationRequest;
import com.linyuan.storyforge.dto.GenerationResult;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.service.GenerationPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * StoryGenerationController - 故事生成控制器
 * 提供统一的故事生成API，使用完整的生成管道
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
public class StoryGenerationController {

    private final GenerationPipeline generationPipeline;

    /**
     * 执行完整的生成流程
     * 包含记忆检索、AI生成、一致性验证、记忆创建
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping("/execute")
    public ApiResponse<GenerationResult> executeGeneration(@RequestBody GenerationRequest request) {
        log.info("收到生成请求 - 项目: {}, 类型: {}", request.getProjectId(), request.getContentType());

        try {
            // 验证请求
            if (!request.isValid()) {
                return ApiResponse.error(400, "请求参数无效：projectId、contentType和characterIds不能为空");
            }

            // 执行生成流程
            GenerationResult result = generationPipeline.execute(request);

            if (result.isSuccess()) {
                return ApiResponse.success(result);
            } else {
                return ApiResponse.error(500, "生成失败: " + result.getErrorMessage(), result);
            }

        } catch (Exception e) {
            log.error("生成失败", e);
            return ApiResponse.error(500, "生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成章节（简化接口）
     *
     * @param projectId      项目ID
     * @param characterIds   角色ID列表
     * @param worldviewId    世界观ID（可选）
     * @param sceneContext   场景描述
     * @param generationGoal 生成目标
     * @return 生成结果
     */
    @PostMapping("/chapter")
    public ApiResponse<GenerationResult> generateChapter(
            @RequestParam UUID projectId,
            @RequestParam List<UUID> characterIds,
            @RequestParam(required = false) UUID worldviewId,
            @RequestParam String sceneContext,
            @RequestParam(required = false) String generationGoal) {

        log.info("生成章节 - 项目: {}, 角色数: {}", projectId, characterIds.size());

        GenerationRequest request = GenerationRequest.builder()
                .projectId(projectId)
                .characterIds(characterIds)
                .worldviewId(worldviewId)
                .contentType(ContentType.CHAPTER)
                .sceneContext(sceneContext)
                .generationGoal(generationGoal)
                .enableMemory(true)
                .enableConsistencyCheck(true)
                .autoCreateMemory(true)
                .maxRetries(2)
                .temperature(0.8)
                .maxTokens(2000)
                .build();

        return executeGeneration(request);
    }

    /**
     * 生成对话（简化接口）
     *
     * @param projectId    项目ID
     * @param characterIds 角色ID列表
     * @param sceneContext 场景描述
     * @param emotionalTone 情感基调
     * @return 生成结果
     */
    @PostMapping("/dialogue")
    public ApiResponse<GenerationResult> generateDialogue(
            @RequestParam UUID projectId,
            @RequestParam List<UUID> characterIds,
            @RequestParam String sceneContext,
            @RequestParam(required = false) String emotionalTone) {

        log.info("生成对话 - 项目: {}, 角色数: {}", projectId, characterIds.size());

        GenerationRequest request = GenerationRequest.builder()
                .projectId(projectId)
                .characterIds(characterIds)
                .contentType(ContentType.DIALOGUE)
                .sceneContext(sceneContext)
                .emotionalTone(emotionalTone)
                .enableMemory(true)
                .enableConsistencyCheck(true)
                .autoCreateMemory(true)
                .maxRetries(2)
                .temperature(0.9)
                .maxTokens(1000)
                .build();

        return executeGeneration(request);
    }

    /**
     * 生成场景描写（简化接口）
     *
     * @param projectId    项目ID
     * @param worldviewId  世界观ID
     * @param sceneContext 场景描述要求
     * @return 生成结果
     */
    @PostMapping("/scene")
    public ApiResponse<GenerationResult> generateScene(
            @RequestParam UUID projectId,
            @RequestParam UUID worldviewId,
            @RequestParam UUID characterId,
            @RequestParam String sceneContext) {

        log.info("生成场景 - 项目: {}, 世界观: {}", projectId, worldviewId);

        GenerationRequest request = GenerationRequest.builder()
                .projectId(projectId)
                .characterIds(List.of(characterId)) // 场景也需要至少一个角色视角
                .worldviewId(worldviewId)
                .contentType(ContentType.SCENE)
                .sceneContext(sceneContext)
                .enableMemory(false) // 场景描写可以不检索记忆
                .enableConsistencyCheck(true)
                .autoCreateMemory(false) // 场景描写不自动创建记忆
                .maxRetries(1)
                .temperature(0.85)
                .maxTokens(1500)
                .build();

        return executeGeneration(request);
    }

    /**
     * 生成内心独白（简化接口）
     *
     * @param projectId     项目ID
     * @param characterId   角色ID
     * @param sceneContext  场景描述
     * @param emotionalTone 情感基调
     * @return 生成结果
     */
    @PostMapping("/monologue")
    public ApiResponse<GenerationResult> generateMonologue(
            @RequestParam UUID projectId,
            @RequestParam UUID characterId,
            @RequestParam String sceneContext,
            @RequestParam String emotionalTone) {

        log.info("生成内心独白 - 项目: {}, 角色: {}", projectId, characterId);

        GenerationRequest request = GenerationRequest.builder()
                .projectId(projectId)
                .characterIds(List.of(characterId))
                .contentType(ContentType.INNER_MONOLOGUE)
                .sceneContext(sceneContext)
                .emotionalTone(emotionalTone)
                .enableMemory(true)
                .enableConsistencyCheck(true)
                .autoCreateMemory(true)
                .maxRetries(2)
                .temperature(0.85)
                .maxTokens(800)
                .build();

        return executeGeneration(request);
    }

    /**
     * 测试接口：快速生成（不验证、不创建记忆）
     * 用于快速测试生成效果
     *
     * @param projectId    项目ID
     * @param characterIds 角色ID列表
     * @param prompt       生成提示
     * @return 生成结果
     */
    @PostMapping("/quick-test")
    public ApiResponse<GenerationResult> quickTest(
            @RequestParam UUID projectId,
            @RequestParam List<UUID> characterIds,
            @RequestParam String prompt) {

        log.info("快速测试生成 - 项目: {}", projectId);

        GenerationRequest request = GenerationRequest.builder()
                .projectId(projectId)
                .characterIds(characterIds)
                .contentType(ContentType.CHAPTER)
                .sceneContext(prompt)
                .enableMemory(false)
                .enableConsistencyCheck(false)
                .autoCreateMemory(false)
                .maxRetries(0)
                .temperature(0.8)
                .maxTokens(1000)
                .build();

        return executeGeneration(request);
    }

    /**
     * 获取生成统计信息
     * 用于监控和调试
     *
     * @param result 生成结果
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<String> getGenerationStats(@RequestBody GenerationResult result) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== 生成统计 ===\n");
        stats.append(String.format("成功: %s\n", result.isSuccess()));
        stats.append(String.format("耗时: %dms\n", result.getDurationMs()));
        stats.append(String.format("重试次数: %d\n", result.getRetryCount()));
        stats.append(String.format("使用记忆: %d 条\n", result.getMemoriesUsed()));
        stats.append(String.format("新建记忆: %d 条\n", result.getNewMemoryIds().size()));
        stats.append(String.format("一致性得分: %.2f\n", result.getLowestConsistencyScore()));
        stats.append(String.format("验证通过: %s\n", result.isPassedAllValidation()));

        if (!result.getAllViolations().isEmpty()) {
            stats.append("\n违规项:\n");
            for (String violation : result.getAllViolations()) {
                stats.append(String.format("- %s\n", violation));
            }
        }

        return ApiResponse.success(stats.toString());
    }
}
