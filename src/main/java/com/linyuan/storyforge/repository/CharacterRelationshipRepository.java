package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CharacterRelationship entity
 */
@Repository
public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, UUID> {

    /**
     * Find relationships by character A ID
     */
    List<CharacterRelationship> findByCharacterAId(UUID characterAId);

    /**
     * Find relationships by character B ID
     */
    List<CharacterRelationship> findByCharacterBId(UUID characterBId);

    /**
     * Find all relationships for a specific character (either as A or B)
     */
    @Query("SELECT cr FROM CharacterRelationship cr WHERE cr.characterA.id = :characterId OR cr.characterB.id = :characterId")
    List<CharacterRelationship> findByCharacterId(@Param("characterId") UUID characterId);

    /**
     * Find relationship between two specific characters
     */
    @Query("SELECT cr FROM CharacterRelationship cr WHERE " +
           "(cr.characterA.id = :characterAId AND cr.characterB.id = :characterBId) OR " +
           "(cr.characterA.id = :characterBId AND cr.characterB.id = :characterAId)")
    Optional<CharacterRelationship> findByCharacterIds(
            @Param("characterAId") UUID characterAId,
            @Param("characterBId") UUID characterBId);

    /**
     * Find relationships by type
     */
    List<CharacterRelationship> findByRelationshipType(String relationshipType);

    /**
     * Check if relationship exists between two characters
     */
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CharacterRelationship cr WHERE " +
           "(cr.characterA.id = :characterAId AND cr.characterB.id = :characterBId) OR " +
           "(cr.characterA.id = :characterBId AND cr.characterB.id = :characterAId)")
    boolean existsByCharacterIds(
            @Param("characterAId") UUID characterAId,
            @Param("characterBId") UUID characterBId);
}
