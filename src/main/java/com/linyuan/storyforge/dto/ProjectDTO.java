package com.linyuan.storyforge.dto;

import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Project entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    private UUID id;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 50, message = "Genre must not exceed 50 characters")
    private String genre;

    private String theme;

    private String writingStyle;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
