package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.dto.request.PasswordReqDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditReqReqDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResDto> getAll();

    UserResDto create(UserEditReqReqDto userDto);

    UserResDto update(UUID id, UserResDto userResDto);

    UserResDto changePassword(UUID id, PasswordReqDto passwordReqDto);

    String getTokenVersionByUserId(UUID userId);
}
