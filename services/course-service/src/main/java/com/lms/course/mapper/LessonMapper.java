package com.lms.course.mapper;

import com.lms.common.dto.LessonDto;
import com.lms.course.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "isFree", source = "free")
    LessonDto toDto(Lesson lesson);
}
