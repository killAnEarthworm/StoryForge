package com.linyuan.storyforge.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Story Chapter entity - story chapters
 */
@Data
@Entity
@Table(name = "story_chapters")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryChapter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    private String title;

    // 章节设定
    @Column(columnDefinition = "TEXT")
    private String outline; // 章节大纲

    @Column(name = "main_conflict", columnDefinition = "TEXT")
    private String mainConflict; // 主要冲突

    @Column(name = "participating_characters", columnDefinition = "uuid[]")
    private List<UUID> participatingCharacters; // 参与角色

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_scene_id")
    private Scene mainScene;

    // 生成参数
    @Column(name = "target_word_count")
    private Integer targetWordCount;

    @Column(length = 50)
    private String tone; // 基调

    @Column(length = 30)
    private String pacing; // 节奏

    // 生成内容
    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    @Type(JsonBinaryType.class)
    @Column(name = "generation_params", columnDefinition = "jsonb")
    private Map<String, Object> generationParams;

    @Column
    @Builder.Default
    private Integer version = 1;

    @Column(length = 20)
    @Builder.Default
    private String status = "outline"; // outline/drafted/revised/final
}
