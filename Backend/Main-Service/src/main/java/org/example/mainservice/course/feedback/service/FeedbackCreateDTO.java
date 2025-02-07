package org.example.mainservice.course.feedback.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class FeedbackCreateDTO {

    @NotBlank
    @JsonProperty("text")
    private String text;

    @NotBlank
    @JsonProperty("reviewer_user_id")
    private UUID reviewerUserProfileId;

    @NotBlank
    @JsonProperty("reviewed_user_id")
    private UUID reviewedUserProfileId;

    @NotBlank
    @JsonProperty("confirmed")
    private boolean confirmed;

    @NotBlank
    @JsonProperty("promotion_id")
    private Long promotionId;

    @NotBlank
    @JsonProperty("related_type")
    private String relatedType;

    @JsonProperty("related_id")
    private Long relatedId;

}
