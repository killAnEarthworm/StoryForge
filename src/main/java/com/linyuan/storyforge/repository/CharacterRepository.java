package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Character entity
 */
@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID> {

    /**
     * Find characters by project ID
     */
    List<Character> findByProjectId(UUID projectId);

    /**
     * Find characters by worldview ID
     */
    List<Character> findByWorldviewId(UUID worldviewId);

    /**
     * Find character by name and project ID
     */
    Character findByNameAndProjectId(String name, UUID projectId);

    /**
     * Find characters by occupation
     */
    List<Character> findByOccupation(String occupation);

    /**
     * Count characters in a project
     */
    @Query("SELECT COUNT(c) FROM Character c WHERE c.project.id = :projectId")
    long countByProjectId(UUID projectId);
}
