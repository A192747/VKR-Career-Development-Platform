package org.example.mainservice.llm.service.frontendDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class TopicsDTO {
    @JsonProperty("topics_ids")
    @NotBlank
    private List<Long> topicsIds;
    @NotBlank
    @Min(value = 1)
    @Max(value = 5)
    @JsonProperty("num_questions")
    private Integer numQuestions;
}
