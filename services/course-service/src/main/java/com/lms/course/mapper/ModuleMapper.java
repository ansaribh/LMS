package com.lms.course.mapper;

import com.lms.common.dto.ModuleDto;
import com.lms.course.entity.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LessonMapper.class})
public interface ModuleMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessons", source = "lessons")
    ModuleDto toDto(Module module);
}
