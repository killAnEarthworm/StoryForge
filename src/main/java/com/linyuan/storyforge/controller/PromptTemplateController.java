package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.PromptTemplateDTO;
import com.linyuan.storyforge.service.PromptTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Prompt Template management
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    /**
     * Get all templates
     */
    @GetMapping
    public ApiResponse<List<PromptTemplateDTO>> getAllTemplates() {
        log.info("GET /api/prompt-templates - Fetching all templates");
        List<PromptTemplateDTO> templates = templateService.getAllTemplates();
        return ApiResponse.success(templates, "Templates retrieved successfully");
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateDTO> getTemplateById(@PathVariable UUID id) {
        log.info("GET /api/prompt-templates/{} - Fetching template", id);
        PromptTemplateDTO template = templateService.getTemplateById(id);
        return ApiResponse.success(template, "Template retrieved successfully");
    }

    /**
     * Get templates by category
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<PromptTemplateDTO>> getTemplatesByCategory(@PathVariable String category) {
        log.info("GET /api/prompt-templates/category/{} - Fetching templates by category", category);
        List<PromptTemplateDTO> templates = templateService.getTemplatesByCategory(category);
        return ApiResponse.success(templates, "Templates retrieved successfully");
    }

    /**
     * Get active templates
     */
    @GetMapping("/active")
    public ApiResponse<List<PromptTemplateDTO>> getActiveTemplates() {
        log.info("GET /api/prompt-templates/active - Fetching active templates");
        List<PromptTemplateDTO> templates = templateService.getActiveTemplates();
        return ApiResponse.success(templates, "Active templates retrieved successfully");
    }

    /**
     * Get active templates by category
     */
    @GetMapping("/active/category/{category}")
    public ApiResponse<List<PromptTemplateDTO>> getActiveTemplatesByCategory(@PathVariable String category) {
        log.info("GET /api/prompt-templates/active/category/{} - Fetching active templates", category);
        List<PromptTemplateDTO> templates = templateService.getActiveTemplatesByCategory(category);
        return ApiResponse.success(templates, "Active templates retrieved successfully");
    }

    /**
     * Get high-performing templates
     */
    @GetMapping("/high-performing")
    public ApiResponse<List<PromptTemplateDTO>> getHighPerformingTemplates(
            @RequestParam(defaultValue = "0.7") Float threshold) {
        log.info("GET /api/prompt-templates/high-performing - Fetching high-performing templates");
        List<PromptTemplateDTO> templates = templateService.getHighPerformingTemplates(threshold);
        return ApiResponse.success(templates, "High-performing templates retrieved successfully");
    }

    /**
     * Search templates by name
     */
    @GetMapping("/search")
    public ApiResponse<List<PromptTemplateDTO>> searchTemplates(@RequestParam String name) {
        log.info("GET /api/prompt-templates/search?name={} - Searching templates", name);
        List<PromptTemplateDTO> templates = templateService.searchTemplatesByName(name);
        return ApiResponse.success(templates, "Templates retrieved successfully");
    }

    /**
     * Create a new template
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PromptTemplateDTO> createTemplate(@Valid @RequestBody PromptTemplateDTO templateDTO) {
        log.info("POST /api/prompt-templates - Creating new template: {}", templateDTO.getName());
        PromptTemplateDTO createdTemplate = templateService.createTemplate(templateDTO);
        return ApiResponse.success(createdTemplate, "Template created successfully");
    }

    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    public ApiResponse<PromptTemplateDTO> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody PromptTemplateDTO templateDTO) {
        log.info("PUT /api/prompt-templates/{} - Updating template", id);
        PromptTemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDTO);
        return ApiResponse.success(updatedTemplate, "Template updated successfully");
    }

    /**
     * Delete a template
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteTemplate(@PathVariable UUID id) {
        log.info("DELETE /api/prompt-templates/{} - Deleting template", id);
        templateService.deleteTemplate(id);
        return ApiResponse.success(null, "Template deleted successfully");
    }

    /**
     * Activate a template
     */
    @PatchMapping("/{id}/activate")
    public ApiResponse<PromptTemplateDTO> activateTemplate(@PathVariable UUID id) {
        log.info("PATCH /api/prompt-templates/{}/activate - Activating template", id);
        PromptTemplateDTO template = templateService.activateTemplate(id);
        return ApiResponse.success(template, "Template activated successfully");
    }

    /**
     * Deactivate a template
     */
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<PromptTemplateDTO> deactivateTemplate(@PathVariable UUID id) {
        log.info("PATCH /api/prompt-templates/{}/deactivate - Deactivating template", id);
        PromptTemplateDTO template = templateService.deactivateTemplate(id);
        return ApiResponse.success(template, "Template deactivated successfully");
    }

    /**
     * Count active templates
     */
    @GetMapping("/active/count")
    public ApiResponse<Long> countActiveTemplates() {
        log.info("GET /api/prompt-templates/active/count - Counting active templates");
        long count = templateService.countActiveTemplates();
        return ApiResponse.success(count, "Active template count retrieved successfully");
    }
}
