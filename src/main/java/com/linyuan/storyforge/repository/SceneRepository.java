package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Scene entity
 */
@Repository
public interface SceneRepository extends JpaRepository<Scene, UUID> {

    /**
     * Find scenes by project ID
     */
    List<Scene> findByProjectId(UUID projectId);

    /**
     * Find scenes by worldview ID
     */
    List<Scene> findByWorldviewId(UUID worldviewId);

    /**
     * Find scenes by location type
     */
    List<Scene> findByLocationType(String locationType);

    /**
     * Count scenes by project ID
     */
    long countByProjectId(UUID projectId);

    /**
     * Check if a scene exists by name and project ID
     */
    boolean existsByNameAndProjectId(String name, UUID projectId);
}
