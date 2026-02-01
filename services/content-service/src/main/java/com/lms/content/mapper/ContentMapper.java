package com.lms.content.mapper;

import com.lms.content.dto.ContentDto;
import com.lms.content.entity.Content;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    ContentDto toDto(Content content);
}
