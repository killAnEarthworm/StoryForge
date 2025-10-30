package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.StoryChapterDTO;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Scene;
import com.linyuan.storyforge.entity.StoryChapter;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.SceneRepository;
import com.linyuan.storyforge.repository.StoryChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing story chapters
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoryChapterService {

    private final StoryChapterRepository chapterRepository;
    private final ProjectRepository projectRepository;
    private final SceneRepository sceneRepository;

    /**
     * Get all chapters
     */
    @Transactional(readOnly = true)
    public List<StoryChapterDTO> getAllChapters() {
        log.debug("Fetching all chapters");
        return chapterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get chapter by ID
     */
    @Transactional(readOnly = true)
    public StoryChapterDTO getChapterById(UUID id) {
        log.debug("Fetching chapter with id: {}", id);
        StoryChapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoryChapter", "id", id));
        return convertToDTO(chapter);
    }

    /**
     * Get chapters by project ID
     */
    @Transactional(readOnly = true)
    public List<StoryChapterDTO> getChaptersByProjectId(UUID projectId) {
        log.debug("Fetching chapters for project: {}", projectId);
        return chapterRepository.findByProjectIdOrderByChapterNumberAsc(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new chapter
     */
    @Transactional
    public StoryChapterDTO createChapter(StoryChapterDTO chapterDTO) {
        log.info("Creating new chapter {} for project: {}", chapterDTO.getChapterNumber(), chapterDTO.getProjectId());

        // Validate project exists
        Project project = projectRepository.findById(chapterDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", chapterDTO.getProjectId()));

        StoryChapter chapter = convertToEntity(chapterDTO);
        chapter.setProject(project);

        // Set main scene if provided
        if (chapterDTO.getMainSceneId() != null) {
            Scene scene = sceneRepository.findById(chapterDTO.getMainSceneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", chapterDTO.getMainSceneId()));
            chapter.setMainScene(scene);
        }

        StoryChapter savedChapter = chapterRepository.save(chapter);
        return convertToDTO(savedChapter);
    }

    /**
     * Update an existing chapter
     */
    @Transactional
    public StoryChapterDTO updateChapter(UUID id, StoryChapterDTO chapterDTO) {
        log.info("Updating chapter with id: {}", id);
        StoryChapter existingChapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoryChapter", "id", id));

        // Update fields
        existingChapter.setChapterNumber(chapterDTO.getChapterNumber());
        existingChapter.setTitle(chapterDTO.getTitle());
        existingChapter.setOutline(chapterDTO.getOutline());
        existingChapter.setMainConflict(chapterDTO.getMainConflict());
        existingChapter.setParticipatingCharacters(chapterDTO.getParticipatingCharacters());
        existingChapter.setTargetWordCount(chapterDTO.getTargetWordCount());
        existingChapter.setTone(chapterDTO.getTone());
        existingChapter.setPacing(chapterDTO.getPacing());
        existingChapter.setGeneratedContent(chapterDTO.getGeneratedContent());
        existingChapter.setGenerationParams(chapterDTO.getGenerationParams());
        existingChapter.setVersion(chapterDTO.getVersion());
        existingChapter.setStatus(chapterDTO.getStatus());

        // Update main scene if provided
        if (chapterDTO.getMainSceneId() != null) {
            Scene scene = sceneRepository.findById(chapterDTO.getMainSceneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", chapterDTO.getMainSceneId()));
            existingChapter.setMainScene(scene);
        }

        StoryChapter updatedChapter = chapterRepository.save(existingChapter);
        return convertToDTO(updatedChapter);
    }

    /**
     * Delete a chapter
     */
    @Transactional
    public void deleteChapter(UUID id) {
        log.info("Deleting chapter with id: {}", id);
        if (!chapterRepository.existsById(id)) {
            throw new ResourceNotFoundException("StoryChapter", "id", id);
        }
        chapterRepository.deleteById(id);
    }

    /**
     * Get chapters by status
     */
    @Transactional(readOnly = true)
    public List<StoryChapterDTO> getChaptersByStatus(String status) {
        log.debug("Fetching chapters with status: {}", status);
        return chapterRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Conversion methods
    StoryChapterDTO convertToDTO(StoryChapter chapter) {
        return StoryChapterDTO.builder()
                .id(chapter.getId())
                .projectId(chapter.getProject().getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .outline(chapter.getOutline())
                .mainConflict(chapter.getMainConflict())
                .participatingCharacters(chapter.getParticipatingCharacters())
                .mainSceneId(chapter.getMainScene() != null ? chapter.getMainScene().getId() : null)
                .targetWordCount(chapter.getTargetWordCount())
                .tone(chapter.getTone())
                .pacing(chapter.getPacing())
                .generatedContent(chapter.getGeneratedContent())
                .generationParams(chapter.getGenerationParams())
                .version(chapter.getVersion())
                .status(chapter.getStatus())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    private StoryChapter convertToEntity(StoryChapterDTO dto) {
        return StoryChapter.builder()
                .chapterNumber(dto.getChapterNumber())
                .title(dto.getTitle())
                .outline(dto.getOutline())
                .mainConflict(dto.getMainConflict())
                .participatingCharacters(dto.getParticipatingCharacters())
                .targetWordCount(dto.getTargetWordCount())
                .tone(dto.getTone())
                .pacing(dto.getPacing())
                .generatedContent(dto.getGeneratedContent())
                .generationParams(dto.getGenerationParams())
                .version(dto.getVersion() != null ? dto.getVersion() : 1)
                .status(dto.getStatus() != null ? dto.getStatus() : "outline")
                .build();
    }
}
