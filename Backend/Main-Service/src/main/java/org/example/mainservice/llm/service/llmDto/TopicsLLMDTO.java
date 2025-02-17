package org.example.mainservice.llm.service.llmDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class TopicsLLMDTO {
    @JsonProperty("topics")
    private List<String> topics;
    @JsonProperty("num_questions")
    private Integer numQuestions;
}
