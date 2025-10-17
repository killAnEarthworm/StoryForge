package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.TimelineDTO;
import com.linyuan.storyforge.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Timeline management
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
}
