package org.example.mainservice.course.userTopic.service;

import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userTopic.service.internal.TopicStatus;
import org.example.mainservice.course.userTopic.service.internal.UserTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface UserTopicService {
    void update(UserTopic userTopic) throws BadRequestException;

    void updateMy(UserTopic userTopic, UUID userID);

    void delete(UserTopic userTopic);

    UserTopic findById(Long id);

    List<UserTopic> getAllUserTopicByUserId(UUID id);

    Page<UserTopic> getAllUserTopic(int page, int size, Sort sort);

    void setUserTopic(UUID userId, long userTopicId);

    void setUserTopicStatus(long userTopicId, TopicStatus topicStatus);

    void save(UserProfile currentUserProfile, Grade newGrade, Topic topic, Promotion promotion);
}
