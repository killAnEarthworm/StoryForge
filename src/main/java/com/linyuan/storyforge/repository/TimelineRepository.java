package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Timeline entity
 */
@Repository
public interface TimelineRepository extends JpaRepository<Timeline, UUID> {

    /**
     * Find timelines by project ID
     */
    List<Timeline> findByProjectId(UUID projectId);

    /**
     * Find timelines by character ID, ordered by event time
     */
    List<Timeline> findByCharacterIdOrderByEventTimeAsc(UUID characterId);

    /**
     * Find timelines by character ID and event type
     */
    List<Timeline> findByCharacterIdAndEventType(UUID characterId, String eventType);

    /**
     * Find timelines by project ID, ordered by event time
     */
    List<Timeline> findByProjectIdOrderByEventTimeAsc(UUID projectId);

    /**
     * Find timelines by event type
     */
    List<Timeline> findByEventType(String eventType);

    /**
     * Find timelines within a time range
     */
    @Query("SELECT t FROM Timeline t WHERE t.eventTime BETWEEN :startTime AND :endTime ORDER BY t.eventTime ASC")
    List<Timeline> findByEventTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find important memories (importance >= threshold)
     */
    @Query("SELECT t FROM Timeline t WHERE t.character.id = :characterId AND t.memoryImportance >= :threshold ORDER BY t.memoryImportance DESC")
    List<Timeline> findImportantMemories(
            @Param("characterId") UUID characterId,
            @Param("threshold") Integer threshold);

    /**
     * Count timelines by character ID
     */
    long countByCharacterId(UUID characterId);

    /**
     * Count timelines by project ID
     */
    long countByProjectId(UUID projectId);
}
