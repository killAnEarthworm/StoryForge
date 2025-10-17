package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.ProjectDTO;
import com.linyuan.storyforge.service.ProjectService;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Project management
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Get all projects
     */
    @GetMapping
    public ApiResponse<List<ProjectDTO>> getAllProjects() {
        log.info("GET /api/projects - Fetching all projects");
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ApiResponse.success(projects, "Projects retrieved successfully");
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<ProjectDTO> getProjectById(@PathVariable UUID id) {
        log.info("GET /api/projects/{} - Fetching project", id);
        ProjectDTO project = projectService.getProjectById(id);
        return ApiResponse.success(project, "Project retrieved successfully");
    }

    /**
     * Create a new project
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        log.info("POST /api/projects - Creating new project: {}", projectDTO.getName());
        ProjectDTO createdProject = projectService.createProject(projectDTO);
        return ApiResponse.success(createdProject, "Project created successfully");
    }

    /**
     * Update an existing project
     */
    @PutMapping("/{id}")
    public ApiResponse<ProjectDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDTO projectDTO) {
        log.info("PUT /api/projects/{} - Updating project", id);
        ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
        return ApiResponse.success(updatedProject, "Project updated successfully");
    }

    /**
     * Delete a project
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteProject(@PathVariable UUID id) {
        log.info("DELETE /api/projects/{} - Deleting project", id);
        projectService.deleteProject(id);
        return ApiResponse.success(null, "Project deleted successfully");
    }

    /**
     * Get projects by status
     */
    @GetMapping("/status/{status}")
    public ApiResponse<List<ProjectDTO>> getProjectsByStatus(@PathVariable String status) {
        log.info("GET /api/projects/status/{} - Fetching projects by status", status);
        List<ProjectDTO> projects = projectService.getProjectsByStatus(status);
        return ApiResponse.success(projects, "Projects retrieved successfully");
    }

    /**
     * Get projects by genre
     */
    @GetMapping("/genre/{genre}")
    public ApiResponse<List<ProjectDTO>> getProjectsByGenre(@PathVariable String genre) {
        log.info("GET /api/projects/genre/{} - Fetching projects by genre", genre);
        List<ProjectDTO> projects = projectService.getProjectsByGenre(genre);
        return ApiResponse.success(projects, "Projects retrieved successfully");
    }

    /**
     * Search projects by name
     */
    @GetMapping("/search")
    public ApiResponse<List<ProjectDTO>> searchProjects(@RequestParam String name) {
        log.info("GET /api/projects/search?name={} - Searching projects", name);
        List<ProjectDTO> projects = projectService.searchProjectsByName(name);
        return ApiResponse.success(projects, "Projects retrieved successfully");
    }
}
