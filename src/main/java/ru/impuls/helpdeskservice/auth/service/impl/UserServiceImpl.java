package ru.impuls.helpdeskservice.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserCreateRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserResponse;
import ru.impuls.helpdeskservice.auth.mapper.UserMapper;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.TokenService;
import ru.impuls.helpdeskservice.auth.service.UserService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.constant.TokenType;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.TokenGeneratorUtil;
import ru.impuls.helpdeskservice.websocket.EventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;
    private final WebSocketHandler webSocketHandler;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAll() {
        return userRepo.findAll(Sort.by(Sort.Direction.ASC, "email")).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponse create(UserCreateRequest userCreateRequest) {
        if(userRepo.existsByEmail(userCreateRequest.email())) {
            throw new AppException(
                    ErrorMessage.USER_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        var role = roleRepo.findByName(userCreateRequest.role().name())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.ROLE_NOT_FOUND,
                        HttpStatus.BAD_REQUEST
                ));
        var user = userMapper.toEntity(userCreateRequest, role, passwordEncoder);
        user = userRepo.save(user);
        var resUserDto = userMapper.toDto(user);
        webSocketHandler.notifyChange("/users", resUserDto, EventType.CREATED);

        return resUserDto;
    }

    @Override
    public UserResponse update(UUID id, UserEditRequest userEditRequest) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
        UserResponse oldUserResponse = userMapper.toDto(user);
        var role = roleRepo.findByName(userEditRequest.role().name())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.ROLE_NOT_FOUND,
                        HttpStatus.BAD_REQUEST
                ));

        userMapper.updateEntity(user, userEditRequest, role);

        if (!oldUserResponse.email().equals(user.getEmail())
                || oldUserResponse.disabled() != user.isDisabled()
        ) {
            user.setTokenVersion(TokenGeneratorUtil.generateNumericToken(8));
            tokenService.deleteAll(user, TokenType.REFRESH);
        }

        user = userRepo.save(user);
        var resUserDto = userMapper.toDto(user);
        webSocketHandler.notifyChange("/users", resUserDto, EventType.UPDATED);

        if (!Objects.equals(oldUserResponse, resUserDto)) {
            webSocketHandler.notifyUser(id, "/account", resUserDto, EventType.UPDATED);
        }

        return resUserDto;
    }

    @Override
    public UserResponse changePassword(UUID id, PasswordRequest passwordRequest) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        userMapper.updatePassword(user, passwordRequest, passwordEncoder);
        userRepo.save(user);
        tokenService.deleteAll(user, TokenType.REFRESH);

        var resUserDto = userMapper.toDto(user);
        webSocketHandler.notifyChange("/users", resUserDto, EventType.DELETED);

        return resUserDto;
    }

    @Override
    public String getTokenVersionByUserId(UUID userId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found with ID:" + userId,
                        HttpStatus.NOT_FOUND
                ));
        return user.getTokenVersion();
    }
}
