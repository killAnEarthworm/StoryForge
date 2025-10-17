package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.ProjectDTO;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing projects
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * Get all projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        log.debug("Fetching all projects");
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(UUID id) {
        log.debug("Fetching project with id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return convertToDTO(project);
    }

    /**
     * Create a new project
     */
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        log.info("Creating new project: {}", projectDTO.getName());
        Project project = convertToEntity(projectDTO);
        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    /**
     * Update an existing project
     */
    @Transactional
    public ProjectDTO updateProject(UUID id, ProjectDTO projectDTO) {
        log.info("Updating project with id: {}", id);
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Update fields
        existingProject.setName(projectDTO.getName());
        existingProject.setDescription(projectDTO.getDescription());
        existingProject.setGenre(projectDTO.getGenre());
        existingProject.setTheme(projectDTO.getTheme());
        existingProject.setWritingStyle(projectDTO.getWritingStyle());
        existingProject.setStatus(projectDTO.getStatus());

        Project updatedProject = projectRepository.save(existingProject);
        return convertToDTO(updatedProject);
    }

    /**
     * Delete a project
     */
    @Transactional
    public void deleteProject(UUID id) {
        log.info("Deleting project with id: {}", id);
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * Get projects by status
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByStatus(String status) {
        log.debug("Fetching projects with status: {}", status);
        return projectRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get projects by genre
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByGenre(String genre) {
        log.debug("Fetching projects with genre: {}", genre);
        return projectRepository.findByGenre(genre).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search projects by name
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> searchProjectsByName(String name) {
        log.debug("Searching projects with name containing: {}", name);
        return projectRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Conversion methods
    private ProjectDTO convertToDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .genre(project.getGenre())
                .theme(project.getTheme())
                .writingStyle(project.getWritingStyle())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private Project convertToEntity(ProjectDTO dto) {
        return Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .genre(dto.getGenre())
                .theme(dto.getTheme())
                .writingStyle(dto.getWritingStyle())
                .status(dto.getStatus() != null ? dto.getStatus() : "draft")
                .build();
    }
}
