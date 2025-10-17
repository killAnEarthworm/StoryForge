package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.WorldviewDTO;
import com.linyuan.storyforge.service.WorldviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Worldview management
 */
@Slf4j
@RestController
@RequestMapping("/api/worldviews")
@RequiredArgsConstructor
public class WorldviewController {

    private final WorldviewService worldviewService;

    /**
     * Get all worldviews
     */
    @GetMapping
    public ApiResponse<List<WorldviewDTO>> getAllWorldviews() {
        log.info("GET /api/worldviews - Fetching all worldviews");
        List<WorldviewDTO> worldviews = worldviewService.getAllWorldviews();
        return ApiResponse.success(worldviews, "Worldviews retrieved successfully");
    }

    /**
     * Get worldview by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<WorldviewDTO> getWorldviewById(@PathVariable UUID id) {
        log.info("GET /api/worldviews/{} - Fetching worldview", id);
        WorldviewDTO worldview = worldviewService.getWorldviewById(id);
        return ApiResponse.success(worldview, "Worldview retrieved successfully");
    }

    /**
     * Get worldviews by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<WorldviewDTO>> getWorldviewsByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/worldviews/project/{} - Fetching worldviews by project", projectId);
        List<WorldviewDTO> worldviews = worldviewService.getWorldviewsByProjectId(projectId);
        return ApiResponse.success(worldviews, "Worldviews retrieved successfully");
    }

    /**
     * Create a new worldview
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorldviewDTO> createWorldview(@Valid @RequestBody WorldviewDTO worldviewDTO) {
        log.info("POST /api/worldviews - Creating new worldview: {}", worldviewDTO.getName());
        WorldviewDTO createdWorldview = worldviewService.createWorldview(worldviewDTO);
        return ApiResponse.success(createdWorldview, "Worldview created successfully");
    }

    /**
     * Update an existing worldview
     */
    @PutMapping("/{id}")
    public ApiResponse<WorldviewDTO> updateWorldview(
            @PathVariable UUID id,
            @Valid @RequestBody WorldviewDTO worldviewDTO) {
        log.info("PUT /api/worldviews/{} - Updating worldview", id);
        WorldviewDTO updatedWorldview = worldviewService.updateWorldview(id, worldviewDTO);
        return ApiResponse.success(updatedWorldview, "Worldview updated successfully");
    }

    /**
     * Delete a worldview
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteWorldview(@PathVariable UUID id) {
        log.info("DELETE /api/worldviews/{} - Deleting worldview", id);
        worldviewService.deleteWorldview(id);
        return ApiResponse.success(null, "Worldview deleted successfully");
    }

    /**
     * Count worldviews by project ID
     */
    @GetMapping("/project/{projectId}/count")
    public ApiResponse<Long> countWorldviewsByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/worldviews/project/{}/count - Counting worldviews", projectId);
        long count = worldviewService.countWorldviewsByProjectId(projectId);
        return ApiResponse.success(count, "Worldview count retrieved successfully");
    }
}
