package com.linyuan.storyforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linyuan.storyforge.dto.*;
import com.linyuan.storyforge.entity.*;
import com.linyuan.storyforge.enums.SceneType;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SceneGenerationService - 场景AI生成服务
 * 基于用户输入和上下文，生成详细的场景内容
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneGenerationService {

    private final AiGenerationService aiService;
    private final SceneService sceneService;
    private final WorldviewService worldviewService;
    private final CharacterService characterService;
    private final CharacterMemoryEnhancedService memoryService;
    private final TimelineService timelineService;
    private final ProjectRepository projectRepository;
    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;

    /**
     * 生成单个场景
     *
     * @param request 场景生成请求
     * @return 生成的场景DTO
     */
    @Transactional
    public SceneDTO generateScene(SceneGenerationRequest request) {
        log.info("开始生成场景 - {}", request.getSimpleDescription());

        // 1. 验证请求
        String validationError = request.validate();
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // 2. 验证项目存在
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // 3. 加载上下文数据
        GenerationContext context = buildGenerationContext(request);

        // 4. 构建提示词
        String prompt = buildScenePrompt(request, context);
        log.debug("提示词长度: {} 字符", prompt.length());

        // 5. 调用AI生成
        double temperature = (request.getCreativity() != null) ? request.getCreativity() : 0.75;
        int maxTokens = calculateMaxTokens(request.getTargetWordCount());
        String aiResponse = aiService.chatWithOptions(prompt, temperature, maxTokens);
        log.debug("AI响应长度: {} 字符", aiResponse.length());

        // 6. 解析响应
        SceneDTO sceneDTO = parseSceneResponse(aiResponse, request);

        // 7. 补充必要字段
        sceneDTO.setProjectId(request.getProjectId());
        sceneDTO.setWorldviewId(request.getWorldviewId());

        // 8. 保存到数据库
        SceneDTO savedScene = sceneService.createScene(sceneDTO);
        log.info("场景生成成功 - ID: {}, 名称: {}", savedScene.getId(), savedScene.getName());

        // 9. 如果启用了时间线事件提取，自动创建时间线事件
        if (Boolean.TRUE.equals(request.getExtractTimelineEvents())) {
            try {
                extractAndCreateTimelineEvents(savedScene, request);
            } catch (Exception e) {
                log.error("提取时间线事件失败（场景已保存） - 场景ID: {}", savedScene.getId(), e);
                // 不中断流程，仅记录错误
            }
        }

        return savedScene;
    }

    /**
     * 批量生成多个场景方案
     *
     * @param request 场景生成请求
     * @param count   生成数量
     * @return 生成的场景列表
     */
    @Transactional
    public List<SceneDTO> generateMultipleScenes(SceneGenerationRequest request, int count) {
        log.info("批量生成{}个场景方案 - {}", count, request.getSimpleDescription());

        if (count < 1 || count > 5) {
            throw new IllegalArgumentException("批量生成数量必须在1-5之间");
        }

        List<SceneDTO> scenes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // 每次调整创意度，生成不同版本
            SceneGenerationRequest variantRequest = request.toBuilder()
                    .creativity(Math.min(1.0, request.getCreativity() + i * 0.05))
                    .build();

            try {
                SceneDTO scene = generateScene(variantRequest);
                scenes.add(scene);
            } catch (Exception e) {
                log.error("生成第{}个场景方案失败", i + 1, e);
                // 继续生成其他方案
            }
        }

        log.info("批量生成完成，成功生成{}个场景", scenes.size());
        return scenes;
    }

    /**
     * 扩展已有场景的物理描述
     *
     * @param sceneId         场景ID
     * @param expansionPoint  扩展点描述
     * @param additionalWords 额外字数
     * @return 扩展后的场景
     */
    @Transactional
    public SceneDTO expandScene(UUID sceneId, String expansionPoint, int additionalWords) {
        log.info("扩展场景 - ID: {}, 扩展点: {}, 额外字数: {}", sceneId, expansionPoint, additionalWords);

        SceneDTO existingScene = sceneService.getSceneById(sceneId);

        String prompt = buildExpansionPrompt(existingScene, expansionPoint, additionalWords);

        double temperature = 0.7;
        int maxTokens = calculateMaxTokens(additionalWords);
        String expansion = aiService.chatWithOptions(prompt, temperature, maxTokens);

        // 将扩展内容添加到物理描述
        String expandedDescription = existingScene.getPhysicalDescription() + "\n\n" + expansion;
        existingScene.setPhysicalDescription(expandedDescription);

        // 更新场景
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", sceneId));
        scene.setPhysicalDescription(expandedDescription);
        sceneRepository.save(scene);

        log.info("场景扩展完成");
        return existingScene;
    }

    /**
     * 构建生成上下文
     */
    private GenerationContext buildGenerationContext(SceneGenerationRequest request) {
        GenerationContext context = new GenerationContext();

        // 加载世界观
        if (request.getWorldviewId() != null) {
            try {
                WorldviewDTO worldview = worldviewService.getWorldviewById(request.getWorldviewId());
                context.setWorldview(worldview);
            } catch (ResourceNotFoundException e) {
                log.warn("世界观不存在: {}", request.getWorldviewId());
            }
        }

        // 加载参与角色和记忆
        if (request.getCharacterIds() != null && !request.getCharacterIds().isEmpty()) {
            List<CharacterDTO> characters = new ArrayList<>();
            Map<UUID, List<CharacterMemory>> memoriesMap = new HashMap<>();

            for (UUID characterId : request.getCharacterIds()) {
                try {
                    CharacterDTO character = characterService.getCharacterById(characterId);
                    characters.add(character);

                    // 检索相关记忆
                    String sceneContext = buildSceneContext(request);
                    List<CharacterMemory> memories = memoryService.retrieveRelevantMemories(
                            characterId,
                            sceneContext,
                            request.getMood(),
                            3  // 每个角色最多3条记忆
                    );
                    memoriesMap.put(characterId, memories);
                } catch (ResourceNotFoundException e) {
                    log.warn("角色不存在: {}", characterId);
                }
            }

            context.setCharacters(characters);
            context.setCharacterMemories(memoriesMap);
        }

        // 加载前置场景
        if (request.getPreviousSceneId() != null) {
            try {
                SceneDTO previousScene = sceneService.getSceneById(request.getPreviousSceneId());
                context.setPreviousScene(previousScene);
            } catch (ResourceNotFoundException e) {
                log.warn("前置场景不存在: {}", request.getPreviousSceneId());
            }
        }

        return context;
    }

    /**
     * 构建场景生成提示词
     * 核心方法，集成SceneType指导、角色记忆、世界观等信息
     */
    private String buildScenePrompt(SceneGenerationRequest request, GenerationContext context) {
        SceneType sceneType = request.getSceneType();
        StringBuilder prompt = new StringBuilder();

        // ========== 1. 角色定位 ==========
        prompt.append("# 角色定位\n");
        prompt.append("你是一位专业的场景设计师，擅长通过细腻的感官描写营造氛围，")
                .append("创造出立体、真实、引人入胜的场景环境。\n\n");

        // ========== 2. 任务说明 ==========
        prompt.append("# 任务\n");
        prompt.append(String.format("请设计一个%s类型的场景。\n\n", sceneType.getDisplayName()));

        // ========== 3. 场景类型指导 ==========
        prompt.append("# 场景类型特征\n");
        prompt.append(String.format("**核心特征**: %s\n", sceneType.getFeatures()));
        prompt.append(String.format("**氛围关键词**: %s\n", sceneType.getAtmosphereKeywords()));
        prompt.append(String.format("**感官侧重**: %s\n\n", sceneType.getSensoryFocus()));

        prompt.append("## 设计要求\n");
        prompt.append(sceneType.getGenerationRequirements()).append("\n");

        // ========== 4. 世界观背景 ==========
        if (context.getWorldview() != null) {
            prompt.append("# 世界观背景\n");
            WorldviewDTO worldview = context.getWorldview();
            prompt.append(String.format("**世界观**: %s\n", worldview.getName()));
            if (worldview.getSummary() != null) {
                prompt.append(String.format("**概要**: %s\n", worldview.getSummary()));
            }
            if (worldview.getRules() != null && !worldview.getRules().isEmpty()) {
                prompt.append("\n**必须遵守的规则**:\n");
                worldview.getRules().forEach(rule -> prompt.append(String.format("- %s\n", rule)));
            }
            prompt.append("\n");
        }

        // ========== 5. 场景基本信息 ==========
        prompt.append("# 场景基本设定\n");
        prompt.append(String.format("- **地点**: %s\n", request.getLocation()));
        if (request.getTimeOfDay() != null) {
            prompt.append(String.format("- **时间**: %s\n", request.getTimeOfDay()));
        }
        if (request.getWeather() != null) {
            prompt.append(String.format("- **天气**: %s\n", request.getWeather()));
        }
        if (request.getSeason() != null) {
            prompt.append(String.format("- **季节**: %s\n", request.getSeason()));
        }
        prompt.append(String.format("- **情绪基调**: %s\n", request.getMood()));
        if (request.getAtmosphere() != null) {
            prompt.append(String.format("- **氛围**: %s\n", request.getAtmosphere()));
        }
        prompt.append("\n");

        // ========== 6. 参与角色信息 ==========
        if (context.getCharacters() != null && !context.getCharacters().isEmpty()) {
            prompt.append("# 可能出现的角色\n");
            for (CharacterDTO character : context.getCharacters()) {
                prompt.append(String.format("- %s", character.getName()));
                if (character.getPersonalityTraits() != null && !character.getPersonalityTraits().isEmpty()) {
                    prompt.append(String.format(" (%s)", String.join("、", character.getPersonalityTraits())));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }

        // ========== 7. 情节上下文 ==========
        if (request.getPlotContext() != null || request.getScenePurpose() != null) {
            prompt.append("# 情节背景\n");
            if (request.getScenePurpose() != null) {
                prompt.append(String.format("**用途**: %s\n", request.getScenePurpose()));
            }
            if (request.getPlotContext() != null) {
                prompt.append(String.format("**背景**: %s\n", request.getPlotContext()));
            }
            prompt.append("\n");
        }

        // ========== 8. 感官描写要求 ==========
        prompt.append("# 感官描写指导\n");
        prompt.append(sceneType.getSensoryGuidance()).append("\n");

        List<String> sensoryRequirements = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getIncludeVisualDetails())) {
            sensoryRequirements.add("视觉");
        }
        if (Boolean.TRUE.equals(request.getIncludeAuditoryDetails())) {
            sensoryRequirements.add("听觉");
        }
        if (Boolean.TRUE.equals(request.getIncludeOlfactory())) {
            sensoryRequirements.add("嗅觉");
        }
        if (Boolean.TRUE.equals(request.getIncludeTactile())) {
            sensoryRequirements.add("触觉");
        }
        if (Boolean.TRUE.equals(request.getIncludeTaste())) {
            sensoryRequirements.add("味觉");
        }

        if (!sensoryRequirements.isEmpty()) {
            prompt.append(String.format("\n**重点感官**: %s\n\n", String.join("、", sensoryRequirements)));
        }

        // ========== 9. 约束条件 ==========
        if ((request.getMustInclude() != null && !request.getMustInclude().isEmpty())
                || (request.getMustAvoid() != null && !request.getMustAvoid().isEmpty())) {
            prompt.append("# 约束条件\n");

            if (request.getMustInclude() != null && !request.getMustInclude().isEmpty()) {
                prompt.append("**必须包含的元素**:\n");
                request.getMustInclude().forEach(item -> prompt.append(String.format("- %s\n", item)));
                prompt.append("\n");
            }

            if (request.getMustAvoid() != null && !request.getMustAvoid().isEmpty()) {
                prompt.append("**必须避免**:\n");
                request.getMustAvoid().forEach(item -> prompt.append(String.format("- %s\n", item)));
                prompt.append("\n");
            }
        }

        // ========== 10. 输出格式 ==========
        prompt.append("# 输出格式\n");
        prompt.append("请以JSON格式输出场景设定，包含以下字段：\n\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"name\": \"场景名称（简洁，5-15字）\",\n");
        prompt.append("  \"locationType\": \"室内/室外/虚拟空间/混合\",\n");
        prompt.append("  \"physicalDescription\": \"详细的物理环境描写（300-800字），包括空间布局、建筑结构、物品摆设、环境特征等\",\n");
        prompt.append("  \"timeSetting\": \"具体时间设定（如'深夜23:00'、'黎明时分'）\",\n");
        prompt.append("  \"atmosphere\": \"氛围和情绪基调的详细描述（100-200字）\",\n");
        prompt.append("  \"weather\": \"天气状况（如'暴雨'、'晴朗'、'阴天'）\",\n");
        prompt.append("  \"lighting\": \"光线情况（如'昏暗'、'明亮'、'忽明忽暗'）\",\n");
        prompt.append("  \"availableProps\": {\n");
        prompt.append("    \"道具名称1\": \"道具描述和用途\",\n");
        prompt.append("    \"道具名称2\": \"道具描述和用途\"\n");
        prompt.append("  },\n");
        prompt.append("  \"environmentalElements\": [\"环境元素1\", \"环境元素2\", \"环境元素3\"],\n");
        prompt.append("  \"sensoryDetails\": {\n");
        prompt.append("    \"visual\": [\"视觉细节1\", \"视觉细节2\"],\n");
        prompt.append("    \"auditory\": [\"听觉细节1\", \"听觉细节2\"],\n");
        prompt.append("    \"olfactory\": [\"嗅觉细节\"],\n");
        prompt.append("    \"tactile\": [\"触觉细节\"],\n");
        prompt.append("    \"taste\": [\"味觉细节（如适用）\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"sceneSummary\": \"场景概要（50-100字简述）\",\n");
        prompt.append("  \"moodKeywords\": [\"情绪关键词1\", \"情绪关键词2\", \"情绪关键词3\"]\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        // ========== 11. 质量要求 ==========
        prompt.append("# 质量标准\n");
        prompt.append("1. **细节真实**: 物理描写要具体可感，避免空洞抽象\n");
        prompt.append("2. **氛围连贯**: 各要素相互呼应，营造统一氛围\n");
        prompt.append("3. **感官丰富**: 充分调动多种感官描写\n");
        prompt.append("4. **道具合理**: 可用道具符合场景设定和时代背景\n");
        prompt.append("5. **可用性强**: 环境元素可以用于故事情节展开\n");

        if (context.getWorldview() != null) {
            prompt.append("6. **世界观一致**: 严格遵守世界观规则\n");
        }

        return prompt.toString();
    }

    /**
     * 解析AI响应
     */
    private SceneDTO parseSceneResponse(String aiResponse, SceneGenerationRequest request) {
        try {
            // 清理Markdown代码块标记
            String cleanJson = aiResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            JsonNode root = objectMapper.readTree(cleanJson);

            SceneDTO sceneDTO = new SceneDTO();

            // 基础字段
            if (root.has("name")) {
                sceneDTO.setName(root.get("name").asText());
            } else {
                sceneDTO.setName(generateDefaultName(request));
            }

            if (root.has("locationType")) {
                sceneDTO.setLocationType(root.get("locationType").asText());
            }

            // 场景设定
            if (root.has("physicalDescription")) {
                sceneDTO.setPhysicalDescription(root.get("physicalDescription").asText());
            }

            if (root.has("timeSetting")) {
                sceneDTO.setTimeSetting(root.get("timeSetting").asText());
            } else if (request.getTimeOfDay() != null) {
                sceneDTO.setTimeSetting(request.getTimeOfDay());
            }

            if (root.has("atmosphere")) {
                sceneDTO.setAtmosphere(root.get("atmosphere").asText());
            } else if (request.getAtmosphere() != null) {
                sceneDTO.setAtmosphere(request.getAtmosphere());
            }

            if (root.has("weather")) {
                sceneDTO.setWeather(root.get("weather").asText());
            } else if (request.getWeather() != null) {
                sceneDTO.setWeather(request.getWeather());
            }

            if (root.has("lighting")) {
                sceneDTO.setLighting(root.get("lighting").asText());
            }

            // 环境元素
            if (root.has("availableProps")) {
                Map<String, Object> props = objectMapper.convertValue(
                        root.get("availableProps"),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                sceneDTO.setAvailableProps(props);
            }

            if (root.has("environmentalElements")) {
                List<String> elements = objectMapper.convertValue(
                        root.get("environmentalElements"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                sceneDTO.setEnvironmentalElements(elements);
            }

            if (root.has("sensoryDetails")) {
                Map<String, Object> sensory = objectMapper.convertValue(
                        root.get("sensoryDetails"),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                sceneDTO.setSensoryDetails(sensory);
            }

            // AI辅助
            if (root.has("sceneSummary")) {
                sceneDTO.setSceneSummary(root.get("sceneSummary").asText());
            }

            if (root.has("moodKeywords")) {
                List<String> keywords = objectMapper.convertValue(
                        root.get("moodKeywords"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                sceneDTO.setMoodKeywords(keywords);
            }

            return sceneDTO;

        } catch (Exception e) {
            log.error("解析场景响应失败", e);
            // 返回fallback
            return createFallbackScene(request, aiResponse);
        }
    }

    /**
     * 构建场景扩展提示词
     */
    private String buildExpansionPrompt(SceneDTO scene, String expansionPoint, int additionalWords) {
        return String.format("""
                # 任务
                请对以下场景的某个部分进行扩展细化。

                # 原场景
                **名称**: %s
                **物理描述**: %s

                # 扩展要求
                **扩展点**: %s
                **额外字数**: %d字

                请针对指定的扩展点，生成更详细的环境描写。保持与原场景的风格和氛围一致。

                只返回扩展的内容，不要重复原有内容。
                """,
                scene.getName(),
                scene.getPhysicalDescription(),
                expansionPoint,
                additionalWords
        );
    }

    /**
     * 构建场景上下文字符串（用于记忆检索）
     */
    private String buildSceneContext(SceneGenerationRequest request) {
        return String.format("%s，%s，%s",
                request.getLocation(),
                request.getMood(),
                request.getPlotContext() != null ? request.getPlotContext() : "");
    }

    /**
     * 计算最大Token数
     */
    private int calculateMaxTokens(int targetWordCount) {
        // 中文大约1.5个字符=1个token，加上JSON结构的开销
        return (int) (targetWordCount * 1.5 * 1.3);
    }

    /**
     * 生成默认名称
     */
    private String generateDefaultName(SceneGenerationRequest request) {
        return String.format("%s - %s", request.getLocation(), request.getSceneType().getDisplayName());
    }

    /**
     * 创建fallback场景
     */
    private SceneDTO createFallbackScene(SceneGenerationRequest request, String rawContent) {
        SceneDTO fallback = new SceneDTO();
        fallback.setName(generateDefaultName(request));
        fallback.setLocationType("未分类");
        fallback.setPhysicalDescription(rawContent);
        fallback.setTimeSetting(request.getTimeOfDay());
        fallback.setAtmosphere(request.getAtmosphere() != null ? request.getAtmosphere() : request.getMood());
        fallback.setWeather(request.getWeather());

        // 基础情绪关键词
        List<String> keywords = new ArrayList<>();
        keywords.add(request.getMood());
        if (request.getAtmosphere() != null) {
            keywords.add(request.getAtmosphere());
        }
        fallback.setMoodKeywords(keywords);

        fallback.setSceneSummary("场景生成时发生解析错误，已保存原始内容");

        return fallback;
    }

    /**
     * 生成上下文内部类
     */
    private static class GenerationContext {
        private WorldviewDTO worldview;
        private List<CharacterDTO> characters;
        private Map<UUID, List<CharacterMemory>> characterMemories;
        private SceneDTO previousScene;

        public WorldviewDTO getWorldview() {
            return worldview;
        }

        public void setWorldview(WorldviewDTO worldview) {
            this.worldview = worldview;
        }

        public List<CharacterDTO> getCharacters() {
            return characters;
        }

        public void setCharacters(List<CharacterDTO> characters) {
            this.characters = characters;
        }

        public Map<UUID, List<CharacterMemory>> getCharacterMemories() {
            return characterMemories;
        }

        public void setCharacterMemories(Map<UUID, List<CharacterMemory>> characterMemories) {
            this.characterMemories = characterMemories;
        }

        public SceneDTO getPreviousScene() {
            return previousScene;
        }

        public void setPreviousScene(SceneDTO previousScene) {
            this.previousScene = previousScene;
        }
    }

    // ==================== 时间线集成方法 ====================

    /**
     * 从生成的场景中提取并创建时间线事件
     * 根据场景类型、参与角色和情节内容自动创建相关的时间线事件
     *
     * @param scene   生成的场景
     * @param request 原始请求
     */
    private void extractAndCreateTimelineEvents(SceneDTO scene, SceneGenerationRequest request) {
        log.info("开始从场景提取时间线事件 - 场景: {}", scene.getName());

        // 如果没有参与角色，无法创建时间线事件
        if (request.getCharacterIds() == null || request.getCharacterIds().isEmpty()) {
            log.debug("场景无参与角色，跳过时间线事件创建");
            return;
        }

        // 根据场景类型确定事件类型和重要度
        String eventType = mapSceneTypeToEventType(request.getSceneType());
        int memoryImportance = calculateMemoryImportance(request);

        // 为每个参与角色创建时间线事件
        for (UUID characterId : request.getCharacterIds()) {
            try {
                // 构建事件描述
                String eventDescription = buildEventDescription(scene, request);

                // 创建时间线事件
                TimelineDTO timelineEvent = timelineService.addEventFromScene(
                        request.getProjectId(),
                        characterId,
                        eventDescription,
                        eventType,
                        memoryImportance
                );

                log.debug("成功为角色 {} 创建时间线事件: {}", characterId, timelineEvent.getId());
            } catch (Exception e) {
                log.error("为角色 {} 创建时间线事件失败", characterId, e);
                // 继续处理其他角色
            }
        }

        log.info("场景时间线事件提取完成 - 场景: {}, 处理角色数: {}",
                scene.getName(), request.getCharacterIds().size());
    }

    /**
     * 将场景类型映射到时间线事件类型
     *
     * @param sceneType 场景类型
     * @return 事件类型代码
     */
    private String mapSceneTypeToEventType(SceneType sceneType) {
        return switch (sceneType) {
            case ACTION -> "action";
            case DIALOGUE -> "dialogue";
            case EMOTIONAL -> "inner_change";
            case CONFLICT -> "conflict";
            case CLIMAX -> "climax";
            case TRANSITION -> "environment_change";
            case OPENING -> "encounter";
            case ENDING -> "resolution";
            case DESCRIPTION -> "environment_change";
            case DAILY -> "action";
        };
    }

    /**
     * 计算记忆重要度
     * 基于场景类型、情感强度和是否包含关键事件
     *
     * @param request 场景生成请求
     * @return 记忆重要度 (1-10)
     */
    private int calculateMemoryImportance(SceneGenerationRequest request) {
        int importance = 5; // 基础重要度

        // 根据场景类型调整
        importance += switch (request.getSceneType()) {
            case CLIMAX -> 4;           // 高潮场景最重要
            case CONFLICT -> 3;         // 冲突场景较重要
            case EMOTIONAL, OPENING, ENDING -> 2;  // 情感、开场、结尾场景重要
            case ACTION, DIALOGUE -> 1; // 动作和对话场景一般
            case TRANSITION, DAILY, DESCRIPTION -> 0; // 过渡和日常场景不太重要
        };

        // 根据情感强度调整（如果有）
        if (request.getEmotionalIntensity() != null && request.getEmotionalIntensity() >= 7) {
            importance += 1;
        }

        // 如果有关键事件列表，提高重要度
        if (request.getKeyEvents() != null && !request.getKeyEvents().isEmpty()) {
            importance += 1;
        }

        // 如果有冲突点，提高重要度
        if (request.getConflict() != null && !request.getConflict().isBlank()) {
            importance += 1;
        }

        // 确保在1-10范围内
        return Math.max(1, Math.min(10, importance));
    }

    /**
     * 构建事件描述
     * 综合场景的多个维度信息，生成简洁的事件描述
     *
     * @param scene   生成的场景
     * @param request 原始请求
     * @return 事件描述
     */
    private String buildEventDescription(SceneDTO scene, SceneGenerationRequest request) {
        StringBuilder description = new StringBuilder();

        // 基础信息：地点 + 场景类型
        description.append(String.format("在%s", request.getLocation()));

        // 添加时间信息（如果有）
        if (request.getTimeOfDay() != null) {
            description.append(String.format("的%s", request.getTimeOfDay()));
        }

        // 添加场景目的（如果有）
        if (request.getScenePurpose() != null && !request.getScenePurpose().isBlank()) {
            description.append("，").append(request.getScenePurpose());
        }
        // 否则使用场景摘要
        else if (scene.getSceneSummary() != null && !scene.getSceneSummary().isBlank()) {
            description.append("，").append(scene.getSceneSummary());
        }
        // 否则使用场景类型的描述
        else {
            description.append("，发生了").append(request.getSceneType().getDisplayName()).append("场景");
        }

        // 添加关键事件（如果有）
        if (request.getKeyEvents() != null && !request.getKeyEvents().isEmpty()) {
            description.append("。关键事件：");
            description.append(String.join("、", request.getKeyEvents()));
        }

        // 限制长度（Timeline eventDescription字段通常不需要太长）
        String result = description.toString();
        if (result.length() > 200) {
            result = result.substring(0, 197) + "...";
        }

        return result;
    }

    /**
     * 为已存在的场景手动提取时间线事件
     * 用于补充或重新生成时间线事件
     *
     * @param sceneId     场景ID
     * @param characterId 角色ID
     * @param eventType   事件类型（可选，如果为null则自动推断）
     * @return 创建的时间线事件
     */
    @Transactional
    public TimelineDTO extractTimelineEventFromExistingScene(UUID sceneId, UUID characterId, String eventType) {
        log.info("为现有场景提取时间线事件 - 场景ID: {}, 角色ID: {}", sceneId, characterId);

        // 1. 获取场景
        SceneDTO scene = sceneService.getSceneById(sceneId);

        // 2. 确定事件类型（如果未提供）
        if (eventType == null || eventType.isBlank()) {
            // 根据场景摘要或氛围推断事件类型
            eventType = inferEventTypeFromScene(scene);
        }

        // 3. 构建简化的事件描述
        String eventDescription = String.format("场景：%s。%s",
                scene.getName(),
                scene.getSceneSummary() != null ? scene.getSceneSummary() : scene.getAtmosphere());

        // 4. 确定记忆重要度（基于场景的情绪关键词）
        int memoryImportance = inferMemoryImportanceFromScene(scene);

        // 5. 创建时间线事件
        TimelineDTO timelineEvent = timelineService.addEventFromScene(
                scene.getProjectId(),
                characterId,
                eventDescription,
                eventType,
                memoryImportance
        );

        log.info("成功为现有场景创建时间线事件 - 事件ID: {}", timelineEvent.getId());
        return timelineEvent;
    }

    /**
     * 从场景推断事件类型
     *
     * @param scene 场景
     * @return 事件类型代码
     */
    private String inferEventTypeFromScene(SceneDTO scene) {
        // 根据场景的情绪关键词推断
        if (scene.getMoodKeywords() != null && !scene.getMoodKeywords().isEmpty()) {
            String firstKeyword = scene.getMoodKeywords().get(0).toLowerCase();

            if (firstKeyword.contains("冲突") || firstKeyword.contains("对抗") || firstKeyword.contains("战斗")) {
                return "conflict";
            } else if (firstKeyword.contains("对话") || firstKeyword.contains("交流") || firstKeyword.contains("沟通")) {
                return "dialogue";
            } else if (firstKeyword.contains("情感") || firstKeyword.contains("悲伤") || firstKeyword.contains("喜悦")) {
                return "inner_change";
            } else if (firstKeyword.contains("发现") || firstKeyword.contains("揭示") || firstKeyword.contains("真相")) {
                return "discovery";
            } else if (firstKeyword.contains("决定") || firstKeyword.contains("选择") || firstKeyword.contains("抉择")) {
                return "decision";
            }
        }

        // 默认返回行动类型
        return "action";
    }

    /**
     * 从场景推断记忆重要度
     *
     * @param scene 场景
     * @return 记忆重要度 (1-10)
     */
    private int inferMemoryImportanceFromScene(SceneDTO scene) {
        int importance = 5; // 基础重要度

        // 根据情绪关键词判断
        if (scene.getMoodKeywords() != null && !scene.getMoodKeywords().isEmpty()) {
            for (String keyword : scene.getMoodKeywords()) {
                String lowerKeyword = keyword.toLowerCase();
                if (lowerKeyword.contains("高潮") || lowerKeyword.contains("转折")) {
                    importance += 3;
                    break;
                } else if (lowerKeyword.contains("冲突") || lowerKeyword.contains("危机") || lowerKeyword.contains("关键")) {
                    importance += 2;
                    break;
                } else if (lowerKeyword.contains("重要") || lowerKeyword.contains("紧张") || lowerKeyword.contains("激动")) {
                    importance += 1;
                    break;
                }
            }
        }

        // 如果氛围强烈，提高重要度
        if (scene.getAtmosphere() != null) {
            String atmosphere = scene.getAtmosphere().toLowerCase();
            if (atmosphere.contains("强烈") || atmosphere.contains("极度") || atmosphere.contains("剧烈")) {
                importance += 1;
            }
        }

        return Math.max(1, Math.min(10, importance));
    }
}
