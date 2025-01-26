package org.example.mainservice.course.topic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class TopicDTO {
    @JsonProperty("id")
    private Long id;
    @NotBlank
    @JsonProperty("name")
    private String name;
}
