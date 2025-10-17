package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Project entity
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find projects by status
     */
    List<Project> findByStatus(String status);

    /**
     * Find projects by genre
     */
    List<Project> findByGenre(String genre);

    /**
     * Find projects by name containing (case-insensitive search)
     */
    List<Project> findByNameContainingIgnoreCase(String name);
}
