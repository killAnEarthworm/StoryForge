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
 * Character Relationship entity
 */
@Data
@Entity
@Table(name = "character_relationships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"character_a_id", "character_b_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_a_id", nullable = false)
    private Character characterA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_b_id", nullable = false)
    private Character characterB;

    @Column(name = "relationship_type", length = 50)
    private String relationshipType; // 父子/朋友/敌人/恋人等

    @Column(name = "relationship_description", columnDefinition = "TEXT")
    private String relationshipDescription;

    @Column(name = "tension_points", columnDefinition = "text[]")
    private List<String> tensionPoints; // 冲突点

    @Column(name = "shared_history", columnDefinition = "TEXT")
    private String sharedHistory;

    @Type(JsonBinaryType.class)
    @Column(name = "dynamic_state", columnDefinition = "jsonb")
    private Map<String, Object> dynamicState; // 关系动态变化
}
