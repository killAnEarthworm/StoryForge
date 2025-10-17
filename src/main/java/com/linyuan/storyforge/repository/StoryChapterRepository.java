package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.StoryChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for StoryChapter entity
 */
@Repository
public interface StoryChapterRepository extends JpaRepository<StoryChapter, UUID> {

    /**
     * Find chapters by project ID, ordered by chapter number
     */
    List<StoryChapter> findByProjectIdOrderByChapterNumberAsc(UUID projectId);

    /**
     * Find chapters by status
     */
    List<StoryChapter> findByStatus(String status);

    /**
     * Find chapter by project ID and chapter number
     */
    StoryChapter findByProjectIdAndChapterNumber(UUID projectId, Integer chapterNumber);

    /**
     * Find chapters by project ID and status
     */
    List<StoryChapter> findByProjectIdAndStatus(UUID projectId, String status);
}
