package com.linyuan.storyforge.repository;

import com.linyuan.storyforge.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Dialogue entity
 */
@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, UUID> {

    /**
     * Find dialogues by chapter ID, ordered by sequence number
     */
    List<Dialogue> findByChapterIdOrderBySequenceNumberAsc(UUID chapterId);

    /**
     * Find dialogues by scene ID, ordered by sequence number
     */
    List<Dialogue> findBySceneIdOrderBySequenceNumberAsc(UUID sceneId);

    /**
     * Find dialogues by speaker ID
     */
    List<Dialogue> findBySpeakerId(UUID speakerId);

    /**
     * Find dialogues by speaker ID and chapter ID
     */
    List<Dialogue> findBySpeakerIdAndChapterId(UUID speakerId, UUID chapterId);

    /**
     * Find dialogues by emotion
     */
    List<Dialogue> findByEmotion(String emotion);

    /**
     * Find dialogues by tone
     */
    List<Dialogue> findByTone(String tone);

    /**
     * Count dialogues by chapter ID
     */
    long countByChapterId(UUID chapterId);

    /**
     * Count dialogues by speaker ID
     */
    long countBySpeakerId(UUID speakerId);

    /**
     * Get the maximum sequence number in a chapter
     */
    @Query("SELECT MAX(d.sequenceNumber) FROM Dialogue d WHERE d.chapter.id = :chapterId")
    Integer findMaxSequenceNumberByChapterId(@Param("chapterId") UUID chapterId);
}
