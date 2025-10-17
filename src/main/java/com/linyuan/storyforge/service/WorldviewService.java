package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.WorldviewDTO;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.WorldviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing worldviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldviewService {

    private final WorldviewRepository worldviewRepository;
    private final ProjectRepository projectRepository;

    /**
     * Get all worldviews
     */
    @Transactional(readOnly = true)
    public List<WorldviewDTO> getAllWorldviews() {
        log.debug("Fetching all worldviews");
        return worldviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get worldview by ID
     */
    @Transactional(readOnly = true)
    public WorldviewDTO getWorldviewById(UUID id) {
        log.debug("Fetching worldview with id: {}", id);
        Worldview worldview = worldviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", id));
        return convertToDTO(worldview);
    }

    /**
     * Get worldviews by project ID
     */
    @Transactional(readOnly = true)
    public List<WorldviewDTO> getWorldviewsByProjectId(UUID projectId) {
        log.debug("Fetching worldviews for project: {}", projectId);
        // Validate project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return worldviewRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new worldview
     */
    @Transactional
    public WorldviewDTO createWorldview(WorldviewDTO worldviewDTO) {
        log.info("Creating new worldview: {}", worldviewDTO.getName());

        // Validate project exists
        Project project = projectRepository.findById(worldviewDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", worldviewDTO.getProjectId()));

        // Check for duplicate name in the same project
        if (worldviewRepository.existsByNameAndProjectId(worldviewDTO.getName(), worldviewDTO.getProjectId())) {
            throw new IllegalArgumentException("Worldview with name '" + worldviewDTO.getName() + "' already exists in this project");
        }

        Worldview worldview = convertToEntity(worldviewDTO);
        worldview.setProject(project);
        Worldview savedWorldview = worldviewRepository.save(worldview);
        return convertToDTO(savedWorldview);
    }

    /**
     * Update an existing worldview
     */
    @Transactional
    public WorldviewDTO updateWorldview(UUID id, WorldviewDTO worldviewDTO) {
        log.info("Updating worldview with id: {}", id);
        Worldview existingWorldview = worldviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", id));

        // Check for duplicate name if name is being changed
        if (!existingWorldview.getName().equals(worldviewDTO.getName())) {
            if (worldviewRepository.existsByNameAndProjectId(worldviewDTO.getName(), existingWorldview.getProject().getId())) {
                throw new IllegalArgumentException("Worldview with name '" + worldviewDTO.getName() + "' already exists in this project");
            }
        }

        // Update fields
        existingWorldview.setName(worldviewDTO.getName());
        existingWorldview.setUniverseLaws(worldviewDTO.getUniverseLaws());
        existingWorldview.setSocialStructure(worldviewDTO.getSocialStructure());
        existingWorldview.setGeography(worldviewDTO.getGeography());
        existingWorldview.setHistoryBackground(worldviewDTO.getHistoryBackground());
        existingWorldview.setTerminology(worldviewDTO.getTerminology());
        existingWorldview.setSummary(worldviewDTO.getSummary());
        existingWorldview.setRules(worldviewDTO.getRules());
        existingWorldview.setConstraints(worldviewDTO.getConstraints());

        Worldview updatedWorldview = worldviewRepository.save(existingWorldview);
        return convertToDTO(updatedWorldview);
    }

    /**
     * Delete a worldview
     */
    @Transactional
    public void deleteWorldview(UUID id) {
        log.info("Deleting worldview with id: {}", id);
        if (!worldviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Worldview", "id", id);
        }
        worldviewRepository.deleteById(id);
    }

    /**
     * Count worldviews by project ID
     */
    @Transactional(readOnly = true)
    public long countWorldviewsByProjectId(UUID projectId) {
        log.debug("Counting worldviews for project: {}", projectId);
        return worldviewRepository.countByProjectId(projectId);
    }

    // Conversion methods
    private WorldviewDTO convertToDTO(Worldview worldview) {
        WorldviewDTO dto = new WorldviewDTO();
        dto.setId(worldview.getId());
        dto.setProjectId(worldview.getProject().getId());
        dto.setName(worldview.getName());
        dto.setUniverseLaws(worldview.getUniverseLaws());
        dto.setSocialStructure(worldview.getSocialStructure());
        dto.setGeography(worldview.getGeography());
        dto.setHistoryBackground(worldview.getHistoryBackground());
        dto.setTerminology(worldview.getTerminology());
        dto.setSummary(worldview.getSummary());
        dto.setRules(worldview.getRules());
        dto.setConstraints(worldview.getConstraints());
        dto.setCreatedAt(worldview.getCreatedAt());
        dto.setUpdatedAt(worldview.getUpdatedAt());
        return dto;
    }

    private Worldview convertToEntity(WorldviewDTO dto) {
        return Worldview.builder()
                .name(dto.getName())
                .universeLaws(dto.getUniverseLaws())
                .socialStructure(dto.getSocialStructure())
                .geography(dto.getGeography())
                .historyBackground(dto.getHistoryBackground())
                .terminology(dto.getTerminology())
                .summary(dto.getSummary())
                .rules(dto.getRules())
                .constraints(dto.getConstraints())
                .build();
    }
}
