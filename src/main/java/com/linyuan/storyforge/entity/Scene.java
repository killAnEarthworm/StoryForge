package com.linyuan.storyforge.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

import java.util.List;
import java.util.Map;

/**
 * Scene entity - scene settings
 */
@Data
@Entity
@Table(name = "scenes")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scene extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worldview_id")
    private Worldview worldview;

    @Column(nullable = false)
    private String name;

    @Column(name = "location_type", length = 50)
    private String locationType; // 室内/室外/虚拟等

    // 场景设定
    @Column(name = "physical_description", columnDefinition = "TEXT")
    private String physicalDescription;

    @Column(name = "time_setting", length = 100)
    private String timeSetting; // 时间背景

    @Column(columnDefinition = "TEXT")
    private String atmosphere; // 氛围/情绪基调

    @Column(length = 50)
    private String weather;

    @Column(length = 50)
    private String lighting;

    // 环境元素
    @Type(type = "jsonb")
    @Column(name = "available_props", columnDefinition = "jsonb")
    private Map<String, Object> availableProps; // 可用道具

    @Column(name = "environmental_elements", columnDefinition = "text[]")
    private List<String> environmentalElements; // 环境元素

    @Type(type = "jsonb")
    @Column(name = "sensory_details", columnDefinition = "jsonb")
    private Map<String, Object> sensoryDetails; // 感官细节

    // AI辅助
    @Column(name = "scene_summary", columnDefinition = "TEXT")
    private String sceneSummary;

    @Column(name = "mood_keywords", columnDefinition = "varchar(30)[]")
    private List<String> moodKeywords;
}
