package ru.impuls.helpdeskservice.auth.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordReqDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditReqDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.impuls.helpdeskservice.util.TokenGeneratorUtil;

@Mapper(
        componentModel = "spring",
        imports = {TokenGeneratorUtil.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "name", source = "userDto.name" )
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userDto.getPassword()))")
    @Mapping(target = "tokenVersion", expression = "java(TokenGeneratorUtil.generateNumericToken(8))")
    @Mapping(target = "role", source = "role")
    User toEntity(UserEditReqDto userDto, Role role, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "tokenVersion", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "name", source = "userResDto.name")
    @Mapping(target = "email", source = "userResDto.email")
    @Mapping(target = "disabled", source = "userResDto.disabled")
    @Mapping(target = "role", source = "role")
    void updateEntity(@MappingTarget User user, UserResDto userResDto, Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "disabled", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(passwordReqDto.getPassword()))")
    @Mapping(target = "tokenVersion", expression = "java(TokenGeneratorUtil.generateNumericToken(8))")
    void updatePassword(@MappingTarget User user, PasswordReqDto passwordReqDto, @Context PasswordEncoder passwordEncoder);
}