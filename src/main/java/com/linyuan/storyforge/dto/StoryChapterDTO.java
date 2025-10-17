package com.linyuan.storyforge.dto;

import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * StoryChapter entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryChapterDTO {

    private UUID id;

    private UUID projectId;

    @NotNull(message = "Chapter number is required")
    private Integer chapterNumber;

    private String title;

    private String outline;

    private String mainConflict;

    private List<UUID> participatingCharacters;

    private UUID mainSceneId;

    private Integer targetWordCount;

    private String tone;

    private String pacing;

    private String generatedContent;

    private Map<String, Object> generationParams;

    private Integer version;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
