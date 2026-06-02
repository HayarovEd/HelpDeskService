package ru.impuls.helpdeskservice.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.dto.response.UserBillingRequest;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.mapper.UserMapper;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.UserHelperService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.TokenGeneratorUtil;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.CREATED;
import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;


@Service
@Transactional
@RequiredArgsConstructor
public class UserHelperServiceImpl implements UserHelperService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final WebSocketHandler webSocketHandler;
    private final UserMapper userMapper;

    @Override
    public User updateExistingUser(User user, UserBillingRequest response) {
        user.setName(response.name());
        user.setEmail(response.email());
        if (response.password() != null && !response.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(response.password()));
        }
        webSocketHandler.notifyChange("/users", userMapper.toEvent(user), UPDATED);
        return userRepo.save(user);
    }

    @Override
    public User createNewUser(UserBillingRequest response) {
        User newUser = User.builder()
                .name(response.name())
                .username(response.username())
                .email(response.email())
                .password(passwordEncoder.encode(response.password()))
                .disabled(false)
                .tokenVersion(TokenGeneratorUtil.generateNumericToken(8))
                .role(getDefaultRole())
                .build();
        webSocketHandler.notifyChange("/users", userMapper.toEvent(newUser), CREATED);
        return userRepo.save(newUser);
    }

    private Role getDefaultRole() {
        // Получаем роль по умолчанию (например, USER)
        return roleRepo.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorMessage.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }
}
