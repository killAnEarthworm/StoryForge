package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.CharacterMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CharacterMemory entity
 */
@Repository
public interface CharacterMemoryRepository extends JpaRepository<CharacterMemory, UUID> {

    /**
     * Find memories by character ID
     */
    List<CharacterMemory> findByCharacterId(UUID characterId);

    /**
     * Find memories by character ID and memory type
     */
    List<CharacterMemory> findByCharacterIdAndMemoryType(UUID characterId, String memoryType);

    /**
     * Find memories by timeline ID
     */
    List<CharacterMemory> findByTimelineId(UUID timelineId);

    /**
     * Find accessible memories (accessibility above threshold)
     */
    @Query("SELECT cm FROM CharacterMemory cm WHERE cm.character.id = :characterId AND cm.accessibility >= :threshold ORDER BY cm.accessibility DESC")
    List<CharacterMemory> findAccessibleMemories(
            @Param("characterId") UUID characterId,
            @Param("threshold") Float threshold);

    /**
     * Find memories by emotional weight (above threshold)
     */
    @Query("SELECT cm FROM CharacterMemory cm WHERE cm.character.id = :characterId AND cm.emotionalWeight >= :threshold ORDER BY cm.emotionalWeight DESC")
    List<CharacterMemory> findEmotionalMemories(
            @Param("characterId") UUID characterId,
            @Param("threshold") Float threshold);

    /**
     * Find most accessed memories
     */
    @Query("SELECT cm FROM CharacterMemory cm WHERE cm.character.id = :characterId ORDER BY cm.accessCount DESC")
    List<CharacterMemory> findMostAccessedMemories(@Param("characterId") UUID characterId);

    /**
     * Find memories by memory type
     */
    List<CharacterMemory> findByMemoryType(String memoryType);

    /**
     * Count memories by character ID
     */
    long countByCharacterId(UUID characterId);

    /**
     * Count memories by character ID and memory type
     */
    long countByCharacterIdAndMemoryType(UUID characterId, String memoryType);
}
