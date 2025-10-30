package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.GenerationContext;
import com.linyuan.storyforge.dto.GenerationRequest;
import com.linyuan.storyforge.entity.*;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MemoryIntegrationService - 记忆集成服务
 * 负责将记忆系统整合到生成流程中
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryIntegrationService {

    private final CharacterMemoryEnhancedService memoryEnhancedService;
    private final CharacterMemoryRepository memoryRepository;
    private final CharacterRepository characterRepository;
    private final ProjectRepository projectRepository;
    private final WorldviewRepository worldviewRepository;
    private final TimelineRepository timelineRepository;
    private final AiGenerationService aiService;

    /**
     * 构建完整的生成上下文
     * 包含项目、角色、世界观、记忆等所有信息
     *
     * @param request 生成请求
     * @return 完整的生成上下文
     */
    @Transactional(readOnly = true)
    public GenerationContext buildGenerationContext(GenerationRequest request) {
        log.info("构建生成上下文 - 项目: {}, 角色数: {}",
                request.getProjectId(), request.getCharacterIds().size());

        long startTime = System.currentTimeMillis();

        // 1. 加载项目
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // 2. 加载世界观（如果有）
        Worldview worldview = null;
        if (request.getWorldviewId() != null) {
            worldview = worldviewRepository.findById(request.getWorldviewId())
                    .orElse(null);
            log.debug("加载世界观: {}", worldview != null ? worldview.getName() : "无");
        }

        // 3. 加载角色
        List<Character> characters = request.getCharacterIds().stream()
                .map(id -> characterRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id)))
                .collect(Collectors.toList());
        log.debug("加载角色: {}", characters.stream()
                .map(Character::getName)
                .collect(Collectors.joining(", ")));

        // 4. 加载记忆（如果启用）
        Map<UUID, List<CharacterMemory>> characterMemories = new HashMap<>();
        if (request.isEnableMemory()) {
            for (Character character : characters) {
                List<CharacterMemory> memories = memoryEnhancedService.retrieveRelevantMemories(
                        character.getId(),
                        request.getSceneContext(),
                        request.getEmotionalTone(),
                        request.getMemoryCount()
                );
                characterMemories.put(character.getId(), memories);
                log.debug("角色 {} 检索到 {} 条相关记忆",
                        character.getName(), memories.size());
            }
        }

        // 5. 构建上下文对象
        GenerationContext context = GenerationContext.builder()
                .project(project)
                .worldview(worldview)
                .characters(characters)
                .characterMemories(characterMemories)
                .sceneContext(request.getSceneContext())
                .emotionalTone(request.getEmotionalTone())
                .previousContent(request.getPreviousContent())
                .generationGoal(request.getGenerationGoal())
                .additionalParams(request.getAdditionalParams())
                .requireConsistencyCheck(request.isEnableConsistencyCheck())
                .maxRetries(request.getMaxRetries())
                .build();

        long duration = System.currentTimeMillis() - startTime;
        log.info("生成上下文构建完成，耗时: {}ms", duration);

        return context;
    }

    /**
     * 构建增强的提示词
     * 将记忆信息注入到提示词中
     *
     * @param basePrompt 基础提示词
     * @param context    生成上下文
     * @return 增强后的提示词
     */
    public String buildEnhancedPrompt(String basePrompt, GenerationContext context) {
        log.debug("构建增强提示词，包含记忆信息");

        StringBuilder enhancedPrompt = new StringBuilder(basePrompt);

        // 添加世界观信息
        if (context.hasWorldview()) {
            Worldview worldview = context.getWorldview();
            enhancedPrompt.append("\n\n## 世界观设定\n");
            enhancedPrompt.append("- 名称: ").append(worldview.getName()).append("\n");
            if (worldview.getSummary() != null) {
                enhancedPrompt.append("- 概述: ").append(worldview.getSummary()).append("\n");
            }
            if (worldview.getRules() != null && !worldview.getRules().isEmpty()) {
                enhancedPrompt.append("- 核心规则: ")
                        .append(String.join("; ", worldview.getRules()))
                        .append("\n");
            }
        }

        // 添加角色及其记忆信息
        if (context.getCharacters() != null && !context.getCharacters().isEmpty()) {
            enhancedPrompt.append("\n## 角色信息\n");

            for (Character character : context.getCharacters()) {
                enhancedPrompt.append("\n### ").append(character.getName()).append("\n");

                // 基础信息
                if (character.getCharacterSummary() != null) {
                    enhancedPrompt.append("概述: ").append(character.getCharacterSummary()).append("\n");
                } else {
                    enhancedPrompt.append("年龄: ").append(character.getAge()).append("\n");
                    enhancedPrompt.append("性格: ")
                            .append(character.getPersonalityTraits() != null ?
                                    String.join("、", character.getPersonalityTraits()) : "无")
                            .append("\n");
                }

                // 说话方式
                if (character.getSpeechPattern() != null) {
                    enhancedPrompt.append("说话方式: ").append(character.getSpeechPattern()).append("\n");
                }

                // 添加记忆
                if (context.hasMemories()) {
                    String memoryContext = context.buildMemoryContext(character.getId());
                    enhancedPrompt.append("\n").append(memoryContext);
                }
            }
        }

        // 添加场景上下文
        if (context.getSceneContext() != null && !context.getSceneContext().isEmpty()) {
            enhancedPrompt.append("\n## 当前场景\n");
            enhancedPrompt.append(context.getSceneContext()).append("\n");
        }

        // 添加情感基调
        if (context.getEmotionalTone() != null && !context.getEmotionalTone().isEmpty()) {
            enhancedPrompt.append("\n## 情感基调\n");
            enhancedPrompt.append(context.getEmotionalTone()).append("\n");
        }

        // 添加前文内容
        if (context.getPreviousContent() != null && !context.getPreviousContent().isEmpty()) {
            enhancedPrompt.append("\n## 前文内容\n");
            // 限制前文长度，避免token过多
            String prevContent = context.getPreviousContent();
            if (prevContent.length() > 1000) {
                prevContent = "..." + prevContent.substring(prevContent.length() - 1000);
            }
            enhancedPrompt.append(prevContent).append("\n");
        }

        log.debug("增强提示词构建完成，总长度: {} 字符", enhancedPrompt.length());
        return enhancedPrompt.toString();
    }

    /**
     * 从生成内容中提取并创建新记忆
     * 使用AI分析内容，提取关键事件
     *
     * @param generatedContent 生成的内容
     * @param context          生成上下文
     * @param timelineId       时间线ID（可选）
     * @return 新创建的记忆ID列表
     */
    @Transactional
    public List<UUID> extractAndCreateMemories(
            String generatedContent,
            GenerationContext context,
            UUID timelineId) {

        log.info("开始从生成内容中提取记忆");

        if (generatedContent == null || generatedContent.trim().isEmpty()) {
            log.warn("生成内容为空，无法提取记忆");
            return Collections.emptyList();
        }

        if (context.getCharacters() == null || context.getCharacters().isEmpty()) {
            log.warn("没有角色信息，无法创建记忆");
            return Collections.emptyList();
        }

        List<UUID> newMemoryIds = new ArrayList<>();

        try {
            // 使用AI提取关键事件
            String extractionPrompt = buildMemoryExtractionPrompt(generatedContent, context);
            String extractionResult = aiService.chatWithOptions(extractionPrompt, 0.3, 500);

            log.debug("AI提取结果: {}", extractionResult);

            // 解析提取结果并创建记忆
            List<CharacterMemory> newMemories = parseAndCreateMemories(
                    extractionResult,
                    context,
                    timelineId
            );

            for (CharacterMemory memory : newMemories) {
                CharacterMemory saved = memoryRepository.save(memory);
                newMemoryIds.add(saved.getId());
                log.info("创建新记忆: {} - {}", saved.getCharacter().getName(), saved.getMemoryType());
            }

        } catch (Exception e) {
            log.error("提取记忆失败", e);
            // 降级方案：创建简单的情节记忆
            newMemoryIds.addAll(createFallbackMemories(generatedContent, context, timelineId));
        }

        log.info("成功创建 {} 条新记忆", newMemoryIds.size());
        return newMemoryIds;
    }

    /**
     * 构建记忆提取提示词
     */
    private String buildMemoryExtractionPrompt(String content, GenerationContext context) {
        String characterNames = context.getCharacters().stream()
                .map(Character::getName)
                .collect(Collectors.joining("、"));

        return String.format("""
                请分析以下故事内容，提取关键事件作为角色记忆。

                角色：%s

                故事内容：
                %s

                请提取每个角色的关键经历，按以下格式输出（每行一条记忆）：
                角色名|记忆类型|记忆内容|情感权重(0.0-1.0)|关键词(用逗号分隔)

                记忆类型可选：核心记忆、情感记忆、技能记忆、情节记忆、语义记忆

                示例：
                张三|情感记忆|在战斗中失去了挚友，感到深深的悲痛|0.9|战斗,挚友,悲痛,失去

                请直接输出结果，不要解释。
                """,
                characterNames,
                content.length() > 500 ? content.substring(0, 500) + "..." : content
        );
    }

    /**
     * 解析AI提取结果并创建记忆对象
     */
    private List<CharacterMemory> parseAndCreateMemories(
            String extractionResult,
            GenerationContext context,
            UUID timelineId) {

        List<CharacterMemory> memories = new ArrayList<>();
        String[] lines = extractionResult.split("\n");

        // 创建角色名称到角色对象的映射
        Map<String, Character> characterMap = context.getCharacters().stream()
                .collect(Collectors.toMap(Character::getName, c -> c));

        Timeline timeline = null;
        if (timelineId != null) {
            timeline = timelineRepository.findById(timelineId).orElse(null);
        }

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || !line.contains("|")) {
                continue;
            }

            try {
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    continue;
                }

                String characterName = parts[0].trim();
                String memoryType = parts[1].trim();
                String memoryContent = parts[2].trim();
                float emotionalWeight = Float.parseFloat(parts[3].trim());
                List<String> keywords = parts.length > 4 ?
                        Arrays.asList(parts[4].split(",")).stream()
                                .map(String::trim)
                                .collect(Collectors.toList()) :
                        Collections.emptyList();

                Character character = characterMap.get(characterName);
                if (character == null) {
                    log.warn("未找到角色: {}", characterName);
                    continue;
                }

                CharacterMemory memory = CharacterMemory.builder()
                        .character(character)
                        .timeline(timeline)
                        .memoryType(memoryType)
                        .memoryContent(memoryContent)
                        .emotionalWeight(emotionalWeight)
                        .keywords(keywords)
                        .accessibility(1.0f)
                        .lastAccessed(LocalDateTime.now())
                        .accessCount(0)
                        .build();

                memories.add(memory);

            } catch (Exception e) {
                log.warn("解析记忆行失败: {}", line, e);
            }
        }

        return memories;
    }

    /**
     * 降级方案：创建简单的情节记忆
     */
    private List<UUID> createFallbackMemories(
            String content,
            GenerationContext context,
            UUID timelineId) {

        log.info("使用降级方案创建记忆");
        List<UUID> memoryIds = new ArrayList<>();

        Timeline timeline = null;
        if (timelineId != null) {
            timeline = timelineRepository.findById(timelineId).orElse(null);
        }

        // 为每个角色创建一条简单的情节记忆
        for (Character character : context.getCharacters()) {
            try {
                String memoryContent = String.format("参与了场景：%s",
                        context.getSceneContext() != null ?
                                context.getSceneContext() : "未命名场景");

                CharacterMemory memory = CharacterMemory.builder()
                        .character(character)
                        .timeline(timeline)
                        .memoryType("情节记忆")
                        .memoryContent(memoryContent)
                        .emotionalWeight(0.5f)
                        .keywords(Collections.emptyList())
                        .accessibility(1.0f)
                        .lastAccessed(LocalDateTime.now())
                        .accessCount(0)
                        .build();

                CharacterMemory saved = memoryRepository.save(memory);
                memoryIds.add(saved.getId());

            } catch (Exception e) {
                log.error("创建降级记忆失败", e);
            }
        }

        return memoryIds;
    }

    /**
     * 手动创建记忆（供外部调用）
     *
     * @param characterId     角色ID
     * @param memoryType      记忆类型
     * @param memoryContent   记忆内容
     * @param emotionalWeight 情感权重
     * @param keywords        关键词
     * @param timelineId      时间线ID（可选）
     * @return 新创建的记忆ID
     */
    @Transactional
    public UUID createMemory(
            UUID characterId,
            String memoryType,
            String memoryContent,
            Float emotionalWeight,
            List<String> keywords,
            UUID timelineId) {

        log.info("手动创建记忆 - 角色: {}, 类型: {}", characterId, memoryType);

        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

        Timeline timeline = null;
        if (timelineId != null) {
            timeline = timelineRepository.findById(timelineId).orElse(null);
        }

        CharacterMemory memory = CharacterMemory.builder()
                .character(character)
                .timeline(timeline)
                .memoryType(memoryType)
                .memoryContent(memoryContent)
                .emotionalWeight(emotionalWeight != null ? emotionalWeight : 0.5f)
                .keywords(keywords)
                .accessibility(1.0f)
                .lastAccessed(LocalDateTime.now())
                .accessCount(0)
                .build();

        CharacterMemory saved = memoryRepository.save(memory);
        log.info("记忆创建成功: {}", saved.getId());

        return saved.getId();
    }
}
