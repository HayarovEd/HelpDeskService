package ru.impuls.helpdeskservice.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.auth.mapper.UserMapper;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.UserService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;

    @Override
    public List<UserDto> getAll() {
        return userRepo.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto create(UserEditDto userDto) {
        if(userRepo.existsByEmail(userDto.getEmail())) {
            throw new AppException(
                    ErrorMessage.USER_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        var role = roleRepo.findByName(userDto.getRole().getName())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.ROLE_NOT_FOUND,
                        HttpStatus.BAD_REQUEST
                ));
        var user = userMapper.toEntity(userDto, role);
        user = userRepo.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public UserDto update(UUID id, UserDto userDto) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
        var role = roleRepo.findByName(userDto.getRole().getName())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.ROLE_NOT_FOUND,
                        HttpStatus.BAD_REQUEST
                ));

        userMapper.toEntity(user, userDto, role);
        user = userRepo.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public UserDto changePassword(UUID id, PasswordDto passwordDto) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        userMapper.toEntity(user, passwordDto);
        userRepo.save(user);

        return userMapper.toDto(user);
    }
}
