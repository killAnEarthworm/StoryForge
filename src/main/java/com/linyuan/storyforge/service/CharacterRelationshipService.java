package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.CharacterRelationshipDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.CharacterRelationship;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRelationshipRepository;
import com.linyuan.storyforge.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing character relationships
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterRelationshipService {

    private final CharacterRelationshipRepository relationshipRepository;
    private final CharacterRepository characterRepository;

    /**
     * Get all relationships
     */
    @Transactional(readOnly = true)
    public List<CharacterRelationshipDTO> getAllRelationships() {
        log.debug("Fetching all character relationships");
        return relationshipRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get relationship by ID
     */
    @Transactional(readOnly = true)
    public CharacterRelationshipDTO getRelationshipById(UUID id) {
        log.debug("Fetching relationship with id: {}", id);
        CharacterRelationship relationship = relationshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CharacterRelationship", "id", id));
        return convertToDTO(relationship);
    }

    /**
     * Get all relationships for a specific character
     */
    @Transactional(readOnly = true)
    public List<CharacterRelationshipDTO> getRelationshipsByCharacterId(UUID characterId) {
        log.debug("Fetching relationships for character: {}", characterId);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return relationshipRepository.findByCharacterId(characterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get relationships by type
     */
    @Transactional(readOnly = true)
    public List<CharacterRelationshipDTO> getRelationshipsByType(String relationshipType) {
        log.debug("Fetching relationships with type: {}", relationshipType);
        return relationshipRepository.findByRelationshipType(relationshipType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new relationship
     */
    @Transactional
    public CharacterRelationshipDTO createRelationship(CharacterRelationshipDTO relationshipDTO) {
        log.info("Creating new relationship between characters {} and {}",
                relationshipDTO.getCharacterAId(), relationshipDTO.getCharacterBId());

        // Validate both characters exist
        Character characterA = characterRepository.findById(relationshipDTO.getCharacterAId())
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", relationshipDTO.getCharacterAId()));
        Character characterB = characterRepository.findById(relationshipDTO.getCharacterBId())
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", relationshipDTO.getCharacterBId()));

        // Prevent self-relationship
        if (relationshipDTO.getCharacterAId().equals(relationshipDTO.getCharacterBId())) {
            throw new IllegalArgumentException("A character cannot have a relationship with itself");
        }

        // Check if relationship already exists
        if (relationshipRepository.existsByCharacterIds(relationshipDTO.getCharacterAId(), relationshipDTO.getCharacterBId())) {
            throw new IllegalArgumentException("Relationship between these characters already exists");
        }

        CharacterRelationship relationship = convertToEntity(relationshipDTO);
        relationship.setCharacterA(characterA);
        relationship.setCharacterB(characterB);
        CharacterRelationship savedRelationship = relationshipRepository.save(relationship);
        return convertToDTO(savedRelationship);
    }

    /**
     * Update an existing relationship
     */
    @Transactional
    public CharacterRelationshipDTO updateRelationship(UUID id, CharacterRelationshipDTO relationshipDTO) {
        log.info("Updating relationship with id: {}", id);
        CharacterRelationship existingRelationship = relationshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CharacterRelationship", "id", id));

        // Update fields
        existingRelationship.setRelationshipType(relationshipDTO.getRelationshipType());
        existingRelationship.setRelationshipDescription(relationshipDTO.getRelationshipDescription());
        existingRelationship.setTensionPoints(relationshipDTO.getTensionPoints());
        existingRelationship.setSharedHistory(relationshipDTO.getSharedHistory());
        existingRelationship.setDynamicState(relationshipDTO.getDynamicState());

        CharacterRelationship updatedRelationship = relationshipRepository.save(existingRelationship);
        return convertToDTO(updatedRelationship);
    }

    /**
     * Delete a relationship
     */
    @Transactional
    public void deleteRelationship(UUID id) {
        log.info("Deleting relationship with id: {}", id);
        if (!relationshipRepository.existsById(id)) {
            throw new ResourceNotFoundException("CharacterRelationship", "id", id);
        }
        relationshipRepository.deleteById(id);
    }

    // Conversion methods
    private CharacterRelationshipDTO convertToDTO(CharacterRelationship relationship) {
        CharacterRelationshipDTO dto = new CharacterRelationshipDTO();
        dto.setId(relationship.getId());
        dto.setCharacterAId(relationship.getCharacterA().getId());
        dto.setCharacterBId(relationship.getCharacterB().getId());
        dto.setRelationshipType(relationship.getRelationshipType());
        dto.setRelationshipDescription(relationship.getRelationshipDescription());
        dto.setTensionPoints(relationship.getTensionPoints());
        dto.setSharedHistory(relationship.getSharedHistory());
        dto.setDynamicState(relationship.getDynamicState());
        dto.setCreatedAt(relationship.getCreatedAt());
        dto.setUpdatedAt(relationship.getUpdatedAt());
        return dto;
    }

    private CharacterRelationship convertToEntity(CharacterRelationshipDTO dto) {
        return CharacterRelationship.builder()
                .relationshipType(dto.getRelationshipType())
                .relationshipDescription(dto.getRelationshipDescription())
                .tensionPoints(dto.getTensionPoints())
                .sharedHistory(dto.getSharedHistory())
                .dynamicState(dto.getDynamicState())
                .build();
    }
}
