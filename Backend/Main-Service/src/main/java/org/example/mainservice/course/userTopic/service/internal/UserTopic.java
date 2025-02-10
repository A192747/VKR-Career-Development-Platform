package org.example.mainservice.course.userTopic.service.internal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_topic")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "commit_link")
    private String commitLink;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "topic_status", nullable = false)
    private TopicStatus topicStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
}
