package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Timeline DTO for API requests/responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDTO {

    private UUID id;

    @NotNull(message = "Project ID cannot be null")
    private UUID projectId;

    @NotNull(message = "Character ID cannot be null")
    private UUID characterId;

    private LocalDateTime eventTime; // 绝对时间
    private String relativeTime; // 相对时间描述（如"三年前"、"童年时期"）

    private String eventType; // 对话/行动/内心变化/环境变化
    private String eventDescription;

    // 事件详情
    private List<UUID> participatingCharacters; // 参与的其他角色ID
    private UUID locationId; // 关联场景
    private Map<String, Object> emotionalChanges; // 情绪变化

    @Min(value = 1, message = "Memory importance must be between 1 and 10")
    @Max(value = 10, message = "Memory importance must be between 1 and 10")
    private Integer memoryImportance; // 记忆重要度 1-10

    // 对后续影响
    private List<String> consequences;
    private Map<String, Object> characterGrowth; // 角色成长变化

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 获取事件简要描述（用于列表展示）
     *
     * @return 简要描述
     */
    public String getSimpleSummary() {
        String timeDesc = relativeTime != null ? relativeTime :
                (eventTime != null ? eventTime.toString() : "未知时间");
        String desc = eventDescription != null && eventDescription.length() > 50
                ? eventDescription.substring(0, 47) + "..."
                : eventDescription;
        return String.format("[%s] %s", timeDesc, desc);
    }
}
