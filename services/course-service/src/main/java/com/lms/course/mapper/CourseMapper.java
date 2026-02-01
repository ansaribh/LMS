package com.lms.course.mapper;

import com.lms.common.dto.CourseDto;
import com.lms.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ModuleMapper.class})
public interface CourseMapper {

    @Mapping(target = "modules", source = "modules")
    CourseDto toDto(Course course);
}
