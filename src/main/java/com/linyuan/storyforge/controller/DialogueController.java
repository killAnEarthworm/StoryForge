package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.DialogueDTO;
import com.linyuan.storyforge.service.DialogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Dialogue management
 */
@Slf4j
@RestController
@RequestMapping("/api/dialogues")
@RequiredArgsConstructor
public class DialogueController {

    private final DialogueService dialogueService;

    /**
     * Get all dialogues
     */
    @GetMapping
    public ApiResponse<List<DialogueDTO>> getAllDialogues() {
        log.info("GET /api/dialogues - Fetching all dialogues");
        List<DialogueDTO> dialogues = dialogueService.getAllDialogues();
        return ApiResponse.success(dialogues, "Dialogues retrieved successfully");
    }

    /**
     * Get dialogue by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<DialogueDTO> getDialogueById(@PathVariable UUID id) {
        log.info("GET /api/dialogues/{} - Fetching dialogue", id);
        DialogueDTO dialogue = dialogueService.getDialogueById(id);
        return ApiResponse.success(dialogue, "Dialogue retrieved successfully");
    }

    /**
     * Get dialogues by chapter ID
     */
    @GetMapping("/chapter/{chapterId}")
    public ApiResponse<List<DialogueDTO>> getDialoguesByChapterId(@PathVariable UUID chapterId) {
        log.info("GET /api/dialogues/chapter/{} - Fetching dialogues by chapter", chapterId);
        List<DialogueDTO> dialogues = dialogueService.getDialoguesByChapterId(chapterId);
        return ApiResponse.success(dialogues, "Dialogues retrieved successfully");
    }

    /**
     * Get dialogues by scene ID
     */
    @GetMapping("/scene/{sceneId}")
    public ApiResponse<List<DialogueDTO>> getDialoguesBySceneId(@PathVariable UUID sceneId) {
        log.info("GET /api/dialogues/scene/{} - Fetching dialogues by scene", sceneId);
        List<DialogueDTO> dialogues = dialogueService.getDialoguesBySceneId(sceneId);
        return ApiResponse.success(dialogues, "Dialogues retrieved successfully");
    }

    /**
     * Get dialogues by speaker ID
     */
    @GetMapping("/speaker/{speakerId}")
    public ApiResponse<List<DialogueDTO>> getDialoguesBySpeakerId(@PathVariable UUID speakerId) {
        log.info("GET /api/dialogues/speaker/{} - Fetching dialogues by speaker", speakerId);
        List<DialogueDTO> dialogues = dialogueService.getDialoguesBySpeakerId(speakerId);
        return ApiResponse.success(dialogues, "Dialogues retrieved successfully");
    }

    /**
     * Get dialogues by emotion
     */
    @GetMapping("/emotion/{emotion}")
    public ApiResponse<List<DialogueDTO>> getDialoguesByEmotion(@PathVariable String emotion) {
        log.info("GET /api/dialogues/emotion/{} - Fetching dialogues by emotion", emotion);
        List<DialogueDTO> dialogues = dialogueService.getDialoguesByEmotion(emotion);
        return ApiResponse.success(dialogues, "Dialogues retrieved successfully");
    }

    /**
     * Create a new dialogue
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DialogueDTO> createDialogue(@Valid @RequestBody DialogueDTO dialogueDTO) {
        log.info("POST /api/dialogues - Creating new dialogue");
        DialogueDTO createdDialogue = dialogueService.createDialogue(dialogueDTO);
        return ApiResponse.success(createdDialogue, "Dialogue created successfully");
    }

    /**
     * Update an existing dialogue
     */
    @PutMapping("/{id}")
    public ApiResponse<DialogueDTO> updateDialogue(
            @PathVariable UUID id,
            @Valid @RequestBody DialogueDTO dialogueDTO) {
        log.info("PUT /api/dialogues/{} - Updating dialogue", id);
        DialogueDTO updatedDialogue = dialogueService.updateDialogue(id, dialogueDTO);
        return ApiResponse.success(updatedDialogue, "Dialogue updated successfully");
    }

    /**
     * Delete a dialogue
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteDialogue(@PathVariable UUID id) {
        log.info("DELETE /api/dialogues/{} - Deleting dialogue", id);
        dialogueService.deleteDialogue(id);
        return ApiResponse.success(null, "Dialogue deleted successfully");
    }

    /**
     * Count dialogues by chapter ID
     */
    @GetMapping("/chapter/{chapterId}/count")
    public ApiResponse<Long> countDialoguesByChapterId(@PathVariable UUID chapterId) {
        log.info("GET /api/dialogues/chapter/{}/count - Counting dialogues", chapterId);
        long count = dialogueService.countDialoguesByChapterId(chapterId);
        return ApiResponse.success(count, "Dialogue count retrieved successfully");
    }
}
