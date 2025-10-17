package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Dialogue DTO for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogueDTO {

    private UUID id;

    @NotNull(message = "Chapter ID cannot be null")
    private UUID chapterId;

    private UUID sceneId;

    @NotNull(message = "Speaker ID cannot be null")
    private UUID speakerId;

    private List<UUID> listenerIds; // 听众角色

    @NotBlank(message = "Dialogue text cannot be blank")
    private String dialogueText;

    private String tone; // 语气
    private String emotion; // 情绪

    // 上下文
    private String contextBefore;
    private String innerThoughts; // 内心想法
    private String bodyLanguage; // 肢体语言

    private Integer sequenceNumber; // 对话顺序

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
