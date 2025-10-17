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
 * Character entity - character settings
 */
@Data
@Entity
@Table(name = "characters")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Character extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worldview_id")
    private Worldview worldview;

    // 基础信息层
    @Column(nullable = false, length = 100)
    private String name;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String appearance;

    @Column(length = 100)
    private String occupation;

    @Column(name = "personality_traits", columnDefinition = "varchar(50)[]")
    private List<String> personalityTraits; // 核心性格特征

    // 深度设定层
    @Column(name = "background_story", columnDefinition = "TEXT")
    private String backgroundStory;

    @Column(name = "childhood_experience", columnDefinition = "TEXT")
    private String childhoodExperience;

    @Type(JsonBinaryType.class)
    @Column(name = "important_experiences", columnDefinition = "jsonb")
    private List<Map<String, Object>> importantExperiences; // [{time, event, impact}]

    @Column(name = "values_beliefs", columnDefinition = "TEXT")
    private String valuesBeliefs;

    @Column(columnDefinition = "text[]")
    private List<String> fears;

    @Column(columnDefinition = "text[]")
    private List<String> desires;

    @Column(columnDefinition = "text[]")
    private List<String> goals;

    // 行为特征
    @Column(name = "speech_pattern", columnDefinition = "TEXT")
    private String speechPattern; // 说话方式

    @Column(name = "behavioral_habits", columnDefinition = "text[]")
    private List<String> behavioralHabits; // 行为习惯

    @Column(columnDefinition = "text[]")
    private List<String> catchphrases; // 口癖

    // 动态属性
    @Type(JsonBinaryType.class)
    @Column(name = "emotional_state", columnDefinition = "jsonb")
    private Map<String, Object> emotionalState; // 当前情绪状态

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> relationships; // 与其他角色的关系

    // AI辅助向量化
    @Column(name = "personality_vector", columnDefinition = "float[]")
    private List<Float> personalityVector; // 性格向量化表示

    @Column(name = "character_summary", columnDefinition = "TEXT")
    private String characterSummary; // 角色概要，用于提示词
}
