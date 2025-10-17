package com.linyuan.storyforge.service;

import com.linyuan.storyforge.dto.TimelineDTO;
import com.linyuan.storyforge.entity.Character;
import com.linyuan.storyforge.entity.Project;
import com.linyuan.storyforge.entity.Timeline;
import com.linyuan.storyforge.exception.ResourceNotFoundException;
import com.linyuan.storyforge.repository.CharacterRepository;
import com.linyuan.storyforge.repository.ProjectRepository;
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
 * Service for managing timelines
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final ProjectRepository projectRepository;
    private final CharacterRepository characterRepository;

    /**
     * Get all timelines
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getAllTimelines() {
        log.debug("Fetching all timelines");
        return timelineRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get timeline by ID
     */
    @Transactional(readOnly = true)
    public TimelineDTO getTimelineById(UUID id) {
        log.debug("Fetching timeline with id: {}", id);
        Timeline timeline = timelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline", "id", id));
        return convertToDTO(timeline);
    }

    /**
     * Get timelines by project ID
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getTimelinesByProjectId(UUID projectId) {
        log.debug("Fetching timelines for project: {}", projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return timelineRepository.findByProjectIdOrderByEventTimeAsc(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get timelines by character ID
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getTimelinesByCharacterId(UUID characterId) {
        log.debug("Fetching timelines for character: {}", characterId);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return timelineRepository.findByCharacterIdOrderByEventTimeAsc(characterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get timelines by event type
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getTimelinesByEventType(String eventType) {
        log.debug("Fetching timelines with event type: {}", eventType);
        return timelineRepository.findByEventType(eventType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get timelines within a time range
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getTimelinesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Fetching timelines between {} and {}", startTime, endTime);
        return timelineRepository.findByEventTimeBetween(startTime, endTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get important memories for a character
     */
    @Transactional(readOnly = true)
    public List<TimelineDTO> getImportantMemories(UUID characterId, Integer threshold) {
        log.debug("Fetching important memories for character: {} with threshold: {}", characterId, threshold);
        if (!characterRepository.existsById(characterId)) {
            throw new ResourceNotFoundException("Character", "id", characterId);
        }
        return timelineRepository.findImportantMemories(characterId, threshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new timeline event
     */
    @Transactional
    public TimelineDTO createTimeline(TimelineDTO timelineDTO) {
        log.info("Creating new timeline event for character: {}", timelineDTO.getCharacterId());

        // Validate project exists
        Project project = projectRepository.findById(timelineDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", timelineDTO.getProjectId()));

        // Validate character exists
        Character character = characterRepository.findById(timelineDTO.getCharacterId())
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", timelineDTO.getCharacterId()));

        Timeline timeline = convertToEntity(timelineDTO);
        timeline.setProject(project);
        timeline.setCharacter(character);
        Timeline savedTimeline = timelineRepository.save(timeline);
        return convertToDTO(savedTimeline);
    }

    /**
     * Update an existing timeline
     */
    @Transactional
    public TimelineDTO updateTimeline(UUID id, TimelineDTO timelineDTO) {
        log.info("Updating timeline with id: {}", id);
        Timeline existingTimeline = timelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeline", "id", id));

        // Update fields
        existingTimeline.setEventTime(timelineDTO.getEventTime());
        existingTimeline.setRelativeTime(timelineDTO.getRelativeTime());
        existingTimeline.setEventType(timelineDTO.getEventType());
        existingTimeline.setEventDescription(timelineDTO.getEventDescription());
        existingTimeline.setParticipatingCharacters(timelineDTO.getParticipatingCharacters());
        existingTimeline.setLocationId(timelineDTO.getLocationId());
        existingTimeline.setEmotionalChanges(timelineDTO.getEmotionalChanges());
        existingTimeline.setMemoryImportance(timelineDTO.getMemoryImportance());
        existingTimeline.setConsequences(timelineDTO.getConsequences());
        existingTimeline.setCharacterGrowth(timelineDTO.getCharacterGrowth());

        Timeline updatedTimeline = timelineRepository.save(existingTimeline);
        return convertToDTO(updatedTimeline);
    }

    /**
     * Delete a timeline
     */
    @Transactional
    public void deleteTimeline(UUID id) {
        log.info("Deleting timeline with id: {}", id);
        if (!timelineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Timeline", "id", id);
        }
        timelineRepository.deleteById(id);
    }

    /**
     * Count timelines by character ID
     */
    @Transactional(readOnly = true)
    public long countTimelinesByCharacterId(UUID characterId) {
        log.debug("Counting timelines for character: {}", characterId);
        return timelineRepository.countByCharacterId(characterId);
    }

    // Conversion methods
    private TimelineDTO convertToDTO(Timeline timeline) {
        TimelineDTO dto = new TimelineDTO();
        dto.setId(timeline.getId());
        dto.setProjectId(timeline.getProject().getId());
        dto.setCharacterId(timeline.getCharacter().getId());
        dto.setEventTime(timeline.getEventTime());
        dto.setRelativeTime(timeline.getRelativeTime());
        dto.setEventType(timeline.getEventType());
        dto.setEventDescription(timeline.getEventDescription());
        dto.setParticipatingCharacters(timeline.getParticipatingCharacters());
        dto.setLocationId(timeline.getLocationId());
        dto.setEmotionalChanges(timeline.getEmotionalChanges());
        dto.setMemoryImportance(timeline.getMemoryImportance());
        dto.setConsequences(timeline.getConsequences());
        dto.setCharacterGrowth(timeline.getCharacterGrowth());
        dto.setCreatedAt(timeline.getCreatedAt());
        dto.setUpdatedAt(timeline.getUpdatedAt());
        return dto;
    }

    private Timeline convertToEntity(TimelineDTO dto) {
        return Timeline.builder()
                .eventTime(dto.getEventTime())
                .relativeTime(dto.getRelativeTime())
                .eventType(dto.getEventType())
                .eventDescription(dto.getEventDescription())
                .participatingCharacters(dto.getParticipatingCharacters())
                .locationId(dto.getLocationId())
                .emotionalChanges(dto.getEmotionalChanges())
                .memoryImportance(dto.getMemoryImportance())
                .consequences(dto.getConsequences())
                .characterGrowth(dto.getCharacterGrowth())
                .build();
    }
}
