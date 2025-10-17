package com.linyuan.storyforge.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Timeline entity - event timeline for characters
 */
@Data
@Entity
@Table(name = "timelines")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timeline extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "event_time")
    private LocalDateTime eventTime; // 绝对时间

    @Column(name = "relative_time", length = 100)
    private String relativeTime; // 相对时间描述

    @Column(name = "event_type", length = 50)
    private String eventType; // 对话/行动/内心变化/环境变化

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    // 事件详情
    @Column(name = "participating_characters", columnDefinition = "uuid[]")
    private List<UUID> participatingCharacters; // 参与的其他角色ID

    @Column(name = "location_id")
    private UUID locationId; // 关联场景

    @Type(JsonBinaryType.class)
    @Column(name = "emotional_changes", columnDefinition = "jsonb")
    private Map<String, Object> emotionalChanges; // 情绪变化

    @Column(name = "memory_importance")
    private Integer memoryImportance; // 记忆重要度 1-10

    // 对后续影响
    @Column(columnDefinition = "text[]")
    private List<String> consequences;

    @Type(JsonBinaryType.class)
    @Column(name = "character_growth", columnDefinition = "jsonb")
    private Map<String, Object> characterGrowth; // 角色成长变化
}
