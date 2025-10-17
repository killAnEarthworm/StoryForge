package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.GenerationHistoryDTO;
import com.linyuan.storyforge.service.GenerationHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Generation History management
 */
@Slf4j
@RestController
@RequestMapping("/api/generation-history")
@RequiredArgsConstructor
public class GenerationHistoryController {

    private final GenerationHistoryService historyService;

    /**
     * Get all generation history
     */
    @GetMapping
    public ApiResponse<List<GenerationHistoryDTO>> getAllHistory() {
        log.info("GET /api/generation-history - Fetching all history");
        List<GenerationHistoryDTO> history = historyService.getAllHistory();
        return ApiResponse.success(history, "Generation history retrieved successfully");
    }

    /**
     * Get history by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<GenerationHistoryDTO> getHistoryById(@PathVariable UUID id) {
        log.info("GET /api/generation-history/{} - Fetching history", id);
        GenerationHistoryDTO history = historyService.getHistoryById(id);
        return ApiResponse.success(history, "Generation history retrieved successfully");
    }

    /**
     * Get history by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<GenerationHistoryDTO>> getHistoryByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/generation-history/project/{} - Fetching history by project", projectId);
        List<GenerationHistoryDTO> history = historyService.getHistoryByProjectId(projectId);
        return ApiResponse.success(history, "Generation history retrieved successfully");
    }

    /**
     * Get history by generation type
     */
    @GetMapping("/type/{generationType}")
    public ApiResponse<List<GenerationHistoryDTO>> getHistoryByGenerationType(@PathVariable String generationType) {
        log.info("GET /api/generation-history/type/{} - Fetching history by type", generationType);
        List<GenerationHistoryDTO> history = historyService.getHistoryByGenerationType(generationType);
        return ApiResponse.success(history, "Generation history retrieved successfully");
    }

    /**
     * Get history by target ID
     */
    @GetMapping("/target/{targetId}")
    public ApiResponse<List<GenerationHistoryDTO>> getHistoryByTargetId(@PathVariable UUID targetId) {
        log.info("GET /api/generation-history/target/{} - Fetching history by target", targetId);
        List<GenerationHistoryDTO> history = historyService.getHistoryByTargetId(targetId);
        return ApiResponse.success(history, "Generation history retrieved successfully");
    }

    /**
     * Get high-quality generations
     */
    @GetMapping("/high-quality")
    public ApiResponse<List<GenerationHistoryDTO>> getHighQualityGenerations(
            @RequestParam(defaultValue = "0.7") Float threshold) {
        log.info("GET /api/generation-history/high-quality - Fetching high-quality generations");
        List<GenerationHistoryDTO> history = historyService.getHighQualityGenerations(threshold);
        return ApiResponse.success(history, "High-quality generations retrieved successfully");
    }

    /**
     * Get generations with user feedback
     */
    @GetMapping("/with-feedback")
    public ApiResponse<List<GenerationHistoryDTO>> getGenerationsWithFeedback() {
        log.info("GET /api/generation-history/with-feedback - Fetching generations with feedback");
        List<GenerationHistoryDTO> history = historyService.getGenerationsWithFeedback();
        return ApiResponse.success(history, "Generations with feedback retrieved successfully");
    }

    /**
     * Create new generation history
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GenerationHistoryDTO> createHistory(@Valid @RequestBody GenerationHistoryDTO historyDTO) {
        log.info("POST /api/generation-history - Creating new history");
        GenerationHistoryDTO createdHistory = historyService.createHistory(historyDTO);
        return ApiResponse.success(createdHistory, "Generation history created successfully");
    }

    /**
     * Update existing generation history
     */
    @PutMapping("/{id}")
    public ApiResponse<GenerationHistoryDTO> updateHistory(
            @PathVariable UUID id,
            @Valid @RequestBody GenerationHistoryDTO historyDTO) {
        log.info("PUT /api/generation-history/{} - Updating history", id);
        GenerationHistoryDTO updatedHistory = historyService.updateHistory(id, historyDTO);
        return ApiResponse.success(updatedHistory, "Generation history updated successfully");
    }

    /**
     * Delete generation history
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteHistory(@PathVariable UUID id) {
        log.info("DELETE /api/generation-history/{} - Deleting history", id);
        historyService.deleteHistory(id);
        return ApiResponse.success(null, "Generation history deleted successfully");
    }

    /**
     * Get average quality score by generation type
     */
    @GetMapping("/type/{generationType}/average-quality")
    public ApiResponse<Float> getAverageQualityScoreByType(@PathVariable String generationType) {
        log.info("GET /api/generation-history/type/{}/average-quality - Getting average quality score", generationType);
        Float avgScore = historyService.getAverageQualityScoreByType(generationType);
        return ApiResponse.success(avgScore, "Average quality score retrieved successfully");
    }

    /**
     * Count history by project ID
     */
    @GetMapping("/project/{projectId}/count")
    public ApiResponse<Long> countHistoryByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/generation-history/project/{}/count - Counting history", projectId);
        long count = historyService.countHistoryByProjectId(projectId);
        return ApiResponse.success(count, "History count retrieved successfully");
    }
}
