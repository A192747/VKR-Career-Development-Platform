package org.example.mainservice.llm.service.frontendDto;

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
public class AnswerDTO {
    @JsonProperty("reference_answer")
    @NotBlank
    private String referenceAnswer;
    @JsonProperty("hash")
    @NotBlank
    private String hash;
}
