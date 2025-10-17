package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.CharacterMemoryDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.CharacterMemory;
import com.linyuan.storyforge.entity.Timeline;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterMemoryRepository;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing character memories
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterMemoryService {

    private final CharacterMemoryRepository memoryRepository;
    private final CharacterRepository characterRepository;
    private final TimelineRepository timelineRepository;

    /**
     * Get all memories
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getAllMemories() {
        log.debug("Fetching all character memories");
        return memoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get memory by ID
     */
    @Transactional(readOnly = true)
    public CharacterMemoryDTO getMemoryById(UUID id) {
        log.debug("Fetching memory with id: {}", id);
        CharacterMemory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CharacterMemory", "id", id));
        return convertToDTO(memory);
    }

    /**
     * Get memories by character ID
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getMemoriesByCharacterId(UUID characterId) {
        log.debug("Fetching memories for character: {}", characterId);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return memoryRepository.findByCharacterId(characterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get memories by character ID and type
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getMemoriesByCharacterIdAndType(UUID characterId, String memoryType) {
        log.debug("Fetching {} memories for character: {}", memoryType, characterId);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return memoryRepository.findByCharacterIdAndMemoryType(characterId, memoryType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get accessible memories (above threshold)
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getAccessibleMemories(UUID characterId, Float threshold) {
        log.debug("Fetching accessible memories for character: {} with threshold: {}", characterId, threshold);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return memoryRepository.findAccessibleMemories(characterId, threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get emotional memories (above threshold)
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getEmotionalMemories(UUID characterId, Float threshold) {
        log.debug("Fetching emotional memories for character: {} with threshold: {}", characterId, threshold);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return memoryRepository.findEmotionalMemories(characterId, threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get most accessed memories
     */
    @Transactional(readOnly = true)
    public List<CharacterMemoryDTO> getMostAccessedMemories(UUID characterId) {
        log.debug("Fetching most accessed memories for character: {}", characterId);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return memoryRepository.findMostAccessedMemories(characterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new memory
     */
    @Transactional
    public CharacterMemoryDTO createMemory(CharacterMemoryDTO memoryDTO) {
        log.info("Creating new memory for character: {}", memoryDTO.getCharacterId());

        // Validate character exists
        Character character = characterRepository.findById(memoryDTO.getCharacterId())
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", memoryDTO.getCharacterId()));

        // Validate timeline if provided
        Timeline timeline = null;
        if (memoryDTO.getTimelineId() != null) {
            timeline = timelineRepository.findById(memoryDTO.getTimelineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Timeline", "id", memoryDTO.getTimelineId()));
        }

        // Set default values if not provided
        if (memoryDTO.getAccessibility() == null) {
            memoryDTO.setAccessibility(1.0f);
        }
        if (memoryDTO.getAccessCount() == null) {
            memoryDTO.setAccessCount(0);
        }

        CharacterMemory memory = convertToEntity(memoryDTO);
        memory.setCharacter(character);
        memory.setTimeline(timeline);
        CharacterMemory savedMemory = memoryRepository.save(memory);
        return convertToDTO(savedMemory);
    }

    /**
     * Update an existing memory
     */
    @Transactional
    public CharacterMemoryDTO updateMemory(UUID id, CharacterMemoryDTO memoryDTO) {
        log.info("Updating memory with id: {}", id);
        CharacterMemory existingMemory = memoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CharacterMemory", "id", id));

        // Update timeline if provided
        if (memoryDTO.getTimelineId() != null &&
            (existingMemory.getTimeline() == null || !existingMemory.getTimeline().getId().equals(memoryDTO.getTimelineId()))) {
            Timeline timeline = timelineRepository.findById(memoryDTO.getTimelineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Timeline", "id", memoryDTO.getTimelineId()));
            existingMemory.setTimeline(timeline);
        }

        // Update fields
        existingMemory.setMemoryType(memoryDTO.getMemoryType());
        existingMemory.setMemoryContent(memoryDTO.getMemoryContent());
        existingMemory.setEmotionalWeight(memoryDTO.getEmotionalWeight());
        existingMemory.setKeywords(memoryDTO.getKeywords());
        existingMemory.setRelatedCharacters(memoryDTO.getRelatedCharacters());
        existingMemory.setRelatedLocations(memoryDTO.getRelatedLocations());
        existingMemory.setAccessibility(memoryDTO.getAccessibility());
        existingMemory.setLastAccessed(memoryDTO.getLastAccessed());
        existingMemory.setAccessCount(memoryDTO.getAccessCount());

        CharacterMemory updatedMemory = memoryRepository.save(existingMemory);
        return convertToDTO(updatedMemory);
    }

    /**
     * Access a memory (increments access count and updates last accessed)
     */
    @Transactional
    public CharacterMemoryDTO accessMemory(UUID id) {
        log.info("Accessing memory with id: {}", id);
        CharacterMemory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CharacterMemory", "id", id));

        memory.setAccessCount(memory.getAccessCount() + 1);
        memory.setLastAccessed(LocalDateTime.now());

        CharacterMemory updatedMemory = memoryRepository.save(memory);
        return convertToDTO(updatedMemory);
    }

    /**
     * Delete a memory
     */
    @Transactional
    public void deleteMemory(UUID id) {
        log.info("Deleting memory with id: {}", id);
        if (!memoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("CharacterMemory", "id", id);
        }
        memoryRepository.deleteById(id);
    }

    /**
     * Count memories by character ID
     */
    @Transactional(readOnly = true)
    public long countMemoriesByCharacterId(UUID characterId) {
        log.debug("Counting memories for character: {}", characterId);
        return memoryRepository.countByCharacterId(characterId);
    }

    // Conversion methods
    private CharacterMemoryDTO convertToDTO(CharacterMemory memory) {
        CharacterMemoryDTO dto = new CharacterMemoryDTO();
        dto.setId(memory.getId());
        dto.setCharacterId(memory.getCharacter().getId());
        dto.setTimelineId(memory.getTimeline() != null ? memory.getTimeline().getId() : null);
        dto.setMemoryType(memory.getMemoryType());
        dto.setMemoryContent(memory.getMemoryContent());
        dto.setEmotionalWeight(memory.getEmotionalWeight());
        dto.setKeywords(memory.getKeywords());
        dto.setRelatedCharacters(memory.getRelatedCharacters());
        dto.setRelatedLocations(memory.getRelatedLocations());
        dto.setAccessibility(memory.getAccessibility());
        dto.setLastAccessed(memory.getLastAccessed());
        dto.setAccessCount(memory.getAccessCount());
        dto.setCreatedAt(memory.getCreatedAt());
        dto.setUpdatedAt(memory.getUpdatedAt());
        return dto;
    }

    private CharacterMemory convertToEntity(CharacterMemoryDTO dto) {
        return CharacterMemory.builder()
                .memoryType(dto.getMemoryType())
                .memoryContent(dto.getMemoryContent())
                .emotionalWeight(dto.getEmotionalWeight())
                .keywords(dto.getKeywords())
                .relatedCharacters(dto.getRelatedCharacters())
                .relatedLocations(dto.getRelatedLocations())
                .accessibility(dto.getAccessibility())
                .lastAccessed(dto.getLastAccessed())
                .accessCount(dto.getAccessCount())
                .build();
    }
}
