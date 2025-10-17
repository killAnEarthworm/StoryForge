package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.StoryChapterDTO;
import com.linyuan.storyforge.service.StoryChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Story Chapter management
 */
@Slf4j
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class StoryChapterController {

    private final StoryChapterService chapterService;

    /**
     * Get all chapters
     */
    @GetMapping
    public ApiResponse<List<StoryChapterDTO>> getAllChapters() {
        log.info("GET /api/chapters - Fetching all chapters");
        List<StoryChapterDTO> chapters = chapterService.getAllChapters();
        return ApiResponse.success(chapters, "Chapters retrieved successfully");
    }

    /**
     * Get chapter by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<StoryChapterDTO> getChapterById(@PathVariable UUID id) {
        log.info("GET /api/chapters/{} - Fetching chapter", id);
        StoryChapterDTO chapter = chapterService.getChapterById(id);
        return ApiResponse.success(chapter, "Chapter retrieved successfully");
    }

    /**
     * Get chapters by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<StoryChapterDTO>> getChaptersByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/chapters/project/{} - Fetching chapters by project", projectId);
        List<StoryChapterDTO> chapters = chapterService.getChaptersByProjectId(projectId);
        return ApiResponse.success(chapters, "Chapters retrieved successfully");
    }

    /**
     * Get chapters by status
     */
    @GetMapping("/status/{status}")
    public ApiResponse<List<StoryChapterDTO>> getChaptersByStatus(@PathVariable String status) {
        log.info("GET /api/chapters/status/{} - Fetching chapters by status", status);
        List<StoryChapterDTO> chapters = chapterService.getChaptersByStatus(status);
        return ApiResponse.success(chapters, "Chapters retrieved successfully");
    }

    /**
     * Create a new chapter
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoryChapterDTO> createChapter(@Valid @RequestBody StoryChapterDTO chapterDTO) {
        log.info("POST /api/chapters - Creating new chapter: {}", chapterDTO.getChapterNumber());
        StoryChapterDTO createdChapter = chapterService.createChapter(chapterDTO);
        return ApiResponse.success(createdChapter, "Chapter created successfully");
    }

    /**
     * Update an existing chapter
     */
    @PutMapping("/{id}")
    public ApiResponse<StoryChapterDTO> updateChapter(
            @PathVariable UUID id,
            @Valid @RequestBody StoryChapterDTO chapterDTO) {
        log.info("PUT /api/chapters/{} - Updating chapter", id);
        StoryChapterDTO updatedChapter = chapterService.updateChapter(id, chapterDTO);
        return ApiResponse.success(updatedChapter, "Chapter updated successfully");
    }

    /**
     * Delete a chapter
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteChapter(@PathVariable UUID id) {
        log.info("DELETE /api/chapters/{} - Deleting chapter", id);
        chapterService.deleteChapter(id);
        return ApiResponse.success(null, "Chapter deleted successfully");
    }
}
