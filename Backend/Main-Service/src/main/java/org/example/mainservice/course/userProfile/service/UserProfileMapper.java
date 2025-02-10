package org.example.mainservice.course.userProfile.service;

import org.example.mainservice.course.grade.service.GradeMapper;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = GradeMapper.class)
public interface UserProfileMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "grade", source = "grade")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    UserProfileDTO toDTO(UserProfile userProfile);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "grade", source = "grade")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    UserProfile toEntity(UserProfileDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    UserProfile toEntity(UserProfileCreateDTO dto);
}
