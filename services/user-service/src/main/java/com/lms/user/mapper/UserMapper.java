package com.lms.user.mapper;

import com.lms.common.dto.UserDto;
import com.lms.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "keycloakId", source = "keycloakId")
    UserDto toDto(User user);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "parents", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserDto dto);
}
