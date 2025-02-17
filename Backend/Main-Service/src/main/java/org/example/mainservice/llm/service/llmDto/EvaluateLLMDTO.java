package org.example.mainservice.llm.service.llmDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class EvaluateLLMDTO {
    @JsonProperty("reference_answer")
    private String referenceAnswer;
    @JsonProperty("user_answer")
    private String userAnswer;
}
