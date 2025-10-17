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
 * Worldview DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldviewDTO {

    private UUID id;

    @NotNull(message = "Project ID cannot be null")
    private UUID projectId;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    // 世界观层次结构
    private Map<String, Object> universeLaws; // 宇宙法则（物理定律、魔法系统等）
    private Map<String, Object> socialStructure; // 社会结构（政治、经济、文化）
    private Map<String, Object> geography; // 地理环境（地图、气候、资源）
    private Map<String, Object> historyBackground; // 历史背景（重大事件、传说）
    private Map<String, Object> terminology; // 专有名词词典

    // AI辅助字段
    private String summary; // 世界观概要，用于提示词
    private List<String> rules; // 世界规则列表
    private List<String> constraints; // 禁忌和限制

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
