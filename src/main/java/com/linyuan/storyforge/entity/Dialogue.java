package com.linyuan.storyforge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Dialogue entity - dialogue records
 */
@Data
@Entity
@Table(name = "dialogues")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dialogue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private StoryChapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "speaker_id", nullable = false)
    private Character speaker;

    @Column(name = "listener_ids", columnDefinition = "uuid[]")
    private List<UUID> listenerIds; // 听众角色

    @Column(name = "dialogue_text", nullable = false, columnDefinition = "TEXT")
    private String dialogueText;

    @Column(length = 50)
    private String tone; // 语气

    @Column(length = 50)
    private String emotion; // 情绪

    // 上下文
    @Column(name = "context_before", columnDefinition = "TEXT")
    private String contextBefore;

    @Column(name = "inner_thoughts", columnDefinition = "TEXT")
    private String innerThoughts; // 内心想法

    @Column(name = "body_language", columnDefinition = "TEXT")
    private String bodyLanguage; // 肢体语言

    @Column(name = "sequence_number")
    private Integer sequenceNumber; // 对话顺序
}
