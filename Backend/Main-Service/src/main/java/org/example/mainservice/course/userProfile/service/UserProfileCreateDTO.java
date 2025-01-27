package org.example.mainservice.course.userProfile.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.mainservice.course.grade.service.GradeDTO;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
public class UserProfileCreateDTO {
    @NotBlank
    @JsonProperty("id")
    private UUID id;
    @NotBlank
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank
    @JsonProperty("email")
    @Email
    private String email;

    @NotBlank
    @JsonProperty("date_of_birth")
    private Instant dateOfBirth;

    @NotBlank
    @JsonProperty("gradeId")
    private Long gradeId;
}
