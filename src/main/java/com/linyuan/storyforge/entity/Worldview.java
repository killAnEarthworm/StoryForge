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

/**
 * Worldview entity - world-building settings
 */
@Data
@Entity
@Table(name = "worldviews")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worldview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    // 世界观层次结构
    @Type(JsonBinaryType.class)
    @Column(name = "universe_laws", columnDefinition = "jsonb")
    private Map<String, Object> universeLaws; // 宇宙法则

    @Type(JsonBinaryType.class)
    @Column(name = "social_structure", columnDefinition = "jsonb")
    private Map<String, Object> socialStructure; // 社会结构

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> geography; // 地理环境

    @Type(JsonBinaryType.class)
    @Column(name = "history_background", columnDefinition = "jsonb")
    private Map<String, Object> historyBackground; // 历史背景

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> terminology; // 专有名词词典

    // AI辅助字段
    @Column(columnDefinition = "TEXT")
    private String summary; // 世界观概要

    @Column(columnDefinition = "text[]")
    private List<String> rules; // 世界规则列表

    @Column(columnDefinition = "text[]")
    private List<String> constraints; // 禁忌和限制
}
