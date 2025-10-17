package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.CharacterDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Worldview;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.repository.ProjectRepository;
import com.linyuan.storyforge.repository.WorldviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing characters
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final ProjectRepository projectRepository;
    private final WorldviewRepository worldviewRepository;

    /**
     * Get all characters
     */
    @Transactional(readOnly = true)
    public List<CharacterDTO> getAllCharacters() {
        log.debug("Fetching all characters");
        return characterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get character by ID
     */
    @Transactional(readOnly = true)
    public CharacterDTO getCharacterById(UUID id) {
        log.debug("Fetching character with id: {}", id);
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));
        return convertToDTO(character);
    }

    /**
     * Get characters by project ID
     */
    @Transactional(readOnly = true)
    public List<CharacterDTO> getCharactersByProjectId(UUID projectId) {
        log.debug("Fetching characters for project: {}", projectId);
        return characterRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new character
     */
    @Transactional
    public CharacterDTO createCharacter(CharacterDTO characterDTO) {
        log.info("Creating new character: {}", characterDTO.getName());

        // Validate project exists
        Project project = projectRepository.findById(characterDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", characterDTO.getProjectId()));

        Character character = convertToEntity(characterDTO);
        character.setProject(project);

        // Set worldview if provided
        if (characterDTO.getWorldviewId() != null) {
            Worldview worldview = worldviewRepository.findById(characterDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", characterDTO.getWorldviewId()));
            character.setWorldview(worldview);
        }

        Character savedCharacter = characterRepository.save(character);
        return convertToDTO(savedCharacter);
    }

    /**
     * Update an existing character
     */
    @Transactional
    public CharacterDTO updateCharacter(UUID id, CharacterDTO characterDTO) {
        log.info("Updating character with id: {}", id);
        Character existingCharacter = characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));

        // Update basic fields
        existingCharacter.setName(characterDTO.getName());
        existingCharacter.setAge(characterDTO.getAge());
        existingCharacter.setAppearance(characterDTO.getAppearance());
        existingCharacter.setOccupation(characterDTO.getOccupation());
        existingCharacter.setPersonalityTraits(characterDTO.getPersonalityTraits());
        existingCharacter.setBackgroundStory(characterDTO.getBackgroundStory());
        existingCharacter.setChildhoodExperience(characterDTO.getChildhoodExperience());
        existingCharacter.setImportantExperiences(characterDTO.getImportantExperiences());
        existingCharacter.setValuesBeliefs(characterDTO.getValuesBeliefs());
        existingCharacter.setFears(characterDTO.getFears());
        existingCharacter.setDesires(characterDTO.getDesires());
        existingCharacter.setGoals(characterDTO.getGoals());
        existingCharacter.setSpeechPattern(characterDTO.getSpeechPattern());
        existingCharacter.setBehavioralHabits(characterDTO.getBehavioralHabits());
        existingCharacter.setCatchphrases(characterDTO.getCatchphrases());
        existingCharacter.setEmotionalState(characterDTO.getEmotionalState());
        existingCharacter.setRelationships(characterDTO.getRelationships());
        existingCharacter.setPersonalityVector(characterDTO.getPersonalityVector());
        existingCharacter.setCharacterSummary(characterDTO.getCharacterSummary());

        // Update worldview if provided
        if (characterDTO.getWorldviewId() != null) {
            Worldview worldview = worldviewRepository.findById(characterDTO.getWorldviewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worldview", "id", characterDTO.getWorldviewId()));
            existingCharacter.setWorldview(worldview);
        }

        Character updatedCharacter = characterRepository.save(existingCharacter);
        return convertToDTO(updatedCharacter);
    }

    /**
     * Delete a character
     */
    @Transactional
    public void deleteCharacter(UUID id) {
        log.info("Deleting character with id: {}", id);
        if (!characterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Character", "id", id);
        }
        characterRepository.deleteById(id);
    }

    // Conversion methods
    private CharacterDTO convertToDTO(Character character) {
        return CharacterDTO.builder()
                .id(character.getId())
                .projectId(character.getProject().getId())
                .worldviewId(character.getWorldview() != null ? character.getWorldview().getId() : null)
                .name(character.getName())
                .age(character.getAge())
                .appearance(character.getAppearance())
                .occupation(character.getOccupation())
                .personalityTraits(character.getPersonalityTraits())
                .backgroundStory(character.getBackgroundStory())
                .childhoodExperience(character.getChildhoodExperience())
                .importantExperiences(character.getImportantExperiences())
                .valuesBeliefs(character.getValuesBeliefs())
                .fears(character.getFears())
                .desires(character.getDesires())
                .goals(character.getGoals())
                .speechPattern(character.getSpeechPattern())
                .behavioralHabits(character.getBehavioralHabits())
                .catchphrases(character.getCatchphrases())
                .emotionalState(character.getEmotionalState())
                .relationships(character.getRelationships())
                .personalityVector(character.getPersonalityVector())
                .characterSummary(character.getCharacterSummary())
                .createdAt(character.getCreatedAt())
                .updatedAt(character.getUpdatedAt())
                .build();
    }

    private Character convertToEntity(CharacterDTO dto) {
        return Character.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .appearance(dto.getAppearance())
                .occupation(dto.getOccupation())
                .personalityTraits(dto.getPersonalityTraits())
                .backgroundStory(dto.getBackgroundStory())
                .childhoodExperience(dto.getChildhoodExperience())
                .importantExperiences(dto.getImportantExperiences())
                .valuesBeliefs(dto.getValuesBeliefs())
                .fears(dto.getFears())
                .desires(dto.getDesires())
                .goals(dto.getGoals())
                .speechPattern(dto.getSpeechPattern())
                .behavioralHabits(dto.getBehavioralHabits())
                .catchphrases(dto.getCatchphrases())
                .emotionalState(dto.getEmotionalState())
                .relationships(dto.getRelationships())
                .personalityVector(dto.getPersonalityVector())
                .characterSummary(dto.getCharacterSummary())
                .build();
    }
}
