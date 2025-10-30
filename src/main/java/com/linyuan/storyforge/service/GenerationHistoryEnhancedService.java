package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.GenerationRequest;
import com.linyuan.storyforge.dto.GenerationResult;
import com.linyuan.storyforge.entity.GenerationHistory;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.GenerationHistoryRepository;
import com.linyuan.storyforge.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GenerationHistoryEnhancedService - 增强的生成历史服务
 * 专门用于Phase 4的章节生成历史记录和分析
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationHistoryEnhancedService {

    private final GenerationHistoryRepository historyRepository;
    private final ProjectRepository projectRepository;

    /**
     * 记录生成历史（从 GenerationResult）
     *
     * @param result          生成结果
     * @param request         生成请求
     * @param generatedContent 生成的内容
     * @param targetId        目标ID（章节ID、对话ID等）
     * @param generationType  生成类型
     * @return 生成历史ID
     */
    @Transactional
    public UUID recordGeneration(
            GenerationResult result,
            GenerationRequest request,
            String generatedContent,
            UUID targetId,
            String generationType) {

        log.info("记录生成历史 - 类型: {}, 目标: {}", generationType, targetId);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Map<String, Object> modelParameters = new HashMap<>();
        modelParameters.put("temperature", request.getTemperature());
        modelParameters.put("maxTokens", request.getMaxTokens());
        modelParameters.put("enableMemory", request.isEnableMemory());
        modelParameters.put("memoryCount", request.getMemoryCount());
        modelParameters.put("enableConsistencyCheck", request.isEnableConsistencyCheck());
        modelParameters.put("maxRetries", request.getMaxRetries());

        Map<String, Object> promptVariables = new HashMap<>();
        promptVariables.put("sceneContext", request.getSceneContext());
        promptVariables.put("emotionalTone", request.getEmotionalTone());
        promptVariables.put("generationGoal", request.getGenerationGoal());
        promptVariables.put("characterIds", request.getCharacterIds());
        promptVariables.put("worldviewId", request.getWorldviewId());

        Float qualityScore = calculateQualityScore(result);

        GenerationHistory history = GenerationHistory.builder()
                .project(project)
                .generationType(generationType)
                .targetId(targetId)
                .promptTemplate("built-in")
                .promptVariables(promptVariables)
                .fullPrompt(buildPromptSummary(request))
                .modelName("ERNIE-Speed-128K")
                .modelParameters(modelParameters)
                .generatedResult(generatedContent != null && generatedContent.length() > 500 ?
                        generatedContent.substring(0, 500) + "..." : generatedContent)
                .qualityScore(qualityScore)
                .build();

        GenerationHistory saved = historyRepository.save(history);
        log.info("生成历史记录成功: {} (质量得分: {})", saved.getId(), qualityScore);

        return saved.getId();
    }

    /**
     * 记录用户反馈
     */
    @Transactional
    public void recordFeedback(UUID historyId, String feedback, Float qualityScore) {
        log.info("记录用户反馈 - 历史ID: {}", historyId);

        GenerationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("GenerationHistory", "id", historyId));

        history.setUserFeedback(feedback);
        if (qualityScore != null) {
            history.setQualityScore(qualityScore);
        }

        historyRepository.save(history);
    }

    /**
     * 分析最佳参数
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeOptimalParameters(UUID projectId, String generationType) {
        log.info("分析最佳参数 - 项目: {}, 类型: {}", projectId, generationType);

        List<GenerationHistory> allHistories = historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        List<GenerationHistory> histories = allHistories.stream()
                .filter(h -> h.getGenerationType().equals(generationType))
                .collect(Collectors.toList());

        if (histories.isEmpty()) {
            return getDefaultParameters();
        }

        List<GenerationHistory> sortedByQuality = histories.stream()
                .filter(h -> h.getQualityScore() != null && h.getQualityScore() > 0)
                .sorted(Comparator.comparing(GenerationHistory::getQualityScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (sortedByQuality.isEmpty()) {
            return getDefaultParameters();
        }

        Map<String, Object> optimalParams = new HashMap<>();
        double avgTemperature = sortedByQuality.stream()
                .map(h -> h.getModelParameters().get("temperature"))
                .filter(Objects::nonNull)
                .mapToDouble(t -> ((Number) t).doubleValue())
                .average()
                .orElse(0.8);

        double avgMaxTokens = sortedByQuality.stream()
                .map(h -> h.getModelParameters().get("maxTokens"))
                .filter(Objects::nonNull)
                .mapToDouble(t -> ((Number) t).doubleValue())
                .average()
                .orElse(2000.0);

        optimalParams.put("temperature", Math.round(avgTemperature * 100.0) / 100.0);
        optimalParams.put("maxTokens", (int) avgMaxTokens);
        optimalParams.put("sampleSize", sortedByQuality.size());
        optimalParams.put("avgQualityScore", Math.round(sortedByQuality.stream()
                .mapToDouble(GenerationHistory::getQualityScore)
                .average()
                .orElse(0.0) * 100.0) / 100.0);

        return optimalParams;
    }

    /**
     * 获取统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectStatistics(UUID projectId) {
        List<GenerationHistory> histories = historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGenerations", histories.size());
        stats.put("byType", histories.stream()
                .collect(Collectors.groupingBy(GenerationHistory::getGenerationType, Collectors.counting())));
        stats.put("avgQualityScore", Math.round(histories.stream()
                .filter(h -> h.getQualityScore() != null)
                .mapToDouble(GenerationHistory::getQualityScore)
                .average()
                .orElse(0.0) * 100.0) / 100.0);
        stats.put("feedbackCount", histories.stream()
                .filter(h -> h.getUserFeedback() != null && !h.getUserFeedback().isEmpty())
                .count());

        return stats;
    }

    // 辅助方法
    private Float calculateQualityScore(GenerationResult result) {
        if (result == null || !result.isSuccess()) {
            return 0.0f;
        }

        float score = 50.0f;
        if (result.isPassedAllValidation()) {
            score += 30.0f;
        } else {
            score += (float) (result.getLowestConsistencyScore() * 30.0);
        }
        score -= result.getRetryCount() * 5.0f;
        if (result.getMemoriesUsed() > 0) score += 5.0f;
        if (result.getNewMemoryIds() != null && !result.getNewMemoryIds().isEmpty()) score += 5.0f;
        if (result.getDurationMs() != null && result.getDurationMs() < 5000) score += 5.0f;

        return Math.max(0.0f, Math.min(100.0f, score));
    }

    private String buildPromptSummary(GenerationRequest request) {
        StringBuilder summary = new StringBuilder();
        summary.append("类型: ").append(request.getContentType()).append("\n");
        if (request.getSceneContext() != null) {
            String context = request.getSceneContext();
            summary.append("场景: ").append(context.length() > 100 ? context.substring(0, 100) + "..." : context).append("\n");
        }
        if (request.getGenerationGoal() != null) {
            String goal = request.getGenerationGoal();
            summary.append("目标: ").append(goal.length() > 100 ? goal.substring(0, 100) + "..." : goal).append("\n");
        }
        return summary.toString();
    }

    private Map<String, Object> getDefaultParameters() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("temperature", 0.8);
        defaults.put("maxTokens", 2000);
        defaults.put("sampleSize", 0);
        defaults.put("note", "使用默认参数（无历史数据）");
        return defaults;
    }
}
