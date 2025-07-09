package ru.impuls.helpdeskservice.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.auth.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid UserEditDto userDto) {
        return userService.create(userDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@PathVariable UUID id, @RequestBody UserDto userDto) {
        return userService.update(id, userDto);
    }

    @PatchMapping("/{id}/change-password")
    @ResponseStatus(HttpStatus.OK)
    public UserDto changePassword(@PathVariable UUID id, @RequestBody @Valid PasswordDto passwordDto) {
        return userService.changePassword(id, passwordDto);
    }
}
