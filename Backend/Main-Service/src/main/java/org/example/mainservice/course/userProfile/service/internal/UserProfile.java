package org.example.mainservice.course.userProfile.service.internal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.course.feedback.service.internal.Feedback;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.schedule.service.internal.Schedule;
import org.example.mainservice.course.userTopic.service.internal.UserTopic;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @Email
    private String email;

    @Column(name = "date_of_birth", nullable = false)
    private Instant dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_grade_id", nullable = false)
    private Grade grade;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<Promotion> promotions;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<UserTopic> userTopics;

    @OneToMany(mappedBy = "reviewerUserProfile", fetch = FetchType.LAZY)
    private List<Feedback> givenReviews;

    @OneToMany(mappedBy = "reviewedUserProfile", fetch = FetchType.LAZY)
    private List<Feedback> receivedReviews;

    @OneToMany(mappedBy = "reviewerUserProfile", fetch = FetchType.LAZY)
    private List<Schedule> scheduleGivenReviews;

    @OneToMany(mappedBy = "reviewedUserProfile", fetch = FetchType.LAZY)
    private List<Schedule> scheduleReceivedReviews;
}
