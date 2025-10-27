package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.CharacterDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.WorldviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing characters
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final ProjectRepository projectRepository;
    private final WorldviewRepository worldviewRepository;
    private final AiGenerationService aiGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all characters
     */
    @Transactional(readOnly = true)
    public List<CharacterDTO> getAllCharacters() {
        log.debug("Fetching all characters");
        return characterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get character by ID
     */
    @Transactional(readOnly = true)
    public CharacterDTO getCharacterById(UUID id) {
        log.debug("Fetching character with id: {}", id);
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));
        return convertToDTO(character);
    }

    /**
     * Get characters by project ID
     */
    @Transactional(readOnly = true)
    public List<CharacterDTO> getCharactersByProjectId(UUID projectId) {
        log.debug("Fetching characters for project: {}", projectId);
        return characterRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new character
     */
    @Transactional
    public CharacterDTO createCharacter(CharacterDTO characterDTO) {
        log.info("Creating new character: {}", characterDTO.getName());

        // Validate project exists
        Project project = projectRepository.findById(characterDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", characterDTO.getProjectId()));

        Character character = convertToEntity(characterDTO);
        character.setProject(project);

        // Set worldview if provided
        if (characterDTO.getWorldviewId() != null) {
            Worldview worldview = worldviewRepository.findById(characterDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", characterDTO.getWorldviewId()));
            character.setWorldview(worldview);
        }

        Character savedCharacter = characterRepository.save(character);
        return convertToDTO(savedCharacter);
    }

    /**
     * Update an existing character
     */
    @Transactional
    public CharacterDTO updateCharacter(UUID id, CharacterDTO characterDTO) {
        log.info("Updating character with id: {}", id);
        Character existingCharacter = characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));

        // Update basic fields
        existingCharacter.setName(characterDTO.getName());
        existingCharacter.setAge(characterDTO.getAge());
        existingCharacter.setAppearance(characterDTO.getAppearance());
        existingCharacter.setOccupation(characterDTO.getOccupation());
        existingCharacter.setPersonalityTraits(characterDTO.getPersonalityTraits());
        existingCharacter.setBackgroundStory(characterDTO.getBackgroundStory());
        existingCharacter.setChildhoodExperience(characterDTO.getChildhoodExperience());
        existingCharacter.setImportantExperiences(characterDTO.getImportantExperiences());
        existingCharacter.setValuesBeliefs(characterDTO.getValuesBeliefs());
        existingCharacter.setFears(characterDTO.getFears());
        existingCharacter.setDesires(characterDTO.getDesires());
        existingCharacter.setGoals(characterDTO.getGoals());
        existingCharacter.setSpeechPattern(characterDTO.getSpeechPattern());
        existingCharacter.setBehavioralHabits(characterDTO.getBehavioralHabits());
        existingCharacter.setCatchphrases(characterDTO.getCatchphrases());
        existingCharacter.setEmotionalState(characterDTO.getEmotionalState());
        existingCharacter.setRelationships(characterDTO.getRelationships());
        existingCharacter.setPersonalityVector(characterDTO.getPersonalityVector());
        existingCharacter.setCharacterSummary(characterDTO.getCharacterSummary());

        // Update worldview if provided
        if (characterDTO.getWorldviewId() != null) {
            Worldview worldview = worldviewRepository.findById(characterDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", characterDTO.getWorldviewId()));
            existingCharacter.setWorldview(worldview);
        }

        Character updatedCharacter = characterRepository.save(existingCharacter);
        return convertToDTO(updatedCharacter);
    }

    /**
     * Delete a character
     */
    @Transactional
    public void deleteCharacter(UUID id) {
        log.info("Deleting character with id: {}", id);
        if (!characterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Character", "id", id);
        }
        characterRepository.deleteById(id);
    }

    /**
     * Generate character using AI
     *
     * @param projectId 项目ID
     * @param keywords 关键词描述（如："一个勇敢的年轻剑客"）
     * @return 生成的角色DTO
     */
    @Transactional
    public CharacterDTO generateCharacterWithAI(UUID projectId, String keywords) {
        log.info("使用AI生成角色，项目: {}, 关键词: {}", projectId, keywords);


        // 验证项目存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // 准备模板变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", keywords);
        variables.put("genre", project.getGenre() != null ? project.getGenre() : "通用");

        // 如果项目有世界观，添加世界观上下文
        // TODO: 从数据库加载世界观详细信息
        variables.put("worldview_context", "现代都市背景");

        // 获取已有角色列表（避免重复）
        List<Character> existingCharacters = characterRepository.findByProjectId(projectId);
        String existingCharactersDesc = existingCharacters.isEmpty() ? "暂无"
                : existingCharacters.stream()
                .map(c -> c.getName() + "(" + (c.getOccupation() != null ? c.getOccupation() : "未知职业") + ")")
                .collect(Collectors.joining(", "));
        variables.put("existing_characters", existingCharactersDesc);

        try {
            // 调用AI生成
            String aiResponse = aiGenerationService.generateWithTemplate(
                    "character-creation",
                    variables
            );

            log.debug("AI生成的角色内容: {}", aiResponse);

            // 解析AI响应为CharacterDTO
            CharacterDTO characterDTO = parseAIResponseToDTO(aiResponse);
            characterDTO.setProjectId(projectId);

            // 保存到数据库
            return createCharacter(characterDTO);

        } catch (Exception e) {
            log.error("AI生成角色失败", e);
            throw new RuntimeException("AI生成角色失败: " + e.getMessage(), e);
        }
    }

    /**
     * Parse AI response to CharacterDTO
     * 支持多种格式：纯JSON、Markdown代码块包裹的JSON
     */
    private CharacterDTO parseAIResponseToDTO(String aiResponse) throws Exception {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("AI响应为空");
        }

        // 清理Markdown代码块标记
        String cleanJson = aiResponse.trim();
        if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(cleanJson);

            // 构建CharacterDTO
            CharacterDTO dto = new CharacterDTO();

            // 基础信息
            if (rootNode.has("basicInfo")) {
                JsonNode basicInfo = rootNode.get("basicInfo");
                dto.setName(getTextValue(basicInfo, "name", "姓名"));
                dto.setAge(getIntValue(basicInfo, "age", "年龄"));
                dto.setAppearance(getTextValue(basicInfo, "appearance", "外貌特征", "外貌"));
                dto.setOccupation(getTextValue(basicInfo, "occupation", "职业"));
            }

            // 性格特征
            if (rootNode.has("personality")) {
                JsonNode personality = rootNode.get("personality");
                dto.setPersonalityTraits(getListValue(personality, "traits", "核心性格", "性格特征"));
            }

            // 背景故事
            dto.setBackgroundStory(getTextValue(rootNode, "backstory", "background_story", "backgroundStory", "背景故事"));
            dto.setChildhoodExperience(getTextValue(rootNode, "childhood", "childhood_experience", "童年经历"));

            // 深层设定
            dto.setValuesBeliefs(getTextValue(rootNode, "values", "valuesBeliefs", "价值观"));
            dto.setFears(getListValue(rootNode, "fears", "恐惧", "内心恐惧"));
            dto.setDesires(getListValue(rootNode, "desires", "欲望", "欲望动机"));
            dto.setGoals(getListValue(rootNode, "goals", "目标", "人生目标"));

            // 行为特征
            if (rootNode.has("behaviorPatterns")) {
                JsonNode behavior = rootNode.get("behaviorPatterns");
                dto.setSpeechPattern(getTextValue(behavior, "speech", "speechPattern", "说话方式"));
                dto.setCatchphrases(getListValue(behavior, "catchphrases", "口癖"));
                dto.setBehavioralHabits(getListValue(behavior, "habits", "behavioralHabits", "行为习惯"));
            }

            // 生成角色概要
            dto.setCharacterSummary(generateCharacterSummary(dto));

            // 验证必填字段
            if (dto.getName() == null || dto.getName().isEmpty()) {
                throw new IllegalArgumentException("AI生成的角色缺少姓名字段");
            }

            return dto;

        } catch (Exception e) {
            log.error("解析AI响应失败，响应内容: {}", cleanJson);
            throw new RuntimeException("解析AI生成的角色数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: 从JsonNode中获取文本值，支持多个可能的字段名
     */
    private String getTextValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return node.get(fieldName).asText();
            }
        }
        return null;
    }

    /**
     * Helper: 从JsonNode中获取整数值
     */
    private Integer getIntValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return node.get(fieldName).asInt();
            }
        }
        return null;
    }

    /**
     * Helper: 从JsonNode中获取列表值
     */
    private List<String> getListValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && node.get(fieldName).isArray()) {
                List<String> result = new ArrayList<>();
                node.get(fieldName).forEach(item -> result.add(item.asText()));
                return result;
            }
        }
        return new ArrayList<>();
    }

    /**
     * 生成角色概要（用于AI prompt）
     */
    private String generateCharacterSummary(CharacterDTO dto) {
        StringBuilder summary = new StringBuilder();
        summary.append(dto.getName());

        if (dto.getAge() != null) {
            summary.append("，").append(dto.getAge()).append("岁");
        }

        if (dto.getOccupation() != null && !dto.getOccupation().isEmpty()) {
            summary.append("，").append(dto.getOccupation());
        }

        if (dto.getPersonalityTraits() != null && !dto.getPersonalityTraits().isEmpty()) {
            summary.append("。性格：").append(String.join("、", dto.getPersonalityTraits()));
        }

        return summary.toString();
    }

    // Conversion methods
    private CharacterDTO convertToDTO(Character character) {
        return CharacterDTO.builder()
                .id(character.getId())
                .projectId(character.getProject().getId())
                .worldviewId(character.getWorldview() != null ? character.getWorldview().getId() : null)
                .name(character.getName())
                .age(character.getAge())
                .appearance(character.getAppearance())
                .occupation(character.getOccupation())
                .personalityTraits(character.getPersonalityTraits())
                .backgroundStory(character.getBackgroundStory())
                .childhoodExperience(character.getChildhoodExperience())
                .importantExperiences(character.getImportantExperiences())
                .valuesBeliefs(character.getValuesBeliefs())
                .fears(character.getFears())
                .desires(character.getDesires())
                .goals(character.getGoals())
                .speechPattern(character.getSpeechPattern())
                .behavioralHabits(character.getBehavioralHabits())
                .catchphrases(character.getCatchphrases())
                .emotionalState(character.getEmotionalState())
                .relationships(character.getRelationships())
                .personalityVector(character.getPersonalityVector())
                .characterSummary(character.getCharacterSummary())
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .build();
    }

    private Character convertToEntity(CharacterDTO dto) {
        return Character.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .appearance(dto.getAppearance())
                .occupation(dto.getOccupation())
                .personalityTraits(dto.getPersonalityTraits())
                .backgroundStory(dto.getBackgroundStory())
                .childhoodExperience(dto.getChildhoodExperience())
                .importantExperiences(dto.getImportantExperiences())
                .valuesBeliefs(dto.getValuesBeliefs())
                .fears(dto.getFears())
                .desires(dto.getDesires())
                .goals(dto.getGoals())
                .speechPattern(dto.getSpeechPattern())
                .behavioralHabits(dto.getBehavioralHabits())
                .catchphrases(dto.getCatchphrases())
                .emotionalState(dto.getEmotionalState())
                .relationships(dto.getRelationships())
                .personalityVector(dto.getPersonalityVector())
                .characterSummary(dto.getCharacterSummary())
                .build();
    }
}
