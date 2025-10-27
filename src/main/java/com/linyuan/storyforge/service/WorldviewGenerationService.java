package com.linyuan.storyforge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linyuan.storyforge.dto.WorldviewDTO;
import com.linyuan.storyforge.dto.WorldviewGenerationRequest;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.enums.WorldviewGenre;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.WorldviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * WorldviewGenerationService - 世界观AI生成服务
 * 基于用户输入的关键词和类型，生成完整的世界观设定
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldviewGenerationService {

    private final AiGenerationService aiService;
    private final WorldviewService worldviewService;
    private final ProjectRepository projectRepository;
    private final WorldviewRepository worldviewRepository;
    private final ObjectMapper objectMapper;

    /**
     * 生成完整的世界观设定
     *
     * @param request 生成请求参数
     * @return 生成的世界观DTO
     */
    @Transactional
    public WorldviewDTO generateWorldview(WorldviewGenerationRequest request) {
        log.info("开始生成世界观 - 项目: {}, 类型: {}, 关键词: {}",
                request.getProjectId(), request.getGenre(), request.getKeywords());

        // 1. 验证项目存在
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // 2. 构建提示词
        String prompt = buildWorldviewPrompt(request);
        log.debug("生成的提示词长度: {} 字符", prompt.length());

        // 3. 调用AI生成
        double temperature = (request.getCreativity() != null) ? request.getCreativity() : 0.8;
        String aiResponse = aiService.chatWithOptions(prompt, temperature, 3500);
        log.debug("AI响应长度: {} 字符", aiResponse.length());

        // 4. 解析AI响应
        WorldviewDTO worldviewDTO = parseWorldviewResponse(aiResponse, request);

        // 5. 补充必要字段
        worldviewDTO.setProjectId(request.getProjectId());
        if (worldviewDTO.getName() == null || worldviewDTO.getName().isEmpty()) {
            worldviewDTO.setName(generateDefaultName(request));
        }

        // 6. 保存到数据库
        WorldviewDTO savedWorldview = worldviewService.createWorldview(worldviewDTO);
        log.info("世界观生成成功 - ID: {}, 名称: {}", savedWorldview.getId(), savedWorldview.getName());

        return savedWorldview;
    }

    /**
     * 构建世界观生成提示词
     *
     * @param request 生成请求
     * @return 完整的提示词
     */
    private String buildWorldviewPrompt(WorldviewGenerationRequest request) {
        WorldviewGenre genre = request.getGenre();
        String keywordsStr = String.join("、", request.getKeywords());

        StringBuilder prompt = new StringBuilder();

        // 角色定位
        prompt.append("# 角色定位\n");
        prompt.append("你是一位专业的世界观设计师，擅长创造富有深度和一致性的虚构世界。\n\n");

        // 任务说明
        prompt.append("# 任务\n");
        prompt.append("请基于以下信息，创建一个完整、详细、内部一致的世界观设定。\n\n");

        // 输入信息
        prompt.append("# 输入信息\n");
        prompt.append(String.format("- 故事类型: %s\n", genre.getDisplayName()));
        prompt.append(String.format("- 核心关键词: %s\n", keywordsStr));
        if (request.getWorldScale() != null) {
            prompt.append(String.format("- 世界规模: %s\n", request.getWorldScale()));
        }
        if (request.getPowerLevel() != null) {
            prompt.append(String.format("- 力量水平: %s\n", request.getPowerLevel()));
        }
        if (request.getCivilizationStage() != null) {
            prompt.append(String.format("- 文明阶段: %s\n", request.getCivilizationStage()));
        }
        if (request.getAdditionalRequirements() != null) {
            prompt.append(String.format("- 特殊要求: %s\n", request.getAdditionalRequirements()));
        }
        prompt.append("\n");

        // 类型特定要求
        prompt.append("# 类型特定要求\n");
        prompt.append(genre.getGenerationRequirements());
        prompt.append("\n");

        // 生成要求
        prompt.append("# 生成要求\n");
        prompt.append("请创建包含以下完整结构的世界观：\n\n");

        prompt.append("## 1. 基础信息\n");
        prompt.append("- 世界观名称: 简洁且富有特色的名称\n");
        prompt.append("- 核心概要: 用200-300字概括这个世界的本质特征\n\n");

        prompt.append("## 2. 宇宙法则 (universeLaws)\n");
        prompt.append("- physics: 物理规律（与现实的差异、特殊现象）\n");
        prompt.append("- magic_or_tech: 魔法/科技体系（核心机制、能量来源、使用规则）\n");
        prompt.append("- power_system: 力量体系（等级划分、获取方式、限制条件）\n\n");

        prompt.append("## 3. 社会结构 (socialStructure)\n");
        prompt.append("- politics: 政治体系（国家形态、权力结构、统治方式）\n");
        prompt.append("- economy: 经济模式（货币体系、贸易方式、资源分配）\n");
        prompt.append("- culture: 文化特征（价值观、习俗、艺术、宗教）\n");
        prompt.append("- hierarchy: 社会阶层（等级制度、流动性、特权与限制）\n\n");

        if (request.getIncludeDetailedGeography()) {
            prompt.append("## 4. 地理环境 (geography)\n");
            prompt.append("- scale: 世界规模描述\n");
            prompt.append("- regions: 主要地区列表（至少5个），每个包含name和description\n");
            prompt.append("- climate: 气候特征和季节变化\n");
            prompt.append("- special_locations: 特殊地点（圣地、禁区、遗迹等）\n\n");
        }

        if (request.getIncludeDetailedHistory()) {
            prompt.append("## 5. 历史背景 (historyBackground)\n");
            prompt.append("- origin: 创世神话或世界起源（200字）\n");
            prompt.append("- major_events: 重大历史事件列表（至少5个），按时间顺序\n");
            prompt.append("- legends: 著名传说（2-3个）\n");
            prompt.append("- current_era: 当前时代的特征和主要矛盾\n\n");
        }

        prompt.append("## 6. 规则与约束\n");
        prompt.append("- rules: 世界运行的基本规则列表（8-12条）\n");
        prompt.append("- constraints: 绝对禁忌和限制列表（5-8条）\n\n");

        if (request.getIncludeTerminology()) {
            prompt.append("## 7. 专有名词词典 (terminology)\n");
            prompt.append("- 创造15-20个专有名词（地名、组织名、物品名、概念名等）\n");
            prompt.append("- 每个名词提供简洁的解释（20-50字）\n\n");
        }

        // 输出格式要求
        prompt.append("# 输出格式\n");
        prompt.append("**必须**严格按照以下JSON格式输出，不要添加任何其他文字：\n\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"name\": \"世界观名称\",\n");
        prompt.append("  \"summary\": \"200-300字的核心概要\",\n");
        prompt.append("  \"universeLaws\": {\n");
        prompt.append("    \"physics\": \"物理规律描述\",\n");
        prompt.append("    \"magic_or_tech\": \"魔法或科技体系详细描述\",\n");
        prompt.append("    \"power_system\": \"力量体系描述\"\n");
        prompt.append("  },\n");
        prompt.append("  \"socialStructure\": {\n");
        prompt.append("    \"politics\": \"政治体系描述\",\n");
        prompt.append("    \"economy\": \"经济模式描述\",\n");
        prompt.append("    \"culture\": \"文化特征描述\",\n");
        prompt.append("    \"hierarchy\": \"社会阶层描述\"\n");
        prompt.append("  },\n");

        if (request.getIncludeDetailedGeography()) {
            prompt.append("  \"geography\": {\n");
            prompt.append("    \"scale\": \"世界规模\",\n");
            prompt.append("    \"regions\": [{\"name\": \"地区名\", \"description\": \"描述\"}],\n");
            prompt.append("    \"climate\": \"气候描述\",\n");
            prompt.append("    \"special_locations\": [\"地点1\", \"地点2\"]\n");
            prompt.append("  },\n");
        }

        if (request.getIncludeDetailedHistory()) {
            prompt.append("  \"historyBackground\": {\n");
            prompt.append("    \"origin\": \"起源故事\",\n");
            prompt.append("    \"major_events\": [\"事件1\", \"事件2\"],\n");
            prompt.append("    \"legends\": [\"传说1\", \"传说2\"],\n");
            prompt.append("    \"current_era\": \"当前时代描述\"\n");
            prompt.append("  },\n");
        }

        prompt.append("  \"rules\": [\"规则1\", \"规则2\", \"...\"],\n");
        prompt.append("  \"constraints\": [\"约束1\", \"约束2\", \"...\"]\n");

        if (request.getIncludeTerminology()) {
            prompt.append("  ,\"terminology\": {\n");
            prompt.append("    \"专有名词1\": \"解释\",\n");
            prompt.append("    \"专有名词2\": \"解释\"\n");
            prompt.append("  }\n");
        }

        prompt.append("}\n");
        prompt.append("```\n\n");

        // 质量要求
        prompt.append("# 质量要求\n");
        prompt.append("1. **内部一致性**: 所有设定必须相互协调，不能自相矛盾\n");
        prompt.append("2. **细节丰富**: 每个方面都要有具体的细节描述\n");
        prompt.append("3. **逻辑合理**: 设定要符合基本逻辑，有合理的因果关系\n");
        prompt.append("4. **创意独特**: 避免陈词滥调，追求独特性\n");
        prompt.append("5. **可扩展性**: 为后续的故事发展留下足够的空间\n\n");

        prompt.append("请现在开始生成，只输出JSON格式的结果。\n");

        return prompt.toString();
    }

    /**
     * 解析AI响应的JSON
     *
     * @param aiResponse AI的原始响应
     * @param request    原始请求（用于补充信息）
     * @return 解析后的WorldviewDTO
     */
    private WorldviewDTO parseWorldviewResponse(String aiResponse, WorldviewGenerationRequest request) {
        try {
            // 清理响应，移除可能的Markdown代码块标记
            String cleanJson = aiResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            // 配置ObjectMapper，忽略未知属性
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // 解析JSON
            JsonNode rootNode = mapper.readTree(cleanJson);

            // 构建WorldviewDTO
            WorldviewDTO dto = new WorldviewDTO();
            dto.setName(rootNode.path("name").asText());
            dto.setSummary(rootNode.path("summary").asText());

            // 解析universeLaws
            if (rootNode.has("universeLaws")) {
                dto.setUniverseLaws(mapper.convertValue(
                        rootNode.get("universeLaws"),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            // 解析socialStructure
            if (rootNode.has("socialStructure")) {
                dto.setSocialStructure(mapper.convertValue(
                        rootNode.get("socialStructure"),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            // 解析geography
            if (rootNode.has("geography")) {
                dto.setGeography(mapper.convertValue(
                        rootNode.get("geography"),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            // 解析historyBackground
            if (rootNode.has("historyBackground")) {
                dto.setHistoryBackground(mapper.convertValue(
                        rootNode.get("historyBackground"),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            // 解析terminology
            if (rootNode.has("terminology")) {
                dto.setTerminology(mapper.convertValue(
                        rootNode.get("terminology"),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            // 解析rules
            if (rootNode.has("rules")) {
                dto.setRules(mapper.convertValue(
                        rootNode.get("rules"),
                        new TypeReference<List<String>>() {}
                ));
            }

            // 解析constraints
            if (rootNode.has("constraints")) {
                dto.setConstraints(mapper.convertValue(
                        rootNode.get("constraints"),
                        new TypeReference<List<String>>() {}
                ));
            }

            log.info("成功解析AI响应 - 世界观名称: {}", dto.getName());
            return dto;

        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            log.error("原始响应: {}", aiResponse);

            // 返回一个基础的WorldviewDTO，包含错误信息
            WorldviewDTO fallbackDTO = new WorldviewDTO();
            fallbackDTO.setName(generateDefaultName(request));
            fallbackDTO.setSummary("AI生成失败，请稍后重试。错误信息: " + e.getMessage());
            fallbackDTO.setRules(List.of("生成失败"));
            fallbackDTO.setConstraints(List.of("生成失败"));

            return fallbackDTO;
        }
    }

    /**
     * 生成默认的世界观名称
     *
     * @param request 生成请求
     * @return 默认名称
     */
    private String generateDefaultName(WorldviewGenerationRequest request) {
        String firstKeyword = request.getKeywords().isEmpty() ?
                "未命名" : request.getKeywords().get(0);
        return request.getGenre().getDisplayName() + "世界 - " + firstKeyword;
    }

    /**
     * 批量生成多个世界观方案供选择
     *
     * @param request 生成请求
     * @param count   生成数量（建议2-3个）
     * @return 生成的世界观列表
     */
    @Transactional
    public List<WorldviewDTO> generateMultipleWorldviews(WorldviewGenerationRequest request, int count) {
        log.info("批量生成 {} 个世界观方案", count);

        List<WorldviewDTO> worldviews = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                // 每次生成使用略微不同的创意度
                Double originalCreativity = request.getCreativity();
                request.setCreativity(originalCreativity + (i * 0.05)); // 逐渐提高创意度

                WorldviewDTO worldview = generateWorldview(request);
                worldviews.add(worldview);

                // 恢复原始创意度
                request.setCreativity(originalCreativity);

            } catch (Exception e) {
                log.error("第 {} 个世界观生成失败", i + 1, e);
            }
        }

        return worldviews;
    }
}
