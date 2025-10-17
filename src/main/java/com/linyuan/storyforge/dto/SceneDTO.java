package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scene DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneDTO {

    private UUID id;

    @NotNull(message = "Project ID cannot be null")
    private UUID projectId;

    private UUID worldviewId;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String locationType; // 室内/室外/虚拟等

    // 场景设定
    private String physicalDescription;
    private String timeSetting; // 时间背景
    private String atmosphere; // 氛围/情绪基调
    private String weather;
    private String lighting;

    // 环境元素
    private Map<String, Object> availableProps; // 可用道具
    private List<String> environmentalElements; // 环境元素
    private Map<String, Object> sensoryDetails; // 感官细节（声音、气味等）

    // AI辅助
    private String sceneSummary; // 场景概要
    private List<String> moodKeywords; // 情绪关键词

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
