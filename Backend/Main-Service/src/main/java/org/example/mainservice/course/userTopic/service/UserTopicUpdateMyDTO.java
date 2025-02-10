package org.example.mainservice.course.userTopic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.mainservice.course.userTopic.service.internal.TopicStatus;

@Data
@Getter
@Setter
public class UserTopicUpdateMyDTO {
    @NotBlank
    @JsonProperty("user_topic_id")
    private Long id;

    @JsonProperty("commit_link")
    private String commitLink;

    @JsonProperty("topic_status")
    private TopicStatus topicStatus;

}
