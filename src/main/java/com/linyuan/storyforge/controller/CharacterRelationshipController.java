package com.linyuan.storyforge.controller;

import com.linyuan.storyforge.common.ApiResponse;
import com.linyuan.storyforge.dto.CharacterRelationshipDTO;
import com.linyuan.storyforge.service.CharacterRelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Character Relationship management
 */
@Slf4j
@RestController
@RequestMapping("/api/character-relationships")
@RequiredArgsConstructor
public class CharacterRelationshipController {

    private final CharacterRelationshipService relationshipService;

    /**
     * Get all character relationships
     */
    @GetMapping
    public ApiResponse<List<CharacterRelationshipDTO>> getAllRelationships() {
        log.info("GET /api/character-relationships - Fetching all relationships");
        List<CharacterRelationshipDTO> relationships = relationshipService.getAllRelationships();
        return ApiResponse.success(relationships, "Relationships retrieved successfully");
    }

    /**
     * Get relationship by ID
     */
    @GetMapping("/{id}")
    public ApiResponse<CharacterRelationshipDTO> getRelationshipById(@PathVariable UUID id) {
        log.info("GET /api/character-relationships/{} - Fetching relationship", id);
        CharacterRelationshipDTO relationship = relationshipService.getRelationshipById(id);
        return ApiResponse.success(relationship, "Relationship retrieved successfully");
    }

    /**
     * Get relationships by character ID
     */
    @GetMapping("/character/{characterId}")
    public ApiResponse<List<CharacterRelationshipDTO>> getRelationshipsByCharacterId(@PathVariable UUID characterId) {
        log.info("GET /api/character-relationships/character/{} - Fetching relationships by character", characterId);
        List<CharacterRelationshipDTO> relationships = relationshipService.getRelationshipsByCharacterId(characterId);
        return ApiResponse.success(relationships, "Relationships retrieved successfully");
    }

    /**
     * Get relationships by type
     */
    @GetMapping("/type/{relationshipType}")
    public ApiResponse<List<CharacterRelationshipDTO>> getRelationshipsByType(@PathVariable String relationshipType) {
        log.info("GET /api/character-relationships/type/{} - Fetching relationships by type", relationshipType);
        List<CharacterRelationshipDTO> relationships = relationshipService.getRelationshipsByType(relationshipType);
        return ApiResponse.success(relationships, "Relationships retrieved successfully");
    }

    /**
     * Create a new relationship
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CharacterRelationshipDTO> createRelationship(@Valid @RequestBody CharacterRelationshipDTO relationshipDTO) {
        log.info("POST /api/character-relationships - Creating new relationship");
        CharacterRelationshipDTO createdRelationship = relationshipService.createRelationship(relationshipDTO);
        return ApiResponse.success(createdRelationship, "Relationship created successfully");
    }

    /**
     * Update an existing relationship
     */
    @PutMapping("/{id}")
    public ApiResponse<CharacterRelationshipDTO> updateRelationship(
            @PathVariable UUID id,
            @Valid @RequestBody CharacterRelationshipDTO relationshipDTO) {
        log.info("PUT /api/character-relationships/{} - Updating relationship", id);
        CharacterRelationshipDTO updatedRelationship = relationshipService.updateRelationship(id, relationshipDTO);
        return ApiResponse.success(updatedRelationship, "Relationship updated successfully");
    }

    /**
     * Delete a relationship
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteRelationship(@PathVariable UUID id) {
        log.info("DELETE /api/character-relationships/{} - Deleting relationship", id);
        relationshipService.deleteRelationship(id);
        return ApiResponse.success(null, "Relationship deleted successfully");
    }
}
