package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.CharacterDTO;
import com.linyuan.storyforge.service.CharacterService;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Character management
 */
@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    /**
     * Get all characters
     */
    @GetMapping
    public ApiResponse<List<CharacterDTO>> getAllCharacters() {
        log.info("GET /api/characters - Fetching all characters");
        List<CharacterDTO> characters = characterService.getAllCharacters();
        return ApiResponse.success(characters, "Characters retrieved successfully");
    }

    /**
     * Get character by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<CharacterDTO> getCharacterById(@PathVariable UUID id) {
        log.info("GET /api/characters/{} - Fetching character", id);
        CharacterDTO character = characterService.getCharacterById(id);
        return ApiResponse.success(character, "Character retrieved successfully");
    }

    /**
     * Get characters by project ID
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<CharacterDTO>> getCharactersByProjectId(@PathVariable UUID projectId) {
        log.info("GET /api/characters/project/{} - Fetching characters by project", projectId);
        List<CharacterDTO> characters = characterService.getCharactersByProjectId(projectId);
        return ApiResponse.success(characters, "Characters retrieved successfully");
    }

    /**
     * Create a new character
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CharacterDTO> createCharacter(@Valid @RequestBody CharacterDTO characterDTO) {
        log.info("POST /api/characters - Creating new character: {}", characterDTO.getName());
        CharacterDTO createdCharacter = characterService.createCharacter(characterDTO);
        return ApiResponse.success(createdCharacter, "Character created successfully");
    }

    /**
     * Update an existing character
     */
    @PutMapping("/{id}")
    public ApiResponse<CharacterDTO> updateCharacter(
            @PathVariable UUID id,
            @Valid @RequestBody CharacterDTO characterDTO) {
        log.info("PUT /api/characters/{} - Updating character", id);
        CharacterDTO updatedCharacter = characterService.updateCharacter(id, characterDTO);
        return ApiResponse.success(updatedCharacter, "Character updated successfully");
    }

    /**
     * Delete a character
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCharacter(@PathVariable UUID id) {
        log.info("DELETE /api/characters/{} - Deleting character", id);
        characterService.deleteCharacter(id);
        return ApiResponse.success(null, "Character deleted successfully");
    }
}
