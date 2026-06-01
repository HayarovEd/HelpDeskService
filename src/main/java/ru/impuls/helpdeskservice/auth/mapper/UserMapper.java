package ru.impuls.helpdeskservice.auth.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserCreateRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserResponse;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.impuls.helpdeskservice.util.TokenGeneratorUtil;
import ru.impuls.helpdeskservice.websocket.event.UserEvent;

@Mapper(
        componentModel = "spring",
        imports = {TokenGeneratorUtil.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResponse toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "name", source = "userCreateRequest.name" )
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userCreateRequest.password()))")
    @Mapping(target = "tokenVersion", expression = "java(TokenGeneratorUtil.generateNumericToken(8))")
    @Mapping(target = "role", source = "role")
    User toEntity(UserCreateRequest userCreateRequest, Role role, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "tokenVersion", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "name", source = "userEditRequest.name")
    @Mapping(target = "username", source = "userEditRequest.username")
    @Mapping(target = "email", source = "userEditRequest.email")
    @Mapping(target = "disabled", source = "userEditRequest.disabled")
    @Mapping(target = "role", source = "role")
    void updateEntity(@MappingTarget User user, UserEditRequest userEditRequest, Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "disabled", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(passwordRequest.password()))")
    @Mapping(target = "tokenVersion", expression = "java(TokenGeneratorUtil.generateNumericToken(8))")
    void updatePassword(@MappingTarget User user, PasswordRequest passwordRequest, @Context PasswordEncoder passwordEncoder);

    UserEvent toEvent(User user);
}