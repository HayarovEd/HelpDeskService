package ru.impuls.helpdeskservice.auth.mapper;

import ru.impuls.helpdeskservice.auth.dto.request.PasswordDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;

public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserEditDto userDto, Role role);

    void toEntity(User user, UserDto userDto, Role role);

    void toEntity(User user, PasswordDto passwordDto);
}
