package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.DialogueDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.Dialogue;
import com.linyuan.storyforge.entity.Scene;
import com.linyuan.storyforge.entity.StoryChapter;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.repository.DialogueRepository;
import com.linyuan.storyforge.repository.SceneRepository;
import com.linyuan.storyforge.repository.StoryChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing dialogues
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialogueService {

    private final DialogueRepository dialogueRepository;
    private final StoryChapterRepository chapterRepository;
    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;

    /**
     * Get all dialogues
     */
    @Transactional(readOnly = true)
    public List<DialogueDTO> getAllDialogues() {
        log.debug("Fetching all dialogues");
        return dialogueRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get dialogue by ID
     */
    @Transactional(readOnly = true)
    public DialogueDTO getDialogueById(UUID id) {
        log.debug("Fetching dialogue with id: {}", id);
        Dialogue dialogue = dialogueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dialogue", "id", id));
        return convertToDTO(dialogue);
    }

    /**
     * Get dialogues by chapter ID
     */
    @Transactional(readOnly = true)
    public List<DialogueDTO> getDialoguesByChapterId(UUID chapterId) {
        log.debug("Fetching dialogues for chapter: {}", chapterId);
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("StoryChapter", "id", chapterId);
        }
        return dialogueRepository.findByChapterIdOrderBySequenceNumberAsc(chapterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get dialogues by scene ID
     */
    @Transactional(readOnly = true)
    public List<DialogueDTO> getDialoguesBySceneId(UUID sceneId) {
        log.debug("Fetching dialogues for scene: {}", sceneId);
        if (!sceneRepository.existsById(sceneId)) {
            throw new ResourceNotFoundException("Scene", "id", sceneId);
        }
        return dialogueRepository.findBySceneIdOrderBySequenceNumberAsc(sceneId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get dialogues by speaker ID
     */
    @Transactional(readOnly = true)
    public List<DialogueDTO> getDialoguesBySpeakerId(UUID speakerId) {
        log.debug("Fetching dialogues for speaker: {}", speakerId);
        if (!characterRepository.existsById(speakerId)) {
            throw new ResourceNotFoundException("Character", "id", speakerId);
        }
        return dialogueRepository.findBySpeakerId(speakerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get dialogues by emotion
     */
    @Transactional(readOnly = true)
    public List<DialogueDTO> getDialoguesByEmotion(String emotion) {
        log.debug("Fetching dialogues with emotion: {}", emotion);
        return dialogueRepository.findByEmotion(emotion).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new dialogue
     */
    @Transactional
    public DialogueDTO createDialogue(DialogueDTO dialogueDTO) {
        log.info("Creating new dialogue for speaker: {}", dialogueDTO.getSpeakerId());

        // Validate chapter exists
        StoryChapter chapter = chapterRepository.findById(dialogueDTO.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("StoryChapter", "id", dialogueDTO.getChapterId()));

        // Validate speaker exists
        Character speaker = characterRepository.findById(dialogueDTO.getSpeakerId())
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", dialogueDTO.getSpeakerId()));

        // Validate scene if provided
        Scene scene = null;
        if (dialogueDTO.getSceneId() != null) {
            scene = sceneRepository.findById(dialogueDTO.getSceneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", dialogueDTO.getSceneId()));
        }

        // Auto-increment sequence number if not provided
        if (dialogueDTO.getSequenceNumber() == null) {
            Integer maxSequence = dialogueRepository.findMaxSequenceNumberByChapterId(dialogueDTO.getChapterId());
            dialogueDTO.setSequenceNumber(maxSequence != null ? maxSequence + 1 : 1);
        }

        Dialogue dialogue = convertToEntity(dialogueDTO);
        dialogue.setChapter(chapter);
        dialogue.setScene(scene);
        dialogue.setSpeaker(speaker);
        Dialogue savedDialogue = dialogueRepository.save(dialogue);
        return convertToDTO(savedDialogue);
    }

    /**
     * Update an existing dialogue
     */
    @Transactional
    public DialogueDTO updateDialogue(UUID id, DialogueDTO dialogueDTO) {
        log.info("Updating dialogue with id: {}", id);
        Dialogue existingDialogue = dialogueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dialogue", "id", id));

        // Update scene if provided
        if (dialogueDTO.getSceneId() != null &&
            (existingDialogue.getScene() == null || !existingDialogue.getScene().getId().equals(dialogueDTO.getSceneId()))) {
            Scene scene = sceneRepository.findById(dialogueDTO.getSceneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scene", "id", dialogueDTO.getSceneId()));
            existingDialogue.setScene(scene);
        }

        // Update fields
        existingDialogue.setListenerIds(dialogueDTO.getListenerIds());
        existingDialogue.setDialogueText(dialogueDTO.getDialogueText());
        existingDialogue.setTone(dialogueDTO.getTone());
        existingDialogue.setEmotion(dialogueDTO.getEmotion());
        existingDialogue.setContextBefore(dialogueDTO.getContextBefore());
        existingDialogue.setInnerThoughts(dialogueDTO.getInnerThoughts());
        existingDialogue.setBodyLanguage(dialogueDTO.getBodyLanguage());
        existingDialogue.setSequenceNumber(dialogueDTO.getSequenceNumber());

        Dialogue updatedDialogue = dialogueRepository.save(existingDialogue);
        return convertToDTO(updatedDialogue);
    }

    /**
     * Delete a dialogue
     */
    @Transactional
    public void deleteDialogue(UUID id) {
        log.info("Deleting dialogue with id: {}", id);
        if (!dialogueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dialogue", "id", id);
        }
        dialogueRepository.deleteById(id);
    }

    /**
     * Count dialogues by chapter ID
     */
    @Transactional(readOnly = true)
    public long countDialoguesByChapterId(UUID chapterId) {
        log.debug("Counting dialogues for chapter: {}", chapterId);
        return dialogueRepository.countByChapterId(chapterId);
    }

    // Conversion methods
    private DialogueDTO convertToDTO(Dialogue dialogue) {
        DialogueDTO dto = new DialogueDTO();
        dto.setId(dialogue.getId());
        dto.setChapterId(dialogue.getChapter().getId());
        dto.setSceneId(dialogue.getScene() != null ? dialogue.getScene().getId() : null);
        dto.setSpeakerId(dialogue.getSpeaker().getId());
        dto.setListenerIds(dialogue.getListenerIds());
        dto.setDialogueText(dialogue.getDialogueText());
        dto.setTone(dialogue.getTone());
        dto.setEmotion(dialogue.getEmotion());
        dto.setContextBefore(dialogue.getContextBefore());
        dto.setInnerThoughts(dialogue.getInnerThoughts());
        dto.setBodyLanguage(dialogue.getBodyLanguage());
        dto.setSequenceNumber(dialogue.getSequenceNumber());
        dto.setCreatedAt(dialogue.getCreatedAt());
        dto.setUpdatedAt(dialogue.getUpdatedAt());
        return dto;
    }

    private Dialogue convertToEntity(DialogueDTO dto) {
        return Dialogue.builder()
                .listenerIds(dto.getListenerIds())
                .dialogueText(dto.getDialogueText())
                .tone(dto.getTone())
                .emotion(dto.getEmotion())
                .contextBefore(dto.getContextBefore())
                .innerThoughts(dto.getInnerThoughts())
                .bodyLanguage(dto.getBodyLanguage())
                .sequenceNumber(dto.getSequenceNumber())
                .build();
    }
}
