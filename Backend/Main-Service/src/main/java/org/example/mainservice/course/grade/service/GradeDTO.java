package org.example.mainservice.course.grade.service;

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
public class GradeDTO {
    @JsonProperty("id")
    private Long id;
    @NotBlank
    @JsonProperty("name")
    private String name;
}
