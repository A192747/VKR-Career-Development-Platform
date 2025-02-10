package org.example.mainservice.course.userTopic.service.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
}
