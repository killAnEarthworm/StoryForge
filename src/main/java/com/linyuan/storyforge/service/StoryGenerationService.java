package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.*;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Scene;
import com.linyuan.storyforge.entity.StoryChapter;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.SceneRepository;
import com.linyuan.storyforge.repository.StoryChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StoryGenerationService - 故事章节生成服务
 * 将 GenerationPipeline 与 StoryChapter 实体整合
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoryGenerationService {

    private final GenerationPipeline generationPipeline;
    private final MemoryIntegrationService memoryService;
    private final GenerationHistoryEnhancedService historyService;
    private final StoryChapterService chapterService;
    private final StoryChapterRepository chapterRepository;
    private final ProjectRepository projectRepository;
    private final SceneRepository sceneRepository;

    /**
     * 生成并保存章节
     *
     * @param request 章节生成请求
     * @return 生成的章节DTO
     */
    @Transactional
    public StoryChapterDTO generateChapter(ChapterGenerationRequest request) {
        log.info("======== 开始生成章节 ========");
        log.info("项目: {}, 章节号: {}, 标题: {}",
                request.getProjectId(), request.getChapterNumber(), request.getTitle());

        // 1. 验证请求
        if (!request.isValid()) {
            throw new IllegalArgumentException("请求参数无效: " + request.getValidationErrors());
        }

        // 2. 检查章节是否已存在
        List<StoryChapter> existing = chapterRepository
                .findByProjectIdOrderByChapterNumberAsc(request.getProjectId()).stream()
                .filter(c -> c.getChapterNumber().equals(request.getChapterNumber()))
                .collect(Collectors.toList());

        if (!existing.isEmpty() && request.getSaveAsVersion() == null) {
            throw new IllegalStateException(
                    String.format("章节 %d 已存在，请使用 regenerateChapter 方法或指定 saveAsVersion",
                            request.getChapterNumber()));
        }

        // 3. 加载前文上下文（如果需要）
        String previousContent = null;
        if (Boolean.TRUE.equals(request.getLoadPreviousContext()) && request.getChapterNumber() > 1) {
            previousContent = loadPreviousChaptersContext(
                    request.getProjectId(),
                    request.getChapterNumber() - 1,
                    request.getPreviousContextSize()
            );
            log.info("加载前文上下文: {} 个章节", request.getPreviousContextSize());
        }

        // 4. 转换为 GenerationRequest
        GenerationRequest genRequest = request.toGenerationRequest();
        genRequest.setPreviousContent(previousContent);

        // 5. 调用生成管道
        log.info("调用生成管道...");
        GenerationResult result = generationPipeline.execute(genRequest);

        if (!result.isSuccess()) {
            log.error("生成失败: {}", result.getErrorMessage());
            throw new RuntimeException("章节生成失败: " + result.getErrorMessage());
        }

        // 6. 创建 StoryChapter 实体
        StoryChapter chapter = createChapterEntity(request, result);

        // 7. 保存章节
        chapter = chapterRepository.save(chapter);
        log.info("章节保存成功: {}", chapter.getId());

        // 8. 记录生成历史
        UUID historyId = historyService.recordGeneration(
                result,
                genRequest,
                result.getGeneratedContent(),
                chapter.getId(),
                "chapter"
        );
        log.info("生成历史记录成功: {}", historyId);

        // 9. 返回DTO
        StoryChapterDTO dto = chapterService.convertToDTO(chapter);
        dto.setGenerationResult(result); // 附加生成结果信息

        log.info("======== 章节生成完成 ========");
        log.info("字数: {}, 质量得分: {}, 耗时: {}ms",
                result.getGeneratedContent().length(),
                result.getLowestConsistencyScore(),
                result.getDurationMs());

        return dto;
    }

    /**
     * 重新生成章节
     *
     * @param chapterId 章节ID
     * @param options   重新生成选项
     * @return 重新生成的章节
     */
    @Transactional
    public StoryChapterDTO regenerateChapter(UUID chapterId, RegenerateOptions options) {
        log.info("重新生成章节: {}", chapterId);

        // 1. 加载原章节
        StoryChapter originalChapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("StoryChapter", "id", chapterId));

        // 2. 构建新的生成请求（基于原章节）
        ChapterGenerationRequest request = buildRequestFromChapter(originalChapter, options);

        // 3. 如果保留原版本，设置新版本号
        if (Boolean.TRUE.equals(options.getKeepOriginal())) {
            int newVersion = options.getNewVersion() != null ?
                    options.getNewVersion() :
                    originalChapter.getVersion() + 1;
            request.setSaveAsVersion(newVersion);
        }

        // 4. 生成新章节
        StoryChapterDTO newChapter = generateChapter(request);

        // 5. 如果不保留原版本，删除原章节
        if (Boolean.FALSE.equals(options.getKeepOriginal())) {
            chapterRepository.deleteById(chapterId);
            log.info("已删除原章节: {}", chapterId);
        }

        return newChapter;
    }

    /**
     * 优化章节（基于用户反馈）
     *
     * @param chapterId    章节ID
     * @param userFeedback 用户反馈
     * @return 优化后的章节
     */
    @Transactional
    public StoryChapterDTO refineChapter(UUID chapterId, String userFeedback) {
        log.info("优化章节 - ID: {}, 反馈: {}", chapterId, userFeedback);

        RegenerateOptions options = RegenerateOptions.builder()
                .changeInstructions(userFeedback)
                .keepOriginal(true)
                .refreshMemories(true)
                .build();

        return regenerateChapter(chapterId, options);
    }

    /**
     * 生成章节大纲
     *
     * @param request 大纲生成请求
     * @return 生成的大纲文本
     */
    @Transactional
    public String generateChapterOutline(ChapterGenerationRequest request) {
        log.info("生成章节大纲 - 项目: {}, 章节: {}",
                request.getProjectId(), request.getChapterNumber());

        // 修改请求：只生成大纲，不生成完整内容
        GenerationRequest outlineRequest = GenerationRequest.builder()
                .projectId(request.getProjectId())
                .worldviewId(request.getWorldviewId())
                .characterIds(request.getCharacterIds())
                .contentType(com.linyuan.storyforge.enums.ContentType.CHAPTER)
                .sceneContext(request.getSceneContext())
                .generationGoal("请生成本章节的详细大纲（500字以内）：\n" +
                        "1. 章节主题\n" +
                        "2. 主要情节点\n" +
                        "3. 人物发展\n" +
                        "4. 冲突和转折\n" +
                        "5. 章节结尾")
                .enableMemory(false) // 大纲生成不需要记忆
                .enableConsistencyCheck(false) // 大纲不需要一致性检查
                .autoCreateMemory(false)
                .temperature(0.7) // 较低温度，更结构化
                .maxTokens(1000) // 限制长度
                .build();

        GenerationResult result = generationPipeline.execute(outlineRequest);

        if (!result.isSuccess()) {
            throw new RuntimeException("大纲生成失败: " + result.getErrorMessage());
        }

        return result.getGeneratedContent();
    }

    /**
     * 加载前文上下文
     *
     * @param projectId    项目ID
     * @param upToChapter  截止到第几章
     * @param contextSize  上下文大小（最近N章）
     * @return 前文内容摘要
     */
    @Transactional(readOnly = true)
    public String loadPreviousChaptersContext(UUID projectId, int upToChapter, Integer contextSize) {
        log.debug("加载前文上下文 - 项目: {}, 截止章节: {}, 上下文大小: {}",
                projectId, upToChapter, contextSize);

        int size = contextSize != null ? contextSize : 2;

        List<StoryChapter> previousChapters = chapterRepository
                .findByProjectIdOrderByChapterNumberAsc(projectId).stream()
                .filter(c -> c.getChapterNumber() <= upToChapter)
                .filter(c -> c.getChapterNumber() > upToChapter - size)
                .sorted(Comparator.comparing(StoryChapter::getChapterNumber))
                .collect(Collectors.toList());

        if (previousChapters.isEmpty()) {
            return null;
        }

        StringBuilder context = new StringBuilder();
        context.append("## 前文回顾\n\n");

        for (StoryChapter chapter : previousChapters) {
            context.append("### ").append(chapter.getTitle() != null ?
                    chapter.getTitle() :
                    "第" + chapter.getChapterNumber() + "章").append("\n");

            // 使用大纲或截取内容
            if (chapter.getOutline() != null && !chapter.getOutline().isEmpty()) {
                context.append(chapter.getOutline()).append("\n\n");
            } else if (chapter.getGeneratedContent() != null) {
                String content = chapter.getGeneratedContent();
                // 截取前300字作为摘要
                int length = Math.min(300, content.length());
                context.append(content.substring(0, length));
                if (content.length() > 300) {
                    context.append("...");
                }
                context.append("\n\n");
            }
        }

        return context.toString();
    }

    /**
     * 获取章节的所有版本
     *
     * @param projectId     项目ID
     * @param chapterNumber 章节编号
     * @return 所有版本的章节列表
     */
    @Transactional(readOnly = true)
    public List<StoryChapterDTO> getChapterVersions(UUID projectId, int chapterNumber) {
        log.debug("获取章节所有版本 - 项目: {}, 章节号: {}", projectId, chapterNumber);

        return chapterRepository.findByProjectIdOrderByChapterNumberAsc(projectId).stream()
                .filter(c -> c.getChapterNumber().equals(chapterNumber))
                .sorted(Comparator.comparing(StoryChapter::getVersion))
                .map(chapterService::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建 StoryChapter 实体
     */
    private StoryChapter createChapterEntity(ChapterGenerationRequest request, GenerationResult result) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Scene mainScene = null;
        if (request.getMainSceneId() != null) {
            mainScene = sceneRepository.findById(request.getMainSceneId()).orElse(null);
        }

        // 构建生成参数记录
        Map<String, Object> generationParams = new HashMap<>();
        generationParams.put("temperature", request.getTemperature());
        generationParams.put("maxTokens", request.getMaxTokens());
        generationParams.put("memoriesUsed", result.getMemoriesUsed());
        generationParams.put("passedValidation", result.isPassedAllValidation());
        generationParams.put("retryCount", result.getRetryCount());
        generationParams.put("qualityScore", result.getLowestConsistencyScore());

        int version = request.getSaveAsVersion() != null ?
                request.getSaveAsVersion() : 1;

        return StoryChapter.builder()
                .project(project)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .outline(request.getOutline())
                .mainConflict(request.getMainConflict())
                .participatingCharacters(request.getCharacterIds())
                .mainScene(mainScene)
                .targetWordCount(request.getTargetWordCount())
                .tone(request.getTone())
                .pacing(request.getPacing())
                .generatedContent(result.getGeneratedContent())
                .generationParams(generationParams)
                .version(version)
                .status(request.getStatus())
                .build();
    }

    /**
     * 从现有章节构建生成请求
     */
    private ChapterGenerationRequest buildRequestFromChapter(
            StoryChapter chapter,
            RegenerateOptions options) {

        ChapterGenerationRequest.ChapterGenerationRequestBuilder builder = ChapterGenerationRequest.builder()
                .projectId(chapter.getProject().getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .outline(chapter.getOutline())
                .mainConflict(chapter.getMainConflict())
                .characterIds(chapter.getParticipatingCharacters())
                .mainSceneId(chapter.getMainScene() != null ? chapter.getMainScene().getId() : null)
                .targetWordCount(chapter.getTargetWordCount())
                .tone(chapter.getTone())
                .pacing(chapter.getPacing())
                .status("drafted");

        // 应用重新生成选项
        if (options != null) {
            if (options.getTargetWordCount() != null) {
                builder.targetWordCount(options.getTargetWordCount());
            }
            if (options.getTone() != null) {
                builder.tone(options.getTone());
            }
            if (options.getPacing() != null) {
                builder.pacing(options.getPacing());
            }

            // 添加修改说明到生成目标
            String additionalGoal = options.buildAdditionalGoal();
            if (!additionalGoal.isEmpty()) {
                builder.generationGoal(additionalGoal);
            }

            // 调整温度
            if (chapter.getGenerationParams() != null &&
                    chapter.getGenerationParams().containsKey("temperature")) {
                Double originalTemp = ((Number) chapter.getGenerationParams().get("temperature")).doubleValue();
                builder.temperature(options.getAdjustedTemperature(originalTemp));
            }
        }

        return builder.build();
    }
}
