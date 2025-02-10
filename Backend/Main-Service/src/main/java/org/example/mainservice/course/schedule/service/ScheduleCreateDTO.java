package org.example.mainservice.course.schedule.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mainservice.course.schedule.service.internal.InterviewFormat;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ScheduleCreateDTO {

    @NotBlank
    @JsonProperty("meeting_name")
    private String name;

    @NotBlank
    @JsonProperty("reviewer_user_id")
    private UUID reviewerUserProfileId;

    @NotBlank
    @JsonProperty("reviewed_user_id")
    private UUID reviewedUserProfileId;

    @NotBlank
    @JsonProperty("promotion_id")
    private Long promotionId;

    @NotBlank
    @JsonProperty("interview_format")
    private InterviewFormat interviewFormat;

    @NotBlank
    @JsonProperty("comment")
    private String comment;

    @NotBlank
    @JsonProperty("time_begin")
    private Instant beginAt;

    @NotBlank
    @JsonProperty("time_end")
    private Instant finishedAt;

}
