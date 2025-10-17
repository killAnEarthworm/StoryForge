package com.linyuan.storyforge.entity;

import lombok.AllArgsConstructor;

import javax.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project entity - manages different creative projects
 */
@Data
@Entity
@Table(name = "projects")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String genre; // 科幻/古风/魔幻/悬疑等

    @Column(columnDefinition = "TEXT")
    private String theme; // 主题：牺牲与救赎等

    @Column(name = "writing_style", columnDefinition = "TEXT")
    private String writingStyle;

    @Column(length = 20)
    @Builder.Default
    private String status = "draft"; // draft/in_progress/completed
}
