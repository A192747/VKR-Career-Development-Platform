package org.example.mainservice.course.promotion.service.internal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_date", nullable = false)
    private Instant promotionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_grade_id")
    private Grade currentGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_grade_id")
    private Grade newGrade;
}
