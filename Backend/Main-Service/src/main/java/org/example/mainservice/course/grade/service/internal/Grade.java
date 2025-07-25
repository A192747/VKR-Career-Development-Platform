package org.example.mainservice.course.grade.service.internal;

import jakarta.persistence.*;
import lombok.*;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;

import java.util.List;

@Entity
@Table(name = "grade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "grade_topic",
            joinColumns = @JoinColumn(name = "grade_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;


    @OneToMany(mappedBy = "grade", fetch = FetchType.LAZY)
    private List<UserProfile> userProfiles;
}
