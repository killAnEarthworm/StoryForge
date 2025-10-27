package com.linyuan.storyforge.service;

import com.linyuan.storyforge.entity.CharacterMemory;
import com.linyuan.storyforge.repository.CharacterMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CharacterMemoryEnhancedService - 增强的角色记忆服务
 * 实现智能记忆检索、相关性计算和遗忘曲线管理
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterMemoryEnhancedService {

    private final CharacterMemoryRepository memoryRepository;

    /**
     * 智能记忆检索
     * 根据场景上下文返回最相关的记忆
     *
     * @param characterId    角色ID
     * @param sceneContext   场景上下文描述
     * @param currentEmotion 当前情绪状态
     * @param maxResults     最大返回数量
     * @return 相关记忆列表,按相关性排序
     */
    @Transactional
    public List<CharacterMemory> retrieveRelevantMemories(
            UUID characterId,
            String sceneContext,
            String currentEmotion,
            int maxResults) {

        log.info("智能检索记忆 - 角色: {}, 场景: {}, 情绪: {}, 数量: {}",
                characterId, sceneContext, currentEmotion, maxResults);

        // 1. 获取所有可访问的记忆 (可访问性 > 0.3)
        List<CharacterMemory> allMemories = memoryRepository
                .findByCharacterId(characterId).stream()
                .filter(m -> m.getAccessibility() != null && m.getAccessibility() > 0.3f)
                .collect(Collectors.toList());

        if (allMemories.isEmpty()) {
            log.warn("角色 {} 没有可访问的记忆", characterId);
            return Collections.emptyList();
        }

        // 2. 更新所有记忆的遗忘曲线
        allMemories.forEach(memory -> {
            if (memory.getLastAccessed() != null) {
                memory.updateAccessibility();
            }
        });

        // 3. 计算每个记忆的相关性分数
        Map<CharacterMemory, Double> relevanceScores = new HashMap<>();
        for (CharacterMemory memory : allMemories) {
            double score = calculateRelevanceScore(memory, sceneContext, currentEmotion);
            relevanceScores.put(memory, score);
        }

        // 4. 按相关性排序并取前N个
        List<CharacterMemory> relevantMemories = relevanceScores.entrySet().stream()
                .sorted(Map.Entry.<CharacterMemory, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5. 更新被检索记忆的访问记录
        relevantMemories.forEach(memory -> {
            memory.recordAccess();
            memoryRepository.save(memory);
        });

        log.info("成功检索到 {} 条相关记忆", relevantMemories.size());
        return relevantMemories;
    }

    /**
     * 计算记忆的相关性得分
     * 综合考虑关键词匹配、情感共鸣、可访问性和重要性
     *
     * @param memory         记忆对象
     * @param sceneContext   场景上下文
     * @param currentEmotion 当前情绪
     * @return 相关性得分 (0.0-1.0)
     */
    private double calculateRelevanceScore(
            CharacterMemory memory,
            String sceneContext,
            String currentEmotion) {

        double score = 0.0;

        // 1. 关键词匹配度 (30%)
        if (memory.getKeywords() != null && !memory.getKeywords().isEmpty() && sceneContext != null) {
            double keywordMatch = calculateKeywordMatch(memory.getKeywords(), sceneContext);
            score += keywordMatch * 0.3;
        }

        // 2. 情感共鸣度 (20%)
        if (currentEmotion != null && memory.getMemoryType() != null) {
            String memoryTypeStr = memory.getMemoryType();
            if ("EMOTIONAL".equalsIgnoreCase(memoryTypeStr) || "情感记忆".equals(memoryTypeStr)) {
                Float emotionalWeight = memory.getEmotionalWeight();
                if (emotionalWeight != null) {
                    score += emotionalWeight * 0.2;
                }
            }
        }

        // 3. 记忆可访问性 (30%)
        Float accessibility = memory.getAccessibility();
        if (accessibility != null) {
            score += accessibility * 0.3;
        }

        // 4. 记忆重要性权重 (20%)
        Float emotionalWeight = memory.getEmotionalWeight();
        if (emotionalWeight != null) {
            score += emotionalWeight * 0.2;
        }

        return Math.min(1.0, score);
    }

    /**
     * 计算关键词匹配度
     * 使用简单的词频统计方法
     *
     * @param keywords     关键词列表
     * @param sceneContext 场景上下文
     * @return 匹配度 (0.0-1.0)
     */
    private double calculateKeywordMatch(List<String> keywords, String sceneContext) {
        if (keywords == null || keywords.isEmpty() || sceneContext == null || sceneContext.trim().isEmpty()) {
            return 0.0;
        }

        String contextLower = sceneContext.toLowerCase();
        long matchCount = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .filter(keyword -> contextLower.contains(keyword.toLowerCase()))
                .count();

        return (double) matchCount / keywords.size();
    }

    /**
     * 批量更新角色所有记忆的可访问性
     * 应用遗忘曲线算法
     *
     * @param characterId 角色ID
     * @return 更新的记忆数量
     */
    @Transactional
    public int updateMemoriesAccessibility(UUID characterId) {
        log.info("更新角色 {} 的所有记忆可访问性", characterId);

        List<CharacterMemory> memories = memoryRepository.findByCharacterId(characterId);

        memories.forEach(memory -> {
            if (memory.getLastAccessed() != null) {
                memory.updateAccessibility();
                memoryRepository.save(memory);
            }
        });

        log.info("成功更新 {} 条记忆", memories.size());
        return memories.size();
    }

    /**
     * 获取角色最重要的记忆
     * 基于记忆类型、情感权重和可访问性综合评分
     *
     * @param characterId 角色ID
     * @param limit       返回数量
     * @return 最重要的记忆列表
     */
    @Transactional(readOnly = true)
    public List<CharacterMemory> getTopImportantMemories(UUID characterId, int limit) {
        log.info("获取角色 {} 最重要的 {} 条记忆", characterId, limit);

        List<CharacterMemory> allMemories = memoryRepository.findByCharacterId(characterId);

        return allMemories.stream()
                .sorted((m1, m2) -> {
                    double score1 = calculateImportanceScore(m1);
                    double score2 = calculateImportanceScore(m2);
                    return Double.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 计算记忆的重要性得分
     *
     * @param memory 记忆对象
     * @return 重要性得分
     */
    private double calculateImportanceScore(CharacterMemory memory) {
        double score = 0.0;

        // 记忆类型权重
        String memoryType = memory.getMemoryType();
        if (memoryType != null) {
            switch (memoryType.toUpperCase()) {
                case "CORE":
                case "核心记忆":
                    score += 1.0 * 0.4;
                    break;
                case "EMOTIONAL":
                case "情感记忆":
                    score += 0.9 * 0.4;
                    break;
                case "SKILL":
                case "技能记忆":
                    score += 0.7 * 0.4;
                    break;
                case "EPISODIC":
                case "情节记忆":
                    score += 0.6 * 0.4;
                    break;
                case "SEMANTIC":
                case "语义记忆":
                    score += 0.5 * 0.4;
                    break;
                default:
                    score += 0.5 * 0.4;
            }
        }

        // 情感权重 (30%)
        Float emotionalWeight = memory.getEmotionalWeight();
        if (emotionalWeight != null) {
            score += emotionalWeight * 0.3;
        }

        // 可访问性 (30%)
        Float accessibility = memory.getAccessibility();
        if (accessibility != null) {
            score += accessibility * 0.3;
        }

        return score;
    }

    /**
     * 获取与特定角色相关的记忆
     *
     * @param characterId        当前角色ID
     * @param relatedCharacterId 相关角色ID
     * @return 相关记忆列表
     */
    @Transactional(readOnly = true)
    public List<CharacterMemory> getMemoriesAboutCharacter(
            UUID characterId,
            UUID relatedCharacterId) {

        log.info("获取角色 {} 关于角色 {} 的记忆", characterId, relatedCharacterId);

        return memoryRepository.findByCharacterId(characterId).stream()
                .filter(memory -> memory.getRelatedCharacters() != null &&
                        memory.getRelatedCharacters().contains(relatedCharacterId))
                .sorted((m1, m2) -> {
                    Float a1 = m1.getAccessibility();
                    Float a2 = m2.getAccessibility();
                    if (a1 == null) a1 = 0f;
                    if (a2 == null) a2 = 0f;
                    return Float.compare(a2, a1);
                })
                .collect(Collectors.toList());
    }

    /**
     * 查找包含特定关键词的记忆
     *
     * @param characterId 角色ID
     * @param keyword     关键词
     * @return 包含该关键词的记忆列表
     */
    @Transactional(readOnly = true)
    public List<CharacterMemory> findMemoriesByKeyword(UUID characterId, String keyword) {
        log.info("查找角色 {} 包含关键词 '{}' 的记忆", characterId, keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String keywordLower = keyword.toLowerCase();

        return memoryRepository.findByCharacterId(characterId).stream()
                .filter(memory -> {
                    if (memory.getKeywords() != null) {
                        return memory.getKeywords().stream()
                                .anyMatch(k -> k != null && k.toLowerCase().contains(keywordLower));
                    }
                    // 如果没有关键词,搜索记忆内容
                    return memory.getMemoryContent() != null &&
                            memory.getMemoryContent().toLowerCase().contains(keywordLower);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取记忆统计信息
     *
     * @param characterId 角色ID
     * @return 统计信息Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMemoryStatistics(UUID characterId) {
        log.info("获取角色 {} 的记忆统计信息", characterId);

        List<CharacterMemory> memories = memoryRepository.findByCharacterId(characterId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", memories.size());

        // 按类型统计
        Map<String, Long> typeStats = memories.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getMemoryType() != null ? m.getMemoryType() : "UNKNOWN",
                        Collectors.counting()
                ));
        stats.put("byType", typeStats);

        // 可访问性统计
        long accessibleCount = memories.stream()
                .filter(m -> m.getAccessibility() != null && m.getAccessibility() > 0.5f)
                .count();
        stats.put("accessibleCount", accessibleCount);

        // 平均情感权重
        double avgEmotionalWeight = memories.stream()
                .filter(m -> m.getEmotionalWeight() != null)
                .mapToDouble(m -> m.getEmotionalWeight())
                .average()
                .orElse(0.0);
        stats.put("avgEmotionalWeight", avgEmotionalWeight);

        // 平均访问次数
        double avgAccessCount = memories.stream()
                .filter(m -> m.getAccessCount() != null)
                .mapToDouble(m -> m.getAccessCount())
                .average()
                .orElse(0.0);
        stats.put("avgAccessCount", avgAccessCount);

        return stats;
    }
}
