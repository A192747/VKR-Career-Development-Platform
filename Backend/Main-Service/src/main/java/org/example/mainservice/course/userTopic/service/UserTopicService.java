package org.example.mainservice.course.userTopic.service;

import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.userTopic.service.internal.TopicStatus;
import org.example.mainservice.course.userTopic.service.internal.UserTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface UserTopicService {
    Long save(UserTopic userTopic) throws BadRequestException;
    void update(UserTopic userTopic) throws BadRequestException;
    void updateMy(UserTopic userTopic, UUID userID);
    void delete(Long id);
    UserTopic getById(Long id);
    List<UserTopic> getAllUserTopicByUserId(UUID id);
    Page<UserTopic> getAllUserTopic(int page, int size, Sort sort);
    void setUserTopic(UUID userId, long userTopicId);
    void setUserTopicStatus(long userTopicId, TopicStatus topicStatus);
}
