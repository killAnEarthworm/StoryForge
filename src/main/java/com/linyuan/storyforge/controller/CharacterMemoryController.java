package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.CharacterMemoryDTO;
import com.linyuan.storyforge.service.CharacterMemoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Character Memory management
 */
@Slf4j
@RestController
@RequestMapping("/api/character-memories")
@RequiredArgsConstructor
public class CharacterMemoryController {

    private final CharacterMemoryService memoryService;

    /**
     * Get all memories
     */
    @GetMapping
    public ApiResponse<List<CharacterMemoryDTO>> getAllMemories() {
        log.info("GET /api/character-memories - Fetching all memories");
        List<CharacterMemoryDTO> memories = memoryService.getAllMemories();
        return ApiResponse.success(memories, "Memories retrieved successfully");
    }

    /**
     * Get memory by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<CharacterMemoryDTO> getMemoryById(@PathVariable UUID id) {
        log.info("GET /api/character-memories/{} - Fetching memory", id);
        CharacterMemoryDTO memory = memoryService.getMemoryById(id);
        return ApiResponse.success(memory, "Memory retrieved successfully");
    }

    /**
     * Get memories by character ID
     */
    @GetMapping("/character/{characterId}")
    public ApiResponse<List<CharacterMemoryDTO>> getMemoriesByCharacterId(@PathVariable UUID characterId) {
        log.info("GET /api/character-memories/character/{} - Fetching memories by character", characterId);
        List<CharacterMemoryDTO> memories = memoryService.getMemoriesByCharacterId(characterId);
        return ApiResponse.success(memories, "Memories retrieved successfully");
    }

    /**
     * Get memories by character ID and type
     */
    @GetMapping("/character/{characterId}/type/{memoryType}")
    public ApiResponse<List<CharacterMemoryDTO>> getMemoriesByCharacterIdAndType(
            @PathVariable UUID characterId,
            @PathVariable String memoryType) {
        log.info("GET /api/character-memories/character/{}/type/{} - Fetching memories", characterId, memoryType);
        List<CharacterMemoryDTO> memories = memoryService.getMemoriesByCharacterIdAndType(characterId, memoryType);
        return ApiResponse.success(memories, "Memories retrieved successfully");
    }

    /**
     * Get accessible memories
     */
    @GetMapping("/character/{characterId}/accessible")
    public ApiResponse<List<CharacterMemoryDTO>> getAccessibleMemories(
            @PathVariable UUID characterId,
            @RequestParam(defaultValue = "0.5") Float threshold) {
        log.info("GET /api/character-memories/character/{}/accessible - Fetching accessible memories", characterId);
        List<CharacterMemoryDTO> memories = memoryService.getAccessibleMemories(characterId, threshold);
        return ApiResponse.success(memories, "Accessible memories retrieved successfully");
    }

    /**
     * Get emotional memories
     */
    @GetMapping("/character/{characterId}/emotional")
    public ApiResponse<List<CharacterMemoryDTO>> getEmotionalMemories(
            @PathVariable UUID characterId,
            @RequestParam(defaultValue = "0.7") Float threshold) {
        log.info("GET /api/character-memories/character/{}/emotional - Fetching emotional memories", characterId);
        List<CharacterMemoryDTO> memories = memoryService.getEmotionalMemories(characterId, threshold);
        return ApiResponse.success(memories, "Emotional memories retrieved successfully");
    }

    /**
     * Get most accessed memories
     */
    @GetMapping("/character/{characterId}/most-accessed")
    public ApiResponse<List<CharacterMemoryDTO>> getMostAccessedMemories(@PathVariable UUID characterId) {
        log.info("GET /api/character-memories/character/{}/most-accessed - Fetching most accessed memories", characterId);
        List<CharacterMemoryDTO> memories = memoryService.getMostAccessedMemories(characterId);
        return ApiResponse.success(memories, "Most accessed memories retrieved successfully");
    }

    /**
     * Create a new memory
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CharacterMemoryDTO> createMemory(@Valid @RequestBody CharacterMemoryDTO memoryDTO) {
        log.info("POST /api/character-memories - Creating new memory");
        CharacterMemoryDTO createdMemory = memoryService.createMemory(memoryDTO);
        return ApiResponse.success(createdMemory, "Memory created successfully");
    }

    /**
     * Update an existing memory
     */
    @PutMapping("/{id}")
    public ApiResponse<CharacterMemoryDTO> updateMemory(
            @PathVariable UUID id,
            @Valid @RequestBody CharacterMemoryDTO memoryDTO) {
        log.info("PUT /api/character-memories/{} - Updating memory", id);
        CharacterMemoryDTO updatedMemory = memoryService.updateMemory(id, memoryDTO);
        return ApiResponse.success(updatedMemory, "Memory updated successfully");
    }

    /**
     * Access a memory (increments access count)
     */
    @PostMapping("/{id}/access")
    public ApiResponse<CharacterMemoryDTO> accessMemory(@PathVariable UUID id) {
        log.info("POST /api/character-memories/{}/access - Accessing memory", id);
        CharacterMemoryDTO memory = memoryService.accessMemory(id);
        return ApiResponse.success(memory, "Memory accessed successfully");
    }

    /**
     * Delete a memory
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteMemory(@PathVariable UUID id) {
        log.info("DELETE /api/character-memories/{} - Deleting memory", id);
        memoryService.deleteMemory(id);
        return ApiResponse.success(null, "Memory deleted successfully");
    }

    /**
     * Count memories by character ID
     */
    @GetMapping("/character/{characterId}/count")
    public ApiResponse<Long> countMemoriesByCharacterId(@PathVariable UUID characterId) {
        log.info("GET /api/character-memories/character/{}/count - Counting memories", characterId);
        long count = memoryService.countMemoriesByCharacterId(characterId);
        return ApiResponse.success(count, "Memory count retrieved successfully");
    }
}
