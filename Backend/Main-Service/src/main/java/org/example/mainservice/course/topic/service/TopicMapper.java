package org.example.mainservice.course.topic.service;

import org.example.mainservice.course.topic.service.internal.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TopicDTO toDTO(Topic topic);
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Topic toEntity(TopicDTO dto);
    @Mapping(target = "name", source = "name")
    Topic toEntity(TopicCreateDTO dto);
}
