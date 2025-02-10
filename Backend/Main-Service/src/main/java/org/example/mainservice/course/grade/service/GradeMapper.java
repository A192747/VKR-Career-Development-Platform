package org.example.mainservice.course.grade.service;

import org.example.mainservice.course.grade.service.internal.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    GradeDTO toDTO(Grade grade);
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Grade toEntity(GradeDTO dto);
    @Mapping(target = "name", source = "name")
    Grade toEntity(GradeCreateDTO dto);
}
