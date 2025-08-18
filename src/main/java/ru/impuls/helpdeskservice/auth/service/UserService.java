package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.dto.request.PasswordRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserCreateRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> getAll();

    UserResponse create(UserCreateRequest userCreateRequest);

    UserResponse update(UUID id, UserEditRequest userEditRequest);

    UserResponse changePassword(UUID id, PasswordRequest passwordRequest);

    String getTokenVersionByUserId(UUID userId);
}
