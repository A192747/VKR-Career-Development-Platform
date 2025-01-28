package org.example.mainservice.course.userTopic.service;

import org.example.mainservice.course.grade.service.GradeMapper;
import org.example.mainservice.course.topic.service.TopicMapper;
import org.example.mainservice.course.userTopic.service.internal.UserTopic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TopicMapper.class)
public interface UserTopicMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "commitLink", source = "commitLink")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "topicStatus", source = "topicStatus")
    @Mapping(target = "topic", source = "topic")
    @Mapping(target = "userId", source = "userProfile.id")
    @Mapping(target = "promotionId", source = "promotion.id")
    UserTopicDTO toDTO(UserTopic userTopic);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "commitLink", source = "commitLink")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "topicStatus", source = "topicStatus")
    @Mapping(target = "topic", source = "topic")
    @Mapping(target = "userProfile.id", source = "userId")
    @Mapping(target = "promotion.id", source = "promotionId")
    UserTopic toEntity(UserTopicDTO dto);

    @Mapping(target = "topic.id", source = "topicId")
    @Mapping(target = "userProfile.id", source = "userProfileId")
    @Mapping(target = "promotion.id", source = "promotionId")
    UserTopic toEntity(UserTopicCreateDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "commitLink", source = "commitLink")
    @Mapping(target = "topicStatus", source = "topicStatus")
    UserTopic toEntity(UserTopicUpdateMyDTO dto);
}
