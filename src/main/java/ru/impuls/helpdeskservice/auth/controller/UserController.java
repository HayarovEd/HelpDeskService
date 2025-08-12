package ru.impuls.helpdeskservice.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordReqDto;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditReqReqDto;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;
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
    public List<UserResDto> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResDto create(@RequestBody @Valid UserEditReqReqDto userDto) {
        return userService.create(userDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResDto update(@PathVariable UUID id, @RequestBody UserResDto userResDto) {
        return userService.update(id, userResDto);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.OK)
    public UserResDto changePassword(@PathVariable UUID id, @RequestBody @Valid PasswordReqDto passwordReqDto) {
        return userService.changePassword(id, passwordReqDto);
    }
}
