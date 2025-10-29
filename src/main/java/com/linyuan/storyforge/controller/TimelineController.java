package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.TimelineDTO;
import com.linyuan.storyforge.enums.TimelineEventType;
import com.linyuan.storyforge.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TimelineController - 时间线事件管理控制器
 * 提供时间线事件相关的REST API
 * 注意：一个项目有且仅有一条时间线，Timeline记录代表时间线上的事件
 *
 * @author StoryForge Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/timelines")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    /**
     * Get all timelines
     */
    @GetMapping
    public ApiResponse<List<TimelineDTO>> getAllTimelines() {
        log.info("GET /api/timelines - Fetching all timelines");
        List<TimelineDTO> timelines = timelineService.getAllTimelines();
        return ApiResponse.success(timelines, "Timelines retrieved successfully");
    }

    /**
     * Get timeline by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<TimelineDTO> getTimelineById(@PathVariable UUID id) {
        log.info("GET /api/timelines/{} - Fetching timeline", id);
        TimelineDTO timeline = timelineService.getTimelineById(id);
        return ApiResponse.success(timeline, "Timeline retrieved successfully");
    }

    /**
     * Get timelines by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<TimelineDTO>> getTimelinesByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/timelines/project/{} - Fetching timelines by project", projectId);
        List<TimelineDTO> timelines = timelineService.getTimelinesByProjectId(projectId);
        return ApiResponse.success(timelines, "Timelines retrieved successfully");
    }

    /**
     * Get timelines by character ID
     */
    @GetMapping("/character/{characterId}")
    public ApiResponse<List<TimelineDTO>> getTimelinesByCharacterId(@PathVariable UUID characterId) {
        log.info("GET /api/timelines/character/{} - Fetching timelines by character", characterId);
        List<TimelineDTO> timelines = timelineService.getTimelinesByCharacterId(characterId);
        return ApiResponse.success(timelines, "Timelines retrieved successfully");
    }

    /**
     * Get timelines by event type
     */
    @GetMapping("/event-type/{eventType}")
    public ApiResponse<List<TimelineDTO>> getTimelinesByEventType(@PathVariable String eventType) {
        log.info("GET /api/timelines/event-type/{} - Fetching timelines by event type", eventType);
        List<TimelineDTO> timelines = timelineService.getTimelinesByEventType(eventType);
        return ApiResponse.success(timelines, "Timelines retrieved successfully");
    }

    /**
     * Get timelines within a time range
     */
    @GetMapping("/time-range")
    public ApiResponse<List<TimelineDTO>> getTimelinesByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("GET /api/timelines/time-range - Fetching timelines between {} and {}", startTime, endTime);
        List<TimelineDTO> timelines = timelineService.getTimelinesByTimeRange(startTime, endTime);
        return ApiResponse.success(timelines, "Timelines retrieved successfully");
    }

    /**
     * Get important memories for a character
     */
    @GetMapping("/character/{characterId}/important")
    public ApiResponse<List<TimelineDTO>> getImportantMemories(
            @PathVariable UUID characterId,
            @RequestParam(defaultValue = "7") Integer threshold) {
        log.info("GET /api/timelines/character/{}/important - Fetching important memories", characterId);
        List<TimelineDTO> timelines = timelineService.getImportantMemories(characterId, threshold);
        return ApiResponse.success(timelines, "Important memories retrieved successfully");
    }

    /**
     * Create a new timeline event
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TimelineDTO> createTimeline(@Valid @RequestBody TimelineDTO timelineDTO) {
        log.info("POST /api/timelines - Creating new timeline event");
        TimelineDTO createdTimeline = timelineService.createTimeline(timelineDTO);
        return ApiResponse.success(createdTimeline, "Timeline created successfully");
    }

    /**
     * Update an existing timeline
     */
    @PutMapping("/{id}")
    public ApiResponse<TimelineDTO> updateTimeline(
            @PathVariable UUID id,
            @Valid @RequestBody TimelineDTO timelineDTO) {
        log.info("PUT /api/timelines/{} - Updating timeline", id);
        TimelineDTO updatedTimeline = timelineService.updateTimeline(id, timelineDTO);
        return ApiResponse.success(updatedTimeline, "Timeline updated successfully");
    }

    /**
     * Delete a timeline
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteTimeline(@PathVariable UUID id) {
        log.info("DELETE /api/timelines/{} - Deleting timeline", id);
        timelineService.deleteTimeline(id);
        return ApiResponse.success(null, "Timeline deleted successfully");
    }

    /**
     * Count timelines by character ID
     */
    @GetMapping("/character/{characterId}/count")
    public ApiResponse<Long> countTimelinesByCharacterId(@PathVariable UUID characterId) {
        log.info("GET /api/timelines/character/{}/count - Counting timelines", characterId);
        long count = timelineService.countTimelinesByCharacterId(characterId);
        return ApiResponse.success(count, "Timeline count retrieved successfully");
    }

    // ==================== 新增：项目时间线管理 ====================

    /**
     * 获取项目完整时间线
     * GET /api/timelines/project/{projectId}/complete
     * 返回项目的所有事件，按时间排序
     *
     * @param projectId 项目ID
     * @return 完整的项目时间线事件列表
     */
    @GetMapping("/project/{projectId}/complete")
    public ApiResponse<List<TimelineDTO>> getCompleteProjectTimeline(@PathVariable UUID projectId) {
        log.info("GET /api/timelines/project/{}/complete - 获取完整项目时间线", projectId);
        List<TimelineDTO> timeline = timelineService.getCompleteProjectTimeline(projectId);
        return ApiResponse.success(timeline,
                String.format("成功获取项目时间线，共 %d 个事件", timeline.size()));
    }

    /**
     * 获取项目关键事件
     * GET /api/timelines/project/{projectId}/key-events?threshold=7
     * 返回高重要度的关键事件
     *
     * @param projectId 项目ID
     * @param threshold 重要度阈值（默认7）
     * @return 关键事件列表
     */
    @GetMapping("/project/{projectId}/key-events")
    public ApiResponse<List<TimelineDTO>> getKeyEvents(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "7") Integer threshold) {
        log.info("GET /api/timelines/project/{}/key-events - 获取关键事件，阈值: {}", projectId, threshold);
        List<TimelineDTO> keyEvents = timelineService.getKeyEvents(projectId, threshold);
        return ApiResponse.success(keyEvents,
                String.format("成功获取 %d 个关键事件", keyEvents.size()));
    }

    /**
     * 按事件类型获取项目事件
     * GET /api/timelines/project/{projectId}/by-type/{eventType}
     *
     * @param projectId 项目ID
     * @param eventType 事件类型
     * @return 指定类型的事件列表
     */
    @GetMapping("/project/{projectId}/by-type/{eventType}")
    public ApiResponse<List<TimelineDTO>> getEventsByType(
            @PathVariable UUID projectId,
            @PathVariable String eventType) {
        log.info("GET /api/timelines/project/{}/by-type/{} - 获取特定类型事件", projectId, eventType);
        List<TimelineDTO> events = timelineService.getEventsByType(projectId, eventType);
        return ApiResponse.success(events,
                String.format("成功获取 %d 个 [%s] 类型事件", events.size(), eventType));
    }

    /**
     * 批量创建时间线事件
     * POST /api/timelines/batch
     *
     * @param timelineDTOs 时间线事件列表
     * @return 创建的事件列表
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<TimelineDTO>> batchCreateTimelines(
            @Valid @RequestBody List<TimelineDTO> timelineDTOs) {
        log.info("POST /api/timelines/batch - 批量创建 {} 个时间线事件", timelineDTOs.size());

        if (timelineDTOs.isEmpty()) {
            return ApiResponse.error(400, "事件列表不能为空");
        }

        if (timelineDTOs.size() > 50) {
            return ApiResponse.error(400, "单次批量创建不能超过50个事件");
        }

        try {
            List<TimelineDTO> createdEvents = timelineService.batchCreateTimelines(timelineDTOs);
            return ApiResponse.success(createdEvents,
                    String.format("成功批量创建 %d 个时间线事件", createdEvents.size()));
        } catch (Exception e) {
            log.error("批量创建时间线事件失败", e);
            return ApiResponse.error(500, "批量创建失败: " + e.getMessage());
        }
    }

    /**
     * 从场景描述中添加事件
     * POST /api/timelines/from-scene
     *
     * @param request 包含场景信息的请求体
     * @return 创建的时间线事件
     */
    @PostMapping("/from-scene")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TimelineDTO> addEventFromScene(@RequestBody Map<String, Object> request) {
        log.info("POST /api/timelines/from-scene - 从场景添加事件");

        try {
            UUID projectId = UUID.fromString((String) request.get("projectId"));
            UUID characterId = UUID.fromString((String) request.get("characterId"));
            String sceneDescription = (String) request.get("sceneDescription");
            String eventType = (String) request.get("eventType");
            Integer memoryImportance = (Integer) request.getOrDefault("memoryImportance", 5);

            if (sceneDescription == null || sceneDescription.isBlank()) {
                return ApiResponse.error(400, "场景描述不能为空");
            }

            TimelineDTO timelineEvent = timelineService.addEventFromScene(
                    projectId, characterId, sceneDescription, eventType, memoryImportance);

            return ApiResponse.success(timelineEvent, "成功从场景添加时间线事件");
        } catch (IllegalArgumentException e) {
            log.error("参数错误", e);
            return ApiResponse.error(400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("从场景添加事件失败", e);
            return ApiResponse.error(500, "添加事件失败: " + e.getMessage());
        }
    }

    // ==================== 辅助端点 ====================

    /**
     * 获取所有事件类型
     * GET /api/timelines/event-types
     *
     * @return 事件类型列表
     */
    @GetMapping("/event-types")
    public ApiResponse<List<Map<String, Object>>> getEventTypes() {
        log.info("GET /api/timelines/event-types - 获取事件类型列表");

        List<Map<String, Object>> types = new ArrayList<>();
        for (TimelineEventType type : TimelineEventType.values()) {
            Map<String, Object> typeInfo = new LinkedHashMap<>();
            typeInfo.put("code", type.getCode());
            typeInfo.put("displayName", type.getDisplayName());
            typeInfo.put("description", type.getDescription());
            typeInfo.put("isKeyEvent", type.isKeyEvent());
            typeInfo.put("isCharacterEvent", type.isCharacterEvent());
            typeInfo.put("isPlotEvent", type.isPlotEvent());
            typeInfo.put("recommendedImportance", type.getRecommendedImportance());
            types.add(typeInfo);
        }

        return ApiResponse.success(types, "成功获取事件类型列表");
    }

    /**
     * 获取特定事件类型的详细信息
     * GET /api/timelines/event-types/{code}
     *
     * @param code 事件类型代码
     * @return 事件类型详细信息
     */
    @GetMapping("/event-types/{code}")
    public ApiResponse<Map<String, Object>> getEventTypeInfo(@PathVariable String code) {
        log.info("GET /api/timelines/event-types/{} - 获取事件类型详情", code);

        try {
            TimelineEventType type = TimelineEventType.fromCode(code);
            Map<String, Object> typeInfo = new LinkedHashMap<>();
            typeInfo.put("code", type.getCode());
            typeInfo.put("displayName", type.getDisplayName());
            typeInfo.put("description", type.getDescription());
            typeInfo.put("isKeyEvent", type.isKeyEvent());
            typeInfo.put("isCharacterEvent", type.isCharacterEvent());
            typeInfo.put("isPlotEvent", type.isPlotEvent());
            typeInfo.put("recommendedImportance", type.getRecommendedImportance());

            // 分类标签
            List<String> categories = new ArrayList<>();
            if (type.isKeyEvent()) categories.add("关键事件");
            if (type.isCharacterEvent()) categories.add("角色事件");
            if (type.isPlotEvent()) categories.add("情节事件");
            typeInfo.put("categories", categories);

            return ApiResponse.success(typeInfo, "成功获取事件类型详情");
        } catch (IllegalArgumentException e) {
            log.warn("未找到事件类型: {}", code);
            return ApiResponse.error(404, "未找到事件类型: " + code);
        }
    }

    /**
     * 获取事件创建建议
     * GET /api/timelines/suggestions?eventType=turning_point
     *
     * @param eventType 事件类型代码（可选）
     * @return 创建建议
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getCreationSuggestions(
            @RequestParam(required = false) String eventType) {
        log.info("GET /api/timelines/suggestions - 获取事件创建建议, 类型: {}", eventType);

        Map<String, Object> suggestions = new HashMap<>();

        if (eventType != null) {
            try {
                TimelineEventType type = TimelineEventType.fromCode(eventType);
                suggestions.put("eventType", type.getDisplayName());
                suggestions.put("description", type.getDescription());
                suggestions.put("recommendedImportance", type.getRecommendedImportance());

                // 根据类型提供建议
                List<String> tips = new ArrayList<>();
                if (type.isKeyEvent()) {
                    tips.add("这是关键事件，建议设置较高的记忆重要度（7-10）");
                    tips.add("关键事件应该对故事发展产生重大影响");
                }
                if (type.isCharacterEvent()) {
                    tips.add("角色事件应该详细记录情绪变化和成长变化");
                    tips.add("建议在emotionalChanges和characterGrowth字段中添加详细信息");
                }
                if (type.isPlotEvent()) {
                    tips.add("情节推进事件应该记录因果关系");
                    tips.add("建议在consequences字段中说明对后续情节的影响");
                }
                suggestions.put("tips", tips);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error(400, "无效的事件类型: " + eventType);
            }
        }

        // 通用建议
        suggestions.put("generalTips", List.of(
                "关键事件（转折、高潮、危机等）重要度建议8-10",
                "角色成长、内心变化等事件重要度建议4-7",
                "日常对话和行动重要度建议1-3",
                "记得填写参与角色ID列表（participatingCharacters）",
                "如果有场景关联，填写locationId字段",
                "相对时间描述（如'三年前'）比绝对时间更灵活"
        ));

        return ApiResponse.success(suggestions, "成功获取创建建议");
    }
}
