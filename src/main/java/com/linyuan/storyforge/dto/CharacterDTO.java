package com.linyuan.storyforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Character entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterDTO {

    private UUID id;

    private UUID projectId;

    private UUID worldviewId;

    @NotBlank(message = "Character name is required")
    @Size(max = 100, message = "Character name must not exceed 100 characters")
    private String name;

    private Integer age;

    private String appearance;

    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    private List<String> personalityTraits;

    private String backgroundStory;

    private String childhoodExperience;

    private List<Map<String, Object>> importantExperiences;

    private String valuesBeliefs;

    private List<String> fears;

    private List<String> desires;

    private List<String> goals;

    private String speechPattern;

    private List<String> behavioralHabits;

    private List<String> catchphrases;

    private Map<String, Object> emotionalState;

    private Map<String, Object> relationships;

    private List<Float> personalityVector;

    private String characterSummary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
