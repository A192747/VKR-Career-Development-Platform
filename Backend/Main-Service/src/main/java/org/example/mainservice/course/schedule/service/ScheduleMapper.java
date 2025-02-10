package org.example.mainservice.course.schedule.service;

import org.example.mainservice.course.promotion.service.PromotionMapper;
import org.example.mainservice.course.schedule.service.internal.Schedule;
import org.example.mainservice.course.userProfile.service.UserProfileMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
                UserProfileMapper.class, PromotionMapper.class
        })
public interface ScheduleMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "reviewerUserProfileId", source = "reviewerUserProfile.id")
    @Mapping(target = "reviewedUserProfileId", source = "reviewedUserProfile.id")
    @Mapping(target = "ready", source = "ready")
    @Mapping(target = "promotionId", source = "promotion.id")
    @Mapping(target = "interviewFormat", source = "interviewFormat")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "beginAt", source = "beginAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    ScheduleDTO toDTO(Schedule schedule);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "ready", source = "ready")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "interviewFormat", source = "interviewFormat")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "beginAt", source = "beginAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    Schedule toEntity(ScheduleDTO dto);


    @Mapping(target = "name", source = "name")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "interviewFormat", source = "interviewFormat")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "beginAt", source = "beginAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    Schedule toEntity(ScheduleCreateDTO dto);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "ready", source = "ready")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "interviewFormat", source = "interviewFormat")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "beginAt", source = "beginAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    Schedule toEntity(ScheduleUpdateDTO dto);
}
