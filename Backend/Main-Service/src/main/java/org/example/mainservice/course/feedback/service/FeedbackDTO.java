package org.example.mainservice.course.feedback.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class FeedbackDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("reviewer_user_id")
    private UUID reviewerUserProfileId;

    @JsonProperty("reviewed_user_id")
    private UUID reviewedUserProfileId;

    @JsonProperty("confirmed")
    private boolean confirmed;

    @JsonProperty("promotion_id")
    private Long promotionId;

    @JsonProperty("related_type")
    private String relatedType;

    @JsonProperty("related_id")
    private Long relatedId;

    @JsonProperty("created_at")
    private Instant createdAt;
}
