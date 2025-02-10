package org.example.mainservice.course.schedule.service;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ScheduleUpdateDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("reviewer_user_id")
    private UUID reviewerUserProfileId;

    @JsonProperty("reviewed_user_id")
    private UUID reviewedUserProfileId;

    @JsonProperty("promotion_id")
    private Long promotionId;

    @JsonProperty("meeting_name")
    private String name;

    @JsonProperty("interview_format")
    private InterviewFormat interviewFormat;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("ready_status")
    private boolean isReady;

    @JsonProperty("time_begin")
    private Instant beginAt;

    @JsonProperty("time_end")
    private Instant finishedAt;
}
