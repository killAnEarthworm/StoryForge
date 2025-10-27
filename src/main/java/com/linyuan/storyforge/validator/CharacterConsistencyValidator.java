package com.linyuan.storyforge.validator;

import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.enums.ContentType;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CharacterConsistencyValidator - 角色一致性验证器
 * 验证生成内容是否符合角色设定
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CharacterConsistencyValidator {

    private final CharacterRepository characterRepository;
    private final AiGenerationService aiService;

    /**
     * 验证生成内容是否符合角色设定
     *
     * @param characterId      角色ID
     * @param generatedContent 生成的内容
     * @param contentType      内容类型
     * @return 一致性验证结果
     */
    public ConsistencyResult validateContent(
            UUID characterId,
            String generatedContent,
            ContentType contentType) {

        log.info("验证角色 {} 的内容一致性, 类型: {}", characterId, contentType);

        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

        ConsistencyResult result = ConsistencyResult.builder()
                .violations(new ArrayList<>())
                .build();

        // 1. 性格向量验证
        if (character.getPersonalityVector() != null && !character.getPersonalityVector().isEmpty()) {
            double vectorScore = validatePersonalityVector(
                    character.getPersonalityVector(),
                    generatedContent
            );
            result.setVectorScore(vectorScore);
            log.debug("性格向量得分: {}", vectorScore);
        }

        // 2. 语言模式验证
        if (character.getSpeechPattern() != null && !character.getSpeechPattern().isEmpty()) {
            boolean speechValid = validateSpeechPattern(
                    character.getSpeechPattern(),
                    generatedContent,
                    contentType
            );
            result.setSpeechPatternValid(speechValid);
            if (!speechValid) {
                result.addViolation("内容的语言风格与角色设定的说话方式不符");
            }
            log.debug("语言模式验证: {}", speechValid);
        }

        // 3. 行为习惯验证
        if (character.getBehavioralHabits() != null && !character.getBehavioralHabits().isEmpty()) {
            List<String> behaviorViolations = checkBehaviorViolations(
                    character,
                    generatedContent
            );
            result.getViolations().addAll(behaviorViolations);
            result.setBehaviorPatternValid(behaviorViolations.isEmpty());
            log.debug("发现 {} 个行为习惯违规", behaviorViolations.size());
        }

        // 4. 性格特征验证
        if (character.getPersonalityTraits() != null && !character.getPersonalityTraits().isEmpty()) {
            List<String> traitViolations = checkPersonalityTraits(
                    character.getPersonalityTraits(),
                    generatedContent
            );
            result.getViolations().addAll(traitViolations);
            log.debug("发现 {} 个性格特征违规", traitViolations.size());
        }

        // 5. 计算整体得分
        double overallScore = calculateOverallScore(result);
        result.setOverallScore(overallScore);
        result.setPassed(overallScore >= 0.7 && result.getViolations().isEmpty());

        // 6. AI深度验证(如果需要)
        if (result.needsDeepValidation()) {
            log.info("执行AI深度验证...");
            String aiValidation = performAIValidation(character, generatedContent);
            result.setAiSuggestions(aiValidation);
        }

        log.info("验证完成 - 得分: {}, 通过: {}", overallScore, result.getPassed());
        return result;
    }

    /**
     * 验证性格向量
     * 简化实现:检查内容中是否体现了主要性格特征
     *
     * @param personalityVector 性格向量
     * @param content           内容
     * @return 匹配度得分 (0.0-1.0)
     */
    private double validatePersonalityVector(List<Float> personalityVector, String content) {
        // 简化实现:实际项目中应该使用向量相似度计算
        // 这里返回一个基于内容长度和复杂度的简单得分
        if (content == null || content.length() < 10) {
            return 0.5;
        }

        // 基础得分
        double score = 0.7;

        // 根据向量的极端值调整得分
        if (personalityVector != null && !personalityVector.isEmpty()) {
            // 检查是否有突出的性格特征(向量值>0.7或<0.3)
            boolean hasExtremeTraits = personalityVector.stream()
                    .anyMatch(v -> v > 0.7f || v < 0.3f);

            if (hasExtremeTraits) {
                // 内容应该更鲜明地体现性格
                score += 0.1;
            }
        }

        return Math.min(1.0, score);
    }

    /**
     * 验证说话方式
     *
     * @param speechPattern 说话方式描述
     * @param content       内容
     * @param contentType   内容类型
     * @return true如果匹配
     */
    private boolean validateSpeechPattern(
            String speechPattern,
            String content,
            ContentType contentType) {

        // 只对对话和内心独白类型进行严格检查
        if (contentType != ContentType.DIALOGUE &&
                contentType != ContentType.INNER_MONOLOGUE) {
            return true;
        }

        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 简化实现:检查一些基本的语言特征
        String patternLower = speechPattern.toLowerCase();
        String contentLower = content.toLowerCase();

        // 如果说话方式包含"正式",内容不应该有太多口语化表达
        if (patternLower.contains("正式") || patternLower.contains("formal")) {
            return !containsCasualExpressions(contentLower);
        }

        // 如果说话方式包含"粗鲁",应该有相应的表达
        if (patternLower.contains("粗鲁") || patternLower.contains("粗俗")) {
            return containsRoughExpressions(contentLower);
        }

        // 默认通过
        return true;
    }

    /**
     * 检查是否包含口语化表达
     */
    private boolean containsCasualExpressions(String content) {
        String[] casualWords = {"哎呀", "嘛", "呗", "啊", "哦", "呢", "吧"};
        for (String word : casualWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含粗鲁表达
     */
    private boolean containsRoughExpressions(String content) {
        String[] roughWords = {"混蛋", "该死", "他妈", "草"};
        for (String word : roughWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查行为习惯违规
     *
     * @param character 角色
     * @param content   内容
     * @return 违规列表
     */
    private List<String> checkBehaviorViolations(Character character, String content) {
        List<String> violations = new ArrayList<>();

        if (character.getBehavioralHabits() == null || content == null) {
            return violations;
        }

        // 检查是否违反明确的行为习惯
        for (String habit : character.getBehavioralHabits()) {
            if (habit == null || habit.trim().isEmpty()) {
                continue;
            }

            // 如果习惯描述是否定性的(从不/绝不),检查内容是否违反
            String habitLower = habit.toLowerCase();
            if (habitLower.contains("从不") || habitLower.contains("绝不") ||
                    habitLower.contains("never") || habitLower.contains("avoid")) {

                // 提取被禁止的行为
                String forbidden = extractForbiddenBehavior(habit);
                if (forbidden != null && content.toLowerCase().contains(forbidden.toLowerCase())) {
                    violations.add(String.format("内容中出现了角色明确避免的行为: %s", habit));
                }
            }
        }

        return violations;
    }

    /**
     * 提取被禁止的行为
     */
    private String extractForbiddenBehavior(String habit) {
        // 简化实现:提取"从不"或"绝不"后面的词
        if (habit.contains("从不")) {
            int index = habit.indexOf("从不");
            return habit.substring(index + 2).trim();
        }
        if (habit.contains("绝不")) {
            int index = habit.indexOf("绝不");
            return habit.substring(index + 2).trim();
        }
        return null;
    }

    /**
     * 检查性格特征违规
     *
     * @param traits  性格特征列表
     * @param content 内容
     * @return 违规列表
     */
    private List<String> checkPersonalityTraits(List<String> traits, String content) {
        List<String> violations = new ArrayList<>();

        if (traits == null || traits.isEmpty() || content == null) {
            return violations;
        }

        // 检查是否有明显矛盾的性格表现
        String contentLower = content.toLowerCase();

        for (String trait : traits) {
            if (trait == null) continue;
            String traitLower = trait.toLowerCase();

            // 勇敢的人不应表现得极度胆小
            if ((traitLower.contains("勇敢") || traitLower.contains("brave")) &&
                    (contentLower.contains("害怕") || contentLower.contains("恐惧") ||
                            contentLower.contains("不敢"))) {
                violations.add("勇敢的角色不应表现出过度胆怯");
            }

            // 谨慎的人不应表现得过于冲动
            if ((traitLower.contains("谨慎") || traitLower.contains("cautious")) &&
                    (contentLower.contains("立刻") || contentLower.contains("马上冲") ||
                            contentLower.contains("不管不顾"))) {
                violations.add("谨慎的角色不应表现得过于冲动");
            }

            // 温和的人不应有暴力倾向
            if ((traitLower.contains("温和") || traitLower.contains("gentle")) &&
                    (contentLower.contains("暴打") || contentLower.contains("狠狠") ||
                            contentLower.contains("揍"))) {
                violations.add("温和的角色不应表现暴力倾向");
            }
        }

        return violations;
    }

    /**
     * 计算整体得分
     *
     * @param result 验证结果
     * @return 整体得分 (0.0-1.0)
     */
    private double calculateOverallScore(ConsistencyResult result) {
        double score = 0.0;

        // 向量得分 (40%)
        score += result.getVectorScore() * 0.4;

        // 语言模式 (30%)
        score += (result.getSpeechPatternValid() ? 1.0 : 0.0) * 0.3;

        // 行为模式 (30%)
        score += (result.getBehaviorPatternValid() ? 1.0 : 0.0) * 0.3;

        // 违规惩罚:每个违规扣0.1分
        score -= Math.min(0.3, result.getViolations().size() * 0.1);

        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 使用AI进行深度验证
     *
     * @param character 角色
     * @param content   内容
     * @return AI验证建议
     */
    private String performAIValidation(Character character, String content) {
        try {
            String prompt = String.format("""
                            请验证以下内容是否符合角色设定:

                            ## 角色设定
                            - 姓名: %s
                            - 性格特征: %s
                            - 说话方式: %s
                            - 行为习惯: %s

                            ## 待验证内容
                            %s

                            ## 验证要求
                            1. 判断内容是否符合角色性格
                            2. 语言风格是否一致
                            3. 行为是否合理
                            4. 如有问题,提供简洁的修改建议(不超过200字)

                            请简洁地输出验证结果和建议。
                            """,
                    character.getName(),
                    character.getPersonalityTraits(),
                    character.getSpeechPattern() != null ? character.getSpeechPattern() : "无特殊要求",
                    character.getBehavioralHabits() != null ? character.getBehavioralHabits() : "无特殊要求",
                    content
            );

            return aiService.chatWithOptions(prompt, 0.3, 500);
        } catch (Exception e) {
            log.error("AI验证失败", e);
            return "AI验证暂时不可用";
        }
    }
}
