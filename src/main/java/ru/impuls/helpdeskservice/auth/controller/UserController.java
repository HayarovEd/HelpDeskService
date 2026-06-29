package ru.impuls.helpdeskservice.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.auth.dto.request.PasswordRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserCreateRequest;
import ru.impuls.helpdeskservice.auth.dto.request.UserEditRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserBillingRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserResponse;
import ru.impuls.helpdeskservice.auth.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public UserResponse create(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        return userService.create(userCreateRequest);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public UserResponse update(@PathVariable UUID id, @RequestBody UserEditRequest userEditRequest) {
        return userService.update(id, userEditRequest);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public UserResponse changePassword(@PathVariable UUID id, @RequestBody @Valid PasswordRequest passwordRequest) {
        return userService.changePassword(id, passwordRequest);
    }

    @PutMapping("/sync")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> syncUsers(@Valid @RequestBody List<UserBillingRequest> userResponses) {
        return userService.updateUsersFromList(userResponses);
    }
}
