package ru.impuls.helpdeskservice.auth.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.auth.dto.RoleDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.mapper.UserMapper;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto toDto(User user) {
        if(user == null) return null;

        var roleDto = RoleDto.builder()
                .id(user.getRole().getId())
                .name(user.getRole().getName())
                .build();

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .isDisabled(user.isDisabled())
                .role(roleDto)
                .build();
    }

    @Override
    public User toEntity(UserEditDto userDto, Role role) {
        if(userDto == null) return null;

        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .isDisabled(userDto.isDisabled())
                .role(role)
                .build();
    }

    @Override
    public void toEntity(User user, UserDto userDto, Role role) {
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setDisabled(userDto.isDisabled());
        user.setRole(role);
    }
}
