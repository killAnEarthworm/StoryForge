package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.GenerationHistoryDTO;
import com.linyuan.storyforge.entity.GenerationHistory;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.GenerationHistoryRepository;
import com.linyuan.storyforge.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing generation history
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationHistoryService {

    private final GenerationHistoryRepository historyRepository;
    private final ProjectRepository projectRepository;

    /**
     * Get all generation history
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getAllHistory() {
        log.debug("Fetching all generation history");
        return historyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get history by ID
     */
    @Transactional(readOnly = true)
    public GenerationHistoryDTO getHistoryById(UUID id) {
        log.debug("Fetching generation history with id: {}", id);
        GenerationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GenerationHistory", "id", id));
        return convertToDTO(history);
    }

    /**
     * Get history by project ID
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getHistoryByProjectId(UUID projectId) {
        log.debug("Fetching generation history for project: {}", projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return historyRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get history by generation type
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getHistoryByGenerationType(String generationType) {
        log.debug("Fetching generation history with type: {}", generationType);
        return historyRepository.findByGenerationTypeOrderByCreatedAtDesc(generationType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get history by target ID
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getHistoryByTargetId(UUID targetId) {
        log.debug("Fetching generation history for target: {}", targetId);
        return historyRepository.findByTargetIdOrderByCreatedAtDesc(targetId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get high-quality generations
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getHighQualityGenerations(Float threshold) {
        log.debug("Fetching high-quality generations with threshold: {}", threshold);
        return historyRepository.findHighQualityGenerations(threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get generations with user feedback
     */
    @Transactional(readOnly = true)
    public List<GenerationHistoryDTO> getGenerationsWithFeedback() {
        log.debug("Fetching generations with user feedback");
        return historyRepository.findGenerationsWithFeedback().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new generation history
     */
    @Transactional
    public GenerationHistoryDTO createHistory(GenerationHistoryDTO historyDTO) {
        log.info("Creating new generation history for project: {}", historyDTO.getProjectId());

        // Validate project exists
        Project project = projectRepository.findById(historyDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", historyDTO.getProjectId()));

        GenerationHistory history = convertToEntity(historyDTO);
        history.setProject(project);
        GenerationHistory savedHistory = historyRepository.save(history);
        return convertToDTO(savedHistory);
    }

    /**
     * Update existing generation history
     */
    @Transactional
    public GenerationHistoryDTO updateHistory(UUID id, GenerationHistoryDTO historyDTO) {
        log.info("Updating generation history with id: {}", id);
        GenerationHistory existingHistory = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GenerationHistory", "id", id));

        // Update fields
        existingHistory.setGenerationType(historyDTO.getGenerationType());
        existingHistory.setTargetId(historyDTO.getTargetId());
        existingHistory.setPromptTemplate(historyDTO.getPromptTemplate());
        existingHistory.setPromptVariables(historyDTO.getPromptVariables());
        existingHistory.setFullPrompt(historyDTO.getFullPrompt());
        existingHistory.setModelName(historyDTO.getModelName());
        existingHistory.setModelParameters(historyDTO.getModelParameters());
        existingHistory.setGeneratedResult(historyDTO.getGeneratedResult());
        existingHistory.setQualityScore(historyDTO.getQualityScore());
        existingHistory.setUserFeedback(historyDTO.getUserFeedback());

        GenerationHistory updatedHistory = historyRepository.save(existingHistory);
        return convertToDTO(updatedHistory);
    }

    /**
     * Delete generation history
     */
    @Transactional
    public void deleteHistory(UUID id) {
        log.info("Deleting generation history with id: {}", id);
        if (!historyRepository.existsById(id)) {
            throw new ResourceNotFoundException("GenerationHistory", "id", id);
        }
        historyRepository.deleteById(id);
    }

    /**
     * Get average quality score by generation type
     */
    @Transactional(readOnly = true)
    public Float getAverageQualityScoreByType(String generationType) {
        log.debug("Getting average quality score for type: {}", generationType);
        return historyRepository.getAverageQualityScoreByType(generationType);
    }

    /**
     * Count history by project ID
     */
    @Transactional(readOnly = true)
    public long countHistoryByProjectId(UUID projectId) {
        log.debug("Counting generation history for project: {}", projectId);
        return historyRepository.countByProjectId(projectId);
    }

    // Conversion methods
    private GenerationHistoryDTO convertToDTO(GenerationHistory history) {
        GenerationHistoryDTO dto = new GenerationHistoryDTO();
        dto.setId(history.getId());
        dto.setProjectId(history.getProject().getId());
        dto.setGenerationType(history.getGenerationType());
        dto.setTargetId(history.getTargetId());
        dto.setPromptTemplate(history.getPromptTemplate());
        dto.setPromptVariables(history.getPromptVariables());
        dto.setFullPrompt(history.getFullPrompt());
        dto.setModelName(history.getModelName());
        dto.setModelParameters(history.getModelParameters());
        dto.setGeneratedResult(history.getGeneratedResult());
        dto.setQualityScore(history.getQualityScore());
        dto.setUserFeedback(history.getUserFeedback());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUpdatedAt(history.getUpdatedAt());
        return dto;
    }

    private GenerationHistory convertToEntity(GenerationHistoryDTO dto) {
        return GenerationHistory.builder()
                .generationType(dto.getGenerationType())
                .targetId(dto.getTargetId())
                .promptTemplate(dto.getPromptTemplate())
                .promptVariables(dto.getPromptVariables())
                .fullPrompt(dto.getFullPrompt())
                .modelName(dto.getModelName())
                .modelParameters(dto.getModelParameters())
                .generatedResult(dto.getGeneratedResult())
                .qualityScore(dto.getQualityScore())
                .userFeedback(dto.getUserFeedback())
                .build();
    }
}
