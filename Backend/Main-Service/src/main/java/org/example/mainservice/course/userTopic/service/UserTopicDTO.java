package org.example.mainservice.course.userTopic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.mainservice.course.grade.service.GradeDTO;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.topic.service.TopicDTO;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userTopic.service.internal.TopicStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
public class UserTopicDTO {
    @NotBlank
    @JsonProperty("id")
    private Long id;

    @JsonProperty("commit_link")
    private String commitLink;

    @NotBlank
    @JsonProperty("updated_at")
    private Instant updatedAt;

    @NotBlank
    @JsonProperty("topic_status")
    private TopicStatus topicStatus;

    @NotBlank
    @JsonProperty("topic")
    private TopicDTO topic;

    @NotBlank
    @JsonProperty("user_id")
    private UUID userId;

    @NotBlank
    @JsonProperty("promotion_id")
    private Long promotionId;;
}
