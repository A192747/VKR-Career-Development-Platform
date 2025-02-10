package org.example.mainservice.course.feedback.service;

import org.example.mainservice.course.feedback.service.internal.Feedback;
import org.example.mainservice.course.promotion.service.PromotionMapper;
import org.example.mainservice.course.userProfile.service.UserProfileMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
                UserProfileMapper.class, PromotionMapper.class
        })
public interface FeedbackMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "text", source = "text")
    @Mapping(target = "reviewerUserProfileId", source = "reviewerUserProfile.id")
    @Mapping(target = "reviewedUserProfileId", source = "reviewedUserProfile.id")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "promotionId", source = "promotion.id")
    @Mapping(target = "relatedType", source = "relatedType")
    @Mapping(target = "relatedId", source = "relatedId")
    @Mapping(target = "createdAt", source = "createdAt")
    FeedbackDTO toDTO(Feedback feedback);

    @Mapping(target = "text", source = "text")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "relatedType", source = "relatedType")
    @Mapping(target = "relatedId", source = "relatedId")
    @Mapping(target = "createdAt", source = "createdAt")
    Feedback toEntity(FeedbackDTO dto);


    @Mapping(target = "text", source = "text")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "relatedType", source = "relatedType")
    @Mapping(target = "relatedId", source = "relatedId")
    Feedback toEntity(FeedbackCreateDTO dto);


    @Mapping(target = "text", source = "text")
    @Mapping(target = "reviewerUserProfile.id", source = "reviewerUserProfileId")
    @Mapping(target = "reviewedUserProfile.id", source = "reviewedUserProfileId")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "promotion.id", source = "promotionId")
    @Mapping(target = "relatedType", source = "relatedType")
    @Mapping(target = "relatedId", source = "relatedId")
    Feedback toEntity(FeedbackUpdateDTO dto);
}
