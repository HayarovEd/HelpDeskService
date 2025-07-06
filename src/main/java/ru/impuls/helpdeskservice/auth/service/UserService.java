package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.dto.request.UserEditDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAll();

    UserDto create(UserEditDto userDto);

    UserDto update(UUID id, UserDto userDto);
}
