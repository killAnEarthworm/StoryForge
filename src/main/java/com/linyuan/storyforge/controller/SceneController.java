package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.SceneDTO;
import com.linyuan.storyforge.service.SceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Scene management
 */
@Slf4j
@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneController {

    private final SceneService sceneService;

    /**
     * Get all scenes
     */
    @GetMapping
    public ApiResponse<List<SceneDTO>> getAllScenes() {
        log.info("GET /api/scenes - Fetching all scenes");
        List<SceneDTO> scenes = sceneService.getAllScenes();
        return ApiResponse.success(scenes, "Scenes retrieved successfully");
    }

    /**
     * Get scene by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<SceneDTO> getSceneById(@PathVariable UUID id) {
        log.info("GET /api/scenes/{} - Fetching scene", id);
        SceneDTO scene = sceneService.getSceneById(id);
        return ApiResponse.success(scene, "Scene retrieved successfully");
    }

    /**
     * Get scenes by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<SceneDTO>> getScenesByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/scenes/project/{} - Fetching scenes by project", projectId);
        List<SceneDTO> scenes = sceneService.getScenesByProjectId(projectId);
        return ApiResponse.success(scenes, "Scenes retrieved successfully");
    }

    /**
     * Get scenes by worldview ID
     */
    @GetMapping("/worldview/{worldviewId}")
    public ApiResponse<List<SceneDTO>> getScenesByWorldviewId(@PathVariable UUID worldviewId) {
        log.info("GET /api/scenes/worldview/{} - Fetching scenes by worldview", worldviewId);
        List<SceneDTO> scenes = sceneService.getScenesByWorldviewId(worldviewId);
        return ApiResponse.success(scenes, "Scenes retrieved successfully");
    }

    /**
     * Get scenes by location type
     */
    @GetMapping("/location-type/{locationType}")
    public ApiResponse<List<SceneDTO>> getScenesByLocationType(@PathVariable String locationType) {
        log.info("GET /api/scenes/location-type/{} - Fetching scenes by location type", locationType);
        List<SceneDTO> scenes = sceneService.getScenesByLocationType(locationType);
        return ApiResponse.success(scenes, "Scenes retrieved successfully");
    }

    /**
     * Create a new scene
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SceneDTO> createScene(@Valid @RequestBody SceneDTO sceneDTO) {
        log.info("POST /api/scenes - Creating new scene: {}", sceneDTO.getName());
        SceneDTO createdScene = sceneService.createScene(sceneDTO);
        return ApiResponse.success(createdScene, "Scene created successfully");
    }

    /**
     * Update an existing scene
     */
    @PutMapping("/{id}")
    public ApiResponse<SceneDTO> updateScene(
            @PathVariable UUID id,
            @Valid @RequestBody SceneDTO sceneDTO) {
        log.info("PUT /api/scenes/{} - Updating scene", id);
        SceneDTO updatedScene = sceneService.updateScene(id, sceneDTO);
        return ApiResponse.success(updatedScene, "Scene updated successfully");
    }

    /**
     * Delete a scene
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteScene(@PathVariable UUID id) {
        log.info("DELETE /api/scenes/{} - Deleting scene", id);
        sceneService.deleteScene(id);
        return ApiResponse.success(null, "Scene deleted successfully");
    }

    /**
     * Count scenes by project ID
     */
    @GetMapping("/project/{projectId}/count")
    public ApiResponse<Long> countScenesByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/scenes/project/{}/count - Counting scenes", projectId);
        long count = sceneService.countScenesByProjectId(projectId);
        return ApiResponse.success(count, "Scene count retrieved successfully");
    }
}
