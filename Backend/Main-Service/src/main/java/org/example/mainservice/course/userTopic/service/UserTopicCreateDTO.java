package org.example.mainservice.course.userTopic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
public class UserTopicCreateDTO {
    @NotBlank
    @JsonProperty("topic_id")
    private Long topicId;

    @NotBlank
    @JsonProperty("user_id")
    private UUID userProfileId;

    @NotBlank
    @JsonProperty("promotion_id")
    private Long promotionId;
}
