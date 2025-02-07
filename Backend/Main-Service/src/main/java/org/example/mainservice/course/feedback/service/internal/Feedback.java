package org.example.mainservice.course.feedback.service.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private UserProfile reviewerUserProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private UserProfile reviewedUserProfile;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "related_type", nullable = false)
    private String relatedType;

    @Column(name = "related_id", nullable = false)
    private Long relatedId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
