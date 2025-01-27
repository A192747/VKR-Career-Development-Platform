package org.example.mainservice.course.promotion.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.mainservice.course.grade.service.GradeDTO;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.userProfile.service.UserProfileDTO;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
public class PromotionDTO {

    @JsonProperty("id")
    private Long id;

    @NotBlank
    @JsonProperty("promotion_date")
    private Instant promotionDate;

    @NotBlank
    @JsonProperty("user_profile")
    private UserProfileDTO userProfile;

    @NotBlank
    @JsonProperty("current_grade")
    private GradeDTO currentGrade;

    @NotBlank
    @JsonProperty("new_grade")
    private GradeDTO newGrade;
}
