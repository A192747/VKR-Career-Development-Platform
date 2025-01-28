package org.example.mainservice.course.promotion.service;

import org.example.mainservice.course.grade.service.GradeMapper;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.userProfile.service.UserProfileMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
                UserProfileMapper.class, GradeMapper.class
        }
)
public interface PromotionMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "promotionDate", source = "promotionDate")
    @Mapping(target = "userProfile", source = "userProfile")
    @Mapping(target = "currentGrade", source = "currentGrade")
    @Mapping(target = "newGrade", source = "newGrade")
    PromotionDTO toDTO(Promotion promotion);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "promotionDate", source = "promotionDate")
    @Mapping(target = "userProfile", source = "userProfile")
    @Mapping(target = "currentGrade", source = "currentGrade")
    @Mapping(target = "newGrade", source = "newGrade")
    Promotion toEntity(PromotionDTO dto);


    @Mapping(target = "userProfile.id", source = "userProfileId")
    @Mapping(target = "newGrade.id", source = "newGradeId")
    Promotion toEntity(PromotionCreateDTO dto);
}
