package org.example.mainservice.course.schedule.service.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private UserProfile reviewerUserProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private UserProfile reviewedUserProfile;

    @Column(name = "ready_status", nullable = false)
    private boolean isReady;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "interview_format", nullable = false)
    @Enumerated(EnumType.STRING)
    private InterviewFormat interviewFormat;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "time_begin", nullable = false)
    private Instant beginAt;

    @Column(name = "time_end", nullable = false)
    private Instant finishedAt;
}
