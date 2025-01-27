package org.example.mainservice.course.promotion.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class PromotionCreateDTO {

    @NotBlank
    @JsonProperty("user_profile_id")
    private UUID userProfileId;

    @NotBlank
    @JsonProperty("current_grade_id")
    private Long currentGradeId;

    @NotBlank
    @JsonProperty("new_grade_id")
    private Long newGradeId;
}
