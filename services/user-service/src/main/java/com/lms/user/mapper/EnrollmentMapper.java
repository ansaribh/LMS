package com.lms.user.mapper;

import com.lms.common.dto.EnrollmentDto;
import com.lms.user.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(enrollment.getUser().getFullName())")
    EnrollmentDto toDto(Enrollment enrollment);
}
