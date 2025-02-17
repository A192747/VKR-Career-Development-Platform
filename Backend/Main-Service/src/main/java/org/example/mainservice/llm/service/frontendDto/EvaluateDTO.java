package org.example.mainservice.llm.service.frontendDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class EvaluateDTO {
    @JsonProperty("answer_dto")
    @Valid
    private AnswerDTO answer;
    @JsonProperty("user_answer")
    @NotBlank
    private String userAnswer;
}
