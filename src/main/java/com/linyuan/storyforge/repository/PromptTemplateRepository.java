package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PromptTemplate entity
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {

    /**
     * Find templates by category
     */
    List<PromptTemplate> findByCategory(String category);

    /**
     * Find active templates
     */
    List<PromptTemplate> findByIsActiveTrue();

    /**
     * Find active templates by category
     */
    List<PromptTemplate> findByCategoryAndIsActiveTrue(String category);

    /**
     * Find template by name
     */
    Optional<PromptTemplate> findByName(String name);

    /**
     * Find high-performing templates (effectiveness score above threshold)
     */
    @Query("SELECT pt FROM PromptTemplate pt WHERE pt.effectivenessScore >= :threshold ORDER BY pt.effectivenessScore DESC")
    List<PromptTemplate> findHighPerformingTemplates(@Param("threshold") Float threshold);

    /**
     * Find templates by name containing (case-insensitive search)
     */
    List<PromptTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Check if template exists by name
     */
    boolean existsByName(String name);

    /**
     * Count active templates
     */
    long countByIsActiveTrue();

    /**
     * Count templates by category
     */
    long countByCategory(String category);
}
