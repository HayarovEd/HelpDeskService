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
import ru.impuls.helpdeskservice.auth.dto.response.UserBillingResponse;
import ru.impuls.helpdeskservice.auth.dto.response.UserResponse;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.mapper.UserMapper;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.TokenService;
import ru.impuls.helpdeskservice.auth.service.UserService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.constant.TokenType;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.TokenGeneratorUtil;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.*;
import java.util.stream.Collectors;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.*;

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
        if(userRepo.existsByUsername(userCreateRequest.username())) {
            throw new AppException(ErrorMessage.USER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        var role = roleRepo.findByName(userCreateRequest.role().name())
                .orElseThrow(() -> new AppException(ErrorMessage.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
        var user = userMapper.toEntity(userCreateRequest, role, passwordEncoder);

        userRepo.save(user);
        webSocketHandler.notifyChange("/users", userMapper.toEvent(user), CREATED);

        return userMapper.toDto(user);
    }

    @Override
    public UserResponse update(UUID id, UserEditRequest userEditRequest) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        UserResponse oldUserResponse = userMapper.toDto(user);
        var role = roleRepo.findByName(userEditRequest.role().name())
                .orElseThrow(() -> new AppException(ErrorMessage.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));

        userMapper.updateEntity(user, userEditRequest, role);

        if (!oldUserResponse.email().equals(user.getEmail())
                || oldUserResponse.disabled() != user.isDisabled()) {
            user.setTokenVersion(TokenGeneratorUtil.generateNumericToken(8));
            tokenService.deleteAll(user, TokenType.REFRESH);
        }

        userRepo.save(user);
        var resUserDto = userMapper.toDto(user);
        webSocketHandler.notifyChange("/users", userMapper.toEvent(user), UPDATED);

        if (!Objects.equals(oldUserResponse, resUserDto)) {
            webSocketHandler.notifyUser(id, "/account", userMapper.toEvent(user), UPDATED);
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserResponse changePassword(UUID id, PasswordRequest passwordRequest) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        userMapper.updatePassword(user, passwordRequest, passwordEncoder);
        userRepo.save(user);
        tokenService.deleteAll(user, TokenType.REFRESH);
        webSocketHandler.notifyChange("/users", userMapper.toEvent(user), DELETED);

        return userMapper.toDto(user);
    }

    @Override
    public String getTokenVersionByUserId(UUID userId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        return user.getTokenVersion();
    }

    @Override
    public List<UserResponse> updateUsersFromList(List<UserBillingResponse> userResponses) {
        if (userResponses == null || userResponses.isEmpty()) {
            return Collections.emptyList();
        }

        // Извлекаем все username из списка
        Set<String> usernamesFromRequest = userResponses.stream()
                .map(UserBillingResponse::username)
                .collect(Collectors.toSet());

        // Находим существующих пользователей по username из запроса
        List<User> existingUsers = userRepo.findByUsernameIn(usernamesFromRequest);

        // Создаём карту существующих пользователей для быстрого доступа
        Map<String, User> existingUserMap = existingUsers.stream()
                .collect(Collectors.toMap(User::getUsername, user -> user));

        // Обрабатываем каждый элемент из списка
        for (UserBillingResponse response : userResponses) {
            String username = response.username();

            if (existingUserMap.containsKey(username)) {
                // Случай 1: пользователь существует → обновляем
                User existingUser = existingUserMap.get(username);
                updateExistingUser(existingUser, response);
            } else {
                // Случай 2: пользователь не существует → создаём нового
                createNewUser(response);
            }
        }

        // Случай 3: пользователи, которые есть в БД, но отсутствуют в списке - остаются без изменений
        // (ничего не делаем)
        return userRepo.findAll(Sort.by(Sort.Direction.ASC, "email")).stream()
                .map(userMapper::toDto)
                .toList();
    }

    private void updateExistingUser(User user, UserBillingResponse response) {
        // Обновляем только те поля, которые могут измениться
        user.setName(response.name());
        user.setEmail(response.email());

        // Обновляем пароль только если он указан и не пустой
        if (response.password() != null && !response.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(response.password()));
        }
        // Если нужно обновить роль - можно добавить логику
        // user.setRole(roleRepository.findByName(response.role()));
        userRepo.save(user);
    }

    private void createNewUser(UserBillingResponse response) {
        User newUser = User.builder()
                .name(response.name())
                .username(response.username())
                .email(response.email())
                .password(passwordEncoder.encode(response.password()))
                .disabled(false)
                .tokenVersion("0")
                .role(getDefaultRole())
                .build();

        userRepo.save(newUser);
    }

    private Role getDefaultRole() {
        // Получаем роль по умолчанию (например, USER)
        return roleRepo.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorMessage.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }
}
