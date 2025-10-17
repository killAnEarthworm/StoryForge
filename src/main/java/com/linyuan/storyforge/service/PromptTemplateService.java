package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.PromptTemplateDTO;
import com.linyuan.storyforge.entity.PromptTemplate;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing prompt templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository templateRepository;

    /**
     * Get all templates
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getAllTemplates() {
        log.debug("Fetching all prompt templates");
        return templateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get template by ID
     */
    @Transactional(readOnly = true)
    public PromptTemplateDTO getTemplateById(UUID id) {
        log.debug("Fetching template with id: {}", id);
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptTemplate", "id", id));
        return convertToDTO(template);
    }

    /**
     * Get templates by category
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getTemplatesByCategory(String category) {
        log.debug("Fetching templates with category: {}", category);
        return templateRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active templates
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getActiveTemplates() {
        log.debug("Fetching active templates");
        return templateRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active templates by category
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getActiveTemplatesByCategory(String category) {
        log.debug("Fetching active templates with category: {}", category);
        return templateRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get high-performing templates
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getHighPerformingTemplates(Float threshold) {
        log.debug("Fetching high-performing templates with threshold: {}", threshold);
        return templateRepository.findHighPerformingTemplates(threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search templates by name
     */
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> searchTemplatesByName(String name) {
        log.debug("Searching templates with name containing: {}", name);
        return templateRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new template
     */
    @Transactional
    public PromptTemplateDTO createTemplate(PromptTemplateDTO templateDTO) {
        log.info("Creating new prompt template: {}", templateDTO.getName());

        // Check for duplicate name
        if (templateRepository.existsByName(templateDTO.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDTO.getName() + "' already exists");
        }

        // Set default value for isActive if not provided
        if (templateDTO.getIsActive() == null) {
            templateDTO.setIsActive(true);
        }

        PromptTemplate template = convertToEntity(templateDTO);
        PromptTemplate savedTemplate = templateRepository.save(template);
        return convertToDTO(savedTemplate);
    }

    /**
     * Update an existing template
     */
    @Transactional
    public PromptTemplateDTO updateTemplate(UUID id, PromptTemplateDTO templateDTO) {
        log.info("Updating template with id: {}", id);
        PromptTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptTemplate", "id", id));

        // Check for duplicate name if name is being changed
        if (!existingTemplate.getName().equals(templateDTO.getName())) {
            if (templateRepository.existsByName(templateDTO.getName())) {
                throw new IllegalArgumentException("Template with name '" + templateDTO.getName() + "' already exists");
            }
        }

        // Update fields
        existingTemplate.setName(templateDTO.getName());
        existingTemplate.setCategory(templateDTO.getCategory());
        existingTemplate.setTemplateContent(templateDTO.getTemplateContent());
        existingTemplate.setRequiredVariables(templateDTO.getRequiredVariables());
        existingTemplate.setOptionalVariables(templateDTO.getOptionalVariables());
        existingTemplate.setExampleUsage(templateDTO.getExampleUsage());
        existingTemplate.setEffectivenessScore(templateDTO.getEffectivenessScore());
        existingTemplate.setIsActive(templateDTO.getIsActive());

        PromptTemplate updatedTemplate = templateRepository.save(existingTemplate);
        return convertToDTO(updatedTemplate);
    }

    /**
     * Delete a template
     */
    @Transactional
    public void deleteTemplate(UUID id) {
        log.info("Deleting template with id: {}", id);
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("PromptTemplate", "id", id);
        }
        templateRepository.deleteById(id);
    }

    /**
     * Activate a template
     */
    @Transactional
    public PromptTemplateDTO activateTemplate(UUID id) {
        log.info("Activating template with id: {}", id);
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptTemplate", "id", id));
        template.setIsActive(true);
        PromptTemplate updatedTemplate = templateRepository.save(template);
        return convertToDTO(updatedTemplate);
    }

    /**
     * Deactivate a template
     */
    @Transactional
    public PromptTemplateDTO deactivateTemplate(UUID id) {
        log.info("Deactivating template with id: {}", id);
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptTemplate", "id", id));
        template.setIsActive(false);
        PromptTemplate updatedTemplate = templateRepository.save(template);
        return convertToDTO(updatedTemplate);
    }

    /**
     * Count active templates
     */
    @Transactional(readOnly = true)
    public long countActiveTemplates() {
        log.debug("Counting active templates");
        return templateRepository.countByIsActiveTrue();
    }

    // Conversion methods
    private PromptTemplateDTO convertToDTO(PromptTemplate template) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setCategory(template.getCategory());
        dto.setTemplateContent(template.getTemplateContent());
        dto.setRequiredVariables(template.getRequiredVariables());
        dto.setOptionalVariables(template.getOptionalVariables());
        dto.setExampleUsage(template.getExampleUsage());
        dto.setEffectivenessScore(template.getEffectivenessScore());
        dto.setIsActive(template.getIsActive());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }

    private PromptTemplate convertToEntity(PromptTemplateDTO dto) {
        return PromptTemplate.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .templateContent(dto.getTemplateContent())
                .requiredVariables(dto.getRequiredVariables())
                .optionalVariables(dto.getOptionalVariables())
                .exampleUsage(dto.getExampleUsage())
                .effectivenessScore(dto.getEffectivenessScore())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
