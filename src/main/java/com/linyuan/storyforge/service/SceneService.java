package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.SceneDTO;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Scene;
import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.SceneRepository;
import com.linyuan.storyforge.repository.WorldviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing scenes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;
    private final ProjectRepository projectRepository;
    private final WorldviewRepository worldviewRepository;

    /**
     * Get all scenes
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getAllScenes() {
        log.debug("Fetching all scenes");
        return sceneRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get scene by ID
     */
    @Transactional(readOnly = true)
    public SceneDTO getSceneById(UUID id) {
        log.debug("Fetching scene with id: {}", id);
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", id));
        return convertToDTO(scene);
    }

    /**
     * Get scenes by project ID
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByProjectId(UUID projectId) {
        log.debug("Fetching scenes for project: {}", projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return sceneRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get scenes by worldview ID
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByWorldviewId(UUID worldviewId) {
        log.debug("Fetching scenes for worldview: {}", worldviewId);
        if (!worldviewRepository.existsById(worldviewId)) {
            throw new ResourceNotFoundException("Worldview", "id", worldviewId);
        }
        return sceneRepository.findByWorldviewId(worldviewId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get scenes by location type
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByLocationType(String locationType) {
        log.debug("Fetching scenes with location type: {}", locationType);
        return sceneRepository.findByLocationType(locationType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new scene
     */
    @Transactional
    public SceneDTO createScene(SceneDTO sceneDTO) {
        log.info("Creating new scene: {}", sceneDTO.getName());

        // Validate project exists
        Project project = projectRepository.findById(sceneDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", sceneDTO.getProjectId()));

        // Validate worldview if provided
        Worldview worldview = null;
        if (sceneDTO.getWorldviewId() != null) {
            worldview = worldviewRepository.findById(sceneDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", sceneDTO.getWorldviewId()));
        }

        // Check for duplicate name in the same project
        if (sceneRepository.existsByNameAndProjectId(sceneDTO.getName(), sceneDTO.getProjectId())) {
            throw new IllegalArgumentException("Scene with name '" + sceneDTO.getName() + "' already exists in this project");
        }

        Scene scene = convertToEntity(sceneDTO);
        scene.setProject(project);
        scene.setWorldview(worldview);
        Scene savedScene = sceneRepository.save(scene);
        return convertToDTO(savedScene);
    }

    /**
     * Update an existing scene
     */
    @Transactional
    public SceneDTO updateScene(UUID id, SceneDTO sceneDTO) {
        log.info("Updating scene with id: {}", id);
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", id));

        // Check for duplicate name if name is being changed
        if (!existingScene.getName().equals(sceneDTO.getName())) {
            if (sceneRepository.existsByNameAndProjectId(sceneDTO.getName(), existingScene.getProject().getId())) {
                throw new IllegalArgumentException("Scene with name '" + sceneDTO.getName() + "' already exists in this project");
            }
        }

        // Update worldview if provided
        if (sceneDTO.getWorldviewId() != null &&
            (existingScene.getWorldview() == null || !existingScene.getWorldview().getId().equals(sceneDTO.getWorldviewId()))) {
            Worldview worldview = worldviewRepository.findById(sceneDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", sceneDTO.getWorldviewId()));
            existingScene.setWorldview(worldview);
        }

        // Update fields
        existingScene.setName(sceneDTO.getName());
        existingScene.setLocationType(sceneDTO.getLocationType());
        existingScene.setPhysicalDescription(sceneDTO.getPhysicalDescription());
        existingScene.setTimeSetting(sceneDTO.getTimeSetting());
        existingScene.setAtmosphere(sceneDTO.getAtmosphere());
        existingScene.setWeather(sceneDTO.getWeather());
        existingScene.setLighting(sceneDTO.getLighting());
        existingScene.setAvailableProps(sceneDTO.getAvailableProps());
        existingScene.setEnvironmentalElements(sceneDTO.getEnvironmentalElements());
        existingScene.setSensoryDetails(sceneDTO.getSensoryDetails());
        existingScene.setSceneSummary(sceneDTO.getSceneSummary());
        existingScene.setMoodKeywords(sceneDTO.getMoodKeywords());

        Scene updatedScene = sceneRepository.save(existingScene);
        return convertToDTO(updatedScene);
    }

    /**
     * Delete a scene
     */
    @Transactional
    public void deleteScene(UUID id) {
        log.info("Deleting scene with id: {}", id);
        if (!sceneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Scene", "id", id);
        }
        sceneRepository.deleteById(id);
    }

    /**
     * Count scenes by project ID
     */
    @Transactional(readOnly = true)
    public long countScenesByProjectId(UUID projectId) {
        log.debug("Counting scenes for project: {}", projectId);
        return sceneRepository.countByProjectId(projectId);
    }

    // Conversion methods
    private SceneDTO convertToDTO(Scene scene) {
        SceneDTO dto = new SceneDTO();
        dto.setId(scene.getId());
        dto.setProjectId(scene.getProject().getId());
        dto.setWorldviewId(scene.getWorldview() != null ? scene.getWorldview().getId() : null);
        dto.setName(scene.getName());
        dto.setLocationType(scene.getLocationType());
        dto.setPhysicalDescription(scene.getPhysicalDescription());
        dto.setTimeSetting(scene.getTimeSetting());
        dto.setAtmosphere(scene.getAtmosphere());
        dto.setWeather(scene.getWeather());
        dto.setLighting(scene.getLighting());
        dto.setAvailableProps(scene.getAvailableProps());
        dto.setEnvironmentalElements(scene.getEnvironmentalElements());
        dto.setSensoryDetails(scene.getSensoryDetails());
        dto.setSceneSummary(scene.getSceneSummary());
        dto.setMoodKeywords(scene.getMoodKeywords());
        dto.setCreatedAt(scene.getCreatedAt());
        dto.setUpdatedAt(scene.getUpdatedAt());
        return dto;
    }

    private Scene convertToEntity(SceneDTO dto) {
        return Scene.builder()
                .name(dto.getName())
                .locationType(dto.getLocationType())
                .physicalDescription(dto.getPhysicalDescription())
                .timeSetting(dto.getTimeSetting())
                .atmosphere(dto.getAtmosphere())
                .weather(dto.getWeather())
                .lighting(dto.getLighting())
                .availableProps(dto.getAvailableProps())
                .environmentalElements(dto.getEnvironmentalElements())
                .sensoryDetails(dto.getSensoryDetails())
                .sceneSummary(dto.getSceneSummary())
                .moodKeywords(dto.getMoodKeywords())
                .build();
    }
}
