package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.GenerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for GenerationHistory entity
 */
@Repository
public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, UUID> {

    /**
     * Find generation history by project ID
     */
    List<GenerationHistory> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    /**
     * Find generation history by generation type
     */
    List<GenerationHistory> findByGenerationTypeOrderByCreatedAtDesc(String generationType);

    /**
     * Find generation history by target ID
     */
    List<GenerationHistory> findByTargetIdOrderByCreatedAtDesc(UUID targetId);

    /**
     * Find generation history by model name
     */
    List<GenerationHistory> findByModelNameOrderByCreatedAtDesc(String modelName);

    /**
     * Find high-quality generations (quality score above threshold)
     */
    @Query("SELECT gh FROM GenerationHistory gh WHERE gh.qualityScore >= :threshold ORDER BY gh.qualityScore DESC")
    List<GenerationHistory> findHighQualityGenerations(@Param("threshold") Float threshold);

    /**
     * Find generations with user feedback
     */
    @Query("SELECT gh FROM GenerationHistory gh WHERE gh.userFeedback IS NOT NULL ORDER BY gh.createdAt DESC")
    List<GenerationHistory> findGenerationsWithFeedback();

    /**
     * Count generations by project ID
     */
    long countByProjectId(UUID projectId);

    /**
     * Count generations by generation type
     */
    long countByGenerationType(String generationType);

    /**
     * Get average quality score by generation type
     */
    @Query("SELECT AVG(gh.qualityScore) FROM GenerationHistory gh WHERE gh.generationType = :generationType AND gh.qualityScore IS NOT NULL")
    Float getAverageQualityScoreByType(@Param("generationType") String generationType);
}
