package com.linyuan.storyforge.dto;

import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.CharacterMemory;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Worldview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GenerationContext - 生成上下文
 * 包含AI生成所需的完整上下文信息
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationContext {

    /**
     * 项目信息
     */
    private Project project;

    /**
     * 世界观设定
     */
    private Worldview worldview;

    /**
     * 参与的角色列表
     */
    private List<Character> characters;

    /**
     * 角色记忆映射表 (角色ID -> 相关记忆列表)
     */
    private Map<UUID, List<CharacterMemory>> characterMemories;

    /**
     * 场景描述/上下文
     */
    private String sceneContext;

    /**
     * 情感基调
     */
    private String emotionalTone;

    /**
     * 前文内容（用于章节生成）
     */
    private String previousContent;

    /**
     * 生成目标/指令
     */
    private String generationGoal;

    /**
     * 额外的提示词参数
     */
    private Map<String, Object> additionalParams;

    /**
     * 是否需要一致性验证
     */
    @Builder.Default
    private boolean requireConsistencyCheck = true;

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxRetries = 2;

    /**
     * 构建记忆上下文字符串（用于注入提示词）
     *
     * @param characterId 角色ID
     * @return 格式化的记忆上下文
     */
    public String buildMemoryContext(UUID characterId) {
        if (characterMemories == null || !characterMemories.containsKey(characterId)) {
            return "暂无相关记忆";
        }

        List<CharacterMemory> memories = characterMemories.get(characterId);
        if (memories.isEmpty()) {
            return "暂无相关记忆";
        }

        StringBuilder context = new StringBuilder();
        context.append("相关记忆：\n");
        for (int i = 0; i < memories.size(); i++) {
            CharacterMemory memory = memories.get(i);
            context.append(String.format("%d. [%s] %s\n",
                    i + 1,
                    memory.getMemoryType() != null ? memory.getMemoryType() : "记忆",
                    memory.getMemoryContent()
            ));
        }

        return context.toString();
    }

    /**
     * 获取主要角色（第一个角色）
     *
     * @return 主要角色，如果没有则返回null
     */
    public Character getPrimaryCharacter() {
        if (characters == null || characters.isEmpty()) {
            return null;
        }
        return characters.get(0);
    }

    /**
     * 检查是否有世界观设定
     *
     * @return true如果有世界观
     */
    public boolean hasWorldview() {
        return worldview != null;
    }

    /**
     * 检查是否有记忆数据
     *
     * @return true如果有记忆
     */
    public boolean hasMemories() {
        return characterMemories != null && !characterMemories.isEmpty();
    }
}
