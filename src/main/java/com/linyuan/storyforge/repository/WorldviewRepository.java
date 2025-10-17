package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Worldview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Worldview entity
 */
@Repository
public interface WorldviewRepository extends JpaRepository<Worldview, UUID> {

    /**
     * Find worldviews by project ID
     */
    List<Worldview> findByProjectId(UUID projectId);

    /**
     * Find worldview by name and project ID
     */
    Worldview findByNameAndProjectId(String name, UUID projectId);

    /**
     * Count worldviews by project ID
     */
    long countByProjectId(UUID projectId);

    /**
     * Check if a worldview exists by name and project ID
     */
    boolean existsByNameAndProjectId(String name, UUID projectId);
}
