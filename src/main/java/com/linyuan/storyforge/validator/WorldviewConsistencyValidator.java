package com.linyuan.storyforge.validator;

import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.WorldviewRepository;
import com.linyuan.storyforge.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WorldviewConsistencyValidator - 世界观一致性验证器
 * 验证生成的内容是否符合世界观设定
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WorldviewConsistencyValidator {

    private final WorldviewRepository worldviewRepository;
    private final AiGenerationService aiService;

    /**
     * 验证内容是否符合世界观设定
     *
     * @param worldviewId      世界观ID
     * @param generatedContent 生成的内容
     * @param contentType      内容类型
     * @return 一致性验证结果
     */
    public ConsistencyResult validateContent(
            UUID worldviewId,
            String generatedContent,
            ContentType contentType) {

        log.info("验证世界观 {} 的内容一致性, 类型: {}", worldviewId, contentType);

        Worldview worldview = worldviewRepository.findById(worldviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", worldviewId));

        ConsistencyResult result = ConsistencyResult.builder()
                .violations(new ArrayList<>())
                .build();

        // 1. 规则验证
        if (worldview.getRules() != null && !worldview.getRules().isEmpty()) {
            List<String> ruleViolations = checkRulesCompliance(
                    worldview.getRules(),
                    generatedContent
            );
            result.getViolations().addAll(ruleViolations);
            log.debug("规则验证: 发现 {} 个违规", ruleViolations.size());
        }

        // 2. 约束验证
        if (worldview.getConstraints() != null && !worldview.getConstraints().isEmpty()) {
            List<String> constraintViolations = checkConstraintsCompliance(
                    worldview.getConstraints(),
                    generatedContent
            );
            result.getViolations().addAll(constraintViolations);
            log.debug("约束验证: 发现 {} 个违规", constraintViolations.size());
        }

        // 3. 物理规则验证
        if (worldview.getUniverseLaws() != null) {
            boolean physicsValid = validatePhysicsCompliance(
                    worldview.getUniverseLaws(),
                    generatedContent
            );
            result.setSpeechPatternValid(physicsValid); // 复用字段
            if (!physicsValid) {
                result.addViolation("内容违反了世界观的物理规律设定");
            }
            log.debug("物理规则验证: {}", physicsValid);
        }

        // 4. 术语验证
        if (worldview.getTerminology() != null && !worldview.getTerminology().isEmpty()) {
            List<String> terminologyIssues = checkTerminologyUsage(
                    worldview.getTerminology(),
                    generatedContent
            );
            result.getViolations().addAll(terminologyIssues);
            log.debug("术语验证: 发现 {} 个问题", terminologyIssues.size());
        }

        // 5. 计算整体得分
        double overallScore = calculateOverallScore(result);
        result.setOverallScore(overallScore);
        result.setPassed(overallScore >= 0.7 && result.getViolations().isEmpty());

        // 6. AI深度验证（如果需要）
        if (result.needsDeepValidation()) {
            log.info("执行AI深度验证...");
            String aiValidation = performAIValidation(worldview, generatedContent, contentType);
            result.setAiSuggestions(aiValidation);
        }

        log.info("验证完成 - 得分: {}, 通过: {}, 违规数: {}",
                overallScore, result.getPassed(), result.getViolations().size());

        return result;
    }

    /**
     * 检查是否遵守世界观规则
     *
     * @param rules   规则列表
     * @param content 内容
     * @return 违规列表
     */
    private List<String> checkRulesCompliance(List<String> rules, String content) {
        List<String> violations = new ArrayList<>();

        String contentLower = content.toLowerCase();

        for (String rule : rules) {
            if (rule == null || rule.trim().isEmpty()) {
                continue;
            }

            String ruleLower = rule.toLowerCase();

            // 检查禁止性规则 (包含"禁止"、"不能"、"不允许")
            if (ruleLower.contains("禁止") || ruleLower.contains("不能") ||
                    ruleLower.contains("不允许") || ruleLower.contains("绝不")) {

                // 提取被禁止的行为/概念
                String forbidden = extractForbiddenConcept(rule);
                if (forbidden != null && contentLower.contains(forbidden.toLowerCase())) {
                    violations.add(String.format("违反规则: %s (内容中出现了被禁止的概念)", rule));
                }
            }

            // 检查必要性规则 (包含"必须"、"应该")
            if (ruleLower.contains("必须") || ruleLower.contains("应该")) {
                // 这种规则较难自动检测，记录为提示
                log.debug("发现必要性规则，建议人工审核: {}", rule);
            }
        }

        return violations;
    }

    /**
     * 检查是否违反约束
     *
     * @param constraints 约束列表
     * @param content     内容
     * @return 违规列表
     */
    private List<String> checkConstraintsCompliance(List<String> constraints, String content) {
        List<String> violations = new ArrayList<>();

        String contentLower = content.toLowerCase();

        for (String constraint : constraints) {
            if (constraint == null || constraint.trim().isEmpty()) {
                continue;
            }

            String constraintLower = constraint.toLowerCase();

            // 约束通常是禁止性的
            String forbidden = extractForbiddenConcept(constraint);
            if (forbidden != null && contentLower.contains(forbidden.toLowerCase())) {
                violations.add(String.format("违反约束: %s", constraint));
            }
        }

        return violations;
    }

    /**
     * 验证物理规律合规性
     *
     * @param universeLaws 宇宙法则
     * @param content      内容
     * @return true如果符合
     */
    private boolean validatePhysicsCompliance(Map<String, Object> universeLaws, String content) {
        // 简化实现：检查是否有明显违反物理设定的描述
        // 实际项目中可以更复杂

        if (universeLaws.containsKey("physics")) {
            String physicsRules = String.valueOf(universeLaws.get("physics"));

            // 如果设定中说明了特殊的物理规律，检查内容是否符合
            // 这里做基础检查
            return true; // 默认通过，复杂验证交给AI
        }

        return true;
    }

    /**
     * 检查术语使用
     *
     * @param terminology 专有名词词典
     * @param content     内容
     * @return 问题列表
     */
    private List<String> checkTerminologyUsage(Map<String, Object> terminology, String content) {
        List<String> issues = new ArrayList<>();

        // 提取内容中可能的专有名词（大写开头的词组）
        Pattern pattern = Pattern.compile("[A-Z\\p{Lu}][a-z\\p{Ll}]+(?:\\s+[A-Z\\p{Lu}][a-z\\p{Ll}]+)*");
        Matcher matcher = pattern.matcher(content);

        Set<String> potentialTerms = new HashSet<>();
        while (matcher.find()) {
            potentialTerms.add(matcher.group());
        }

        // 检查是否使用了未定义的专有名词
        for (String term : potentialTerms) {
            boolean defined = terminology.containsKey(term) ||
                    terminology.values().stream()
                            .anyMatch(v -> String.valueOf(v).contains(term));

            if (!defined && term.length() > 3) { // 只检查长度>3的词
                // 这里不算严重违规，只是提示
                log.debug("发现可能未定义的专有名词: {}", term);
            }
        }

        return issues;
    }

    /**
     * 提取被禁止的概念
     *
     * @param rule 规则描述
     * @return 被禁止的关键词
     */
    private String extractForbiddenConcept(String rule) {
        // 简单的提取逻辑
        String[] markers = {"禁止", "不能", "不允许", "绝不", "不得"};

        for (String marker : markers) {
            int index = rule.indexOf(marker);
            if (index >= 0) {
                // 提取标记后的内容
                String after = rule.substring(index + marker.length()).trim();
                // 取第一个词组（去掉标点符号）
                String[] words = after.split("[，。、,\\.\\s]+");
                if (words.length > 0) {
                    return words[0];
                }
            }
        }

        return null;
    }

    /**
     * 计算整体得分
     *
     * @param result 验证结果
     * @return 整体得分 (0.0-1.0)
     */
    private double calculateOverallScore(ConsistencyResult result) {
        double score = 1.0;

        // 每个违规扣0.15分
        score -= Math.min(0.6, result.getViolations().size() * 0.15);

        // 物理规则不符合扣0.2分
        if (!result.getSpeechPatternValid()) {
            score -= 0.2;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 使用AI进行深度验证
     *
     * @param worldview 世界观
     * @param content   内容
     * @param type      内容类型
     * @return AI验证建议
     */
    private String performAIValidation(Worldview worldview, String content, ContentType type) {
        try {
            String prompt = String.format("""
                            请验证以下内容是否符合世界观设定:

                            ## 世界观设定
                            - 名称: %s
                            - 概要: %s
                            - 核心规则: %s
                            - 约束条件: %s

                            ## 待验证内容
                            %s

                            ## 验证要求
                            1. 判断内容是否违反世界观规则
                            2. 检查是否有逻辑矛盾
                            3. 评估设定一致性
                            4. 如有问题，提供修改建议(不超过200字)

                            请简洁地输出验证结果和建议。
                            """,
                    worldview.getName(),
                    worldview.getSummary() != null ? worldview.getSummary() : "无概要",
                    worldview.getRules() != null ? worldview.getRules() : List.of("无规则"),
                    worldview.getConstraints() != null ? worldview.getConstraints() : List.of("无约束"),
                    content
            );

            return aiService.chatWithOptions(prompt, 0.3, 500);

        } catch (Exception e) {
            log.error("AI验证失败", e);
            return "AI验证暂时不可用";
        }
    }

    /**
     * 快速验证：只检查规则和约束
     *
     * @param worldviewId 世界观ID
     * @param content     内容
     * @return 是否通过基础验证
     */
    public boolean quickValidate(UUID worldviewId, String content) {
        Worldview worldview = worldviewRepository.findById(worldviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", worldviewId));

        List<String> violations = new ArrayList<>();

        if (worldview.getRules() != null) {
            violations.addAll(checkRulesCompliance(worldview.getRules(), content));
        }

        if (worldview.getConstraints() != null) {
            violations.addAll(checkConstraintsCompliance(worldview.getConstraints(), content));
        }

        return violations.isEmpty();
    }
}
