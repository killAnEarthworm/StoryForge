package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.GenerationContext;
import com.linyuan.storyforge.dto.GenerationRequest;
import com.linyuan.storyforge.dto.GenerationResult;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.validator.CharacterConsistencyValidator;
import com.linyuan.storyforge.validator.ConsistencyResult;
import com.linyuan.storyforge.validator.WorldviewConsistencyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GenerationPipeline - 统一的生成管道
 * 实现：生成 → 角色一致性验证 → 世界观一致性验证 → 修正（重试）→ 记忆创建
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationPipeline {

    private final MemoryIntegrationService memoryService;
    private final AiGenerationService aiService;
    private final CharacterConsistencyValidator characterValidator;
    private final WorldviewConsistencyValidator worldviewValidator;

    /**
     * 执行完整的生成流程
     * 包含记忆检索、AI生成、一致性验证、记忆创建
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @Transactional
    public GenerationResult execute(GenerationRequest request) {
        log.info("========== 开始生成流程 ==========");
        log.info("项目: {}, 内容类型: {}, 角色数: {}",
                request.getProjectId(), request.getContentType(), request.getCharacterIds().size());

        long startTime = System.currentTimeMillis();

        // 验证请求
        if (!request.isValid()) {
            return GenerationResult.builder()
                    .success(false)
                    .errorMessage("请求参数无效")
                    .build();
        }

        GenerationResult result = GenerationResult.builder().build();
        result.addLog("开始生成流程");

        try {
            // 1. 构建生成上下文（包含记忆检索）
            result.addLog("步骤1: 构建生成上下文");
            GenerationContext context = memoryService.buildGenerationContext(request);

            if (context.hasMemories()) {
                int totalMemories = context.getCharacterMemories().values().stream()
                        .mapToInt(List::size)
                        .sum();
                result.setMemoriesUsed(totalMemories);
                result.addLog(String.format("检索到 %d 条相关记忆", totalMemories));
            }

            // 2. 生成内容（带重试机制）
            result.addLog("步骤2: 开始内容生成");
            String generatedContent = null;
            boolean passed = false;
            int retryCount = 0;

            while (retryCount <= request.getMaxRetries() && !passed) {
                if (retryCount > 0) {
                    result.addLog(String.format("第 %d 次重试生成", retryCount));
                }

                // 2.1 生成内容
                generatedContent = generateContent(request, context, result);
                result.setGeneratedContent(generatedContent);

                // 2.2 一致性验证（如果启用）
                if (request.isEnableConsistencyCheck()) {
                    result.addLog("步骤3: 执行一致性验证");
                    passed = performConsistencyCheck(generatedContent, context, result);

                    if (!passed && retryCount < request.getMaxRetries()) {
                        // 将验证失败信息注入到上下文，用于下次生成
                        injectValidationFeedback(context, result);
                    }
                } else {
                    passed = true; // 不验证则直接通过
                }

                retryCount++;
            }

            result.setRetryCount(retryCount - 1);
            result.setPassedAllValidation(passed);

            if (!passed) {
                result.addLog(String.format("警告: 经过 %d 次重试后仍未通过完整验证", retryCount - 1));
            } else {
                result.addLog("内容通过所有一致性验证");
            }

            // 3. 创建记忆（如果启用且生成成功）
            if (request.isAutoCreateMemory() && generatedContent != null) {
                result.addLog("步骤4: 提取并创建记忆");
                try {
                    List<UUID> newMemoryIds = memoryService.extractAndCreateMemories(
                            generatedContent,
                            context,
                            request.getTimelineId()
                    );
                    result.setNewMemoryIds(newMemoryIds);
                    result.addLog(String.format("成功创建 %d 条新记忆", newMemoryIds.size()));
                } catch (Exception e) {
                    log.error("创建记忆失败", e);
                    result.addLog("警告: 记忆创建失败 - " + e.getMessage());
                }
            }

            result.setSuccess(true);

        } catch (Exception e) {
            log.error("生成流程失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.addLog("错误: " + e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        result.setDurationMs(duration);
        result.addLog(String.format("生成流程完成，总耗时: %dms", duration));

        log.info("========== 生成流程结束 ==========");
        log.info("成功: {}, 耗时: {}ms, 重试次数: {}",
                result.isSuccess(), duration, result.getRetryCount());

        return result;
    }

    /**
     * 生成内容
     *
     * @param request 请求
     * @param context 上下文
     * @param result  结果（用于记录日志）
     * @return 生成的内容
     */
    private String generateContent(
            GenerationRequest request,
            GenerationContext context,
            GenerationResult result) {

        log.info("开始AI生成，内容类型: {}", request.getContentType());

        // 构建基础提示词
        String basePrompt = buildBasePrompt(request);

        // 增强提示词（注入记忆等信息）
        String enhancedPrompt = memoryService.buildEnhancedPrompt(basePrompt, context);

        log.debug("提示词长度: {} 字符", enhancedPrompt.length());
        result.addLog(String.format("提示词长度: %d 字符", enhancedPrompt.length()));

        // 调用AI生成
        String generated = aiService.chatWithOptions(
                enhancedPrompt,
                request.getTemperature(),
                request.getMaxTokens()
        );

        log.info("AI生成完成，内容长度: {} 字符", generated.length());
        result.addLog(String.format("生成内容长度: %d 字符", generated.length()));

        return generated;
    }

    /**
     * 执行一致性验证
     *
     * @param content 生成的内容
     * @param context 生成上下文
     * @param result  结果对象
     * @return true如果通过所有验证
     */
    private boolean performConsistencyCheck(
            String content,
            GenerationContext context,
            GenerationResult result) {

        log.info("开始一致性验证");
        boolean allPassed = true;

        // 1. 角色一致性验证
        if (context.getCharacters() != null && !context.getCharacters().isEmpty()) {
            for (Character character : context.getCharacters()) {
                log.debug("验证角色: {}", character.getName());

                ConsistencyResult charResult = characterValidator.validateContent(
                        character.getId(),
                        content,
                        ContentType.CHAPTER // 默认为章节类型，实际应根据request.getContentType()
                );

                result.addCharacterConsistencyResult(charResult);

                if (!charResult.getPassed()) {
                    allPassed = false;
                    log.warn("角色 {} 验证未通过，得分: {}, 违规数: {}",
                            character.getName(),
                            charResult.getOverallScore(),
                            charResult.getViolations().size());
                    result.addLog(String.format("角色 %s 验证未通过 (得分: %.2f)",
                            character.getName(), charResult.getOverallScore()));
                } else {
                    result.addLog(String.format("角色 %s 验证通过 (得分: %.2f)",
                            character.getName(), charResult.getOverallScore()));
                }
            }
        }

        // 2. 世界观一致性验证
        if (context.hasWorldview()) {
            log.debug("验证世界观: {}", context.getWorldview().getName());

            ConsistencyResult worldResult = worldviewValidator.validateContent(
                    context.getWorldview().getId(),
                    content,
                    ContentType.CHAPTER
            );

            result.setWorldviewConsistencyResult(worldResult);

            if (!worldResult.getPassed()) {
                allPassed = false;
                log.warn("世界观验证未通过，得分: {}, 违规数: {}",
                        worldResult.getOverallScore(),
                        worldResult.getViolations().size());
                result.addLog(String.format("世界观验证未通过 (得分: %.2f)",
                        worldResult.getOverallScore()));
            } else {
                result.addLog(String.format("世界观验证通过 (得分: %.2f)",
                        worldResult.getOverallScore()));
            }
        }

        log.info("一致性验证完成，结果: {}", allPassed ? "通过" : "未通过");
        return allPassed;
    }

    /**
     * 将验证失败信息注入到上下文
     * 用于下次生成时作为参考
     *
     * @param context 生成上下文
     * @param result  当前结果
     */
    private void injectValidationFeedback(GenerationContext context, GenerationResult result) {
        log.info("注入验证反馈到上下文");

        List<String> allViolations = result.getAllViolations();
        if (allViolations.isEmpty()) {
            return;
        }

        StringBuilder feedback = new StringBuilder("\n\n## 上次生成的问题\n");
        feedback.append("请注意避免以下问题：\n");
        for (int i = 0; i < allViolations.size(); i++) {
            feedback.append(String.format("%d. %s\n", i + 1, allViolations.get(i)));
        }

        // 将反馈添加到生成目标中
        String currentGoal = context.getGenerationGoal() != null ?
                context.getGenerationGoal() : "";
        context.setGenerationGoal(currentGoal + feedback.toString());

        log.debug("已添加 {} 条验证反馈", allViolations.size());
    }

    /**
     * 构建基础提示词
     * 根据不同的内容类型构建不同的提示词
     *
     * @param request 请求
     * @return 基础提示词
     */
    private String buildBasePrompt(GenerationRequest request) {
        ContentType contentType = request.getContentType();

        String basePrompt = switch (contentType) {
            case CHAPTER -> buildChapterPrompt(request);
            case DIALOGUE -> buildDialoguePrompt(request);
            case SCENE -> buildScenePrompt(request);
            case INNER_MONOLOGUE -> buildInnerMonologuePrompt(request);
            default -> buildDefaultPrompt(request);
        };

        // 添加生成目标
        if (request.getGenerationGoal() != null && !request.getGenerationGoal().isEmpty()) {
            basePrompt += "\n\n## 生成要求\n" + request.getGenerationGoal();
        }

        return basePrompt;
    }

    /**
     * 构建章节生成提示词
     */
    private String buildChapterPrompt(GenerationRequest request) {
        return String.format("""
                # 故事章节生成任务

                请根据以下信息生成一个精彩的故事章节。

                ## 要求
                1. 保持角色性格一致
                2. 符合世界观设定
                3. 语言生动，情节紧凑
                4. 字数：%d字左右

                请直接输出章节内容，不要添加章节标题。
                """,
                request.getMaxTokens() != null ? request.getMaxTokens() / 2 : 1000
        );
    }

    /**
     * 构建对话生成提示词
     */
    private String buildDialoguePrompt(GenerationRequest request) {
        return """
                # 对话生成任务

                请根据角色设定和场景，生成符合角色性格的对话。

                ## 要求
                1. 每个角色的说话方式必须符合其设定
                2. 对话要推进剧情
                3. 包含适当的动作描写和心理活动
                4. 对话要自然流畅

                格式：
                角色名："对话内容"
                [动作或心理描写]

                请直接输出对话内容。
                """;
    }

    /**
     * 构建场景描写提示词
     */
    private String buildScenePrompt(GenerationRequest request) {
        return """
                # 场景描写任务

                请根据以下信息描写场景。

                ## 要求
                1. 描写要细腻，包含视觉、听觉、嗅觉等多感官体验
                2. 符合世界观设定
                3. 营造适当的氛围
                4. 突出场景的特色和重要元素

                请直接输出场景描写。
                """;
    }

    /**
     * 构建内心独白提示词
     */
    private String buildInnerMonologuePrompt(GenerationRequest request) {
        return """
                # 内心独白生成任务

                请根据角色设定和当前情境，生成角色的内心独白。

                ## 要求
                1. 深入刻画角色的内心世界
                2. 符合角色性格和当前情绪
                3. 可以包含回忆、思考、情感波动
                4. 语言要符合角色特点

                请以第一人称视角输出内心独白。
                """;
    }

    /**
     * 默认提示词
     */
    private String buildDefaultPrompt(GenerationRequest request) {
        return """
                # 内容生成任务

                请根据以下信息生成内容。

                ## 要求
                1. 符合角色设定
                2. 符合世界观设定
                3. 内容连贯、合理

                请直接输出生成的内容。
                """;
    }
}
