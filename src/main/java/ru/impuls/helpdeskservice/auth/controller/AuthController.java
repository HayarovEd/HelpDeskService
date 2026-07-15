package ru.impuls.helpdeskservice.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.auth.dto.request.LoginRequest;
import ru.impuls.helpdeskservice.auth.dto.response.TokenResponse;
import ru.impuls.helpdeskservice.auth.dto.response.UnregisteredUserRequest;
import ru.impuls.helpdeskservice.auth.dto.response.UserBillingRequest;
import ru.impuls.helpdeskservice.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse servletResponse) {
        return authService.login(loginRequest, servletResponse);
    }

    @PostMapping("/login_from_app")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse loginUserByOutApp(@RequestBody @Valid UserBillingRequest userBillingRequest, HttpServletResponse servletResponse) {
        return authService.loginUserByOutApp(userBillingRequest, servletResponse);
    }

    @PostMapping("/unregistered_login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse loginUnregisteredUser(@RequestBody @Valid UnregisteredUserRequest unregisteredUserRequest, HttpServletResponse servletResponse) {
        return authService.loginUnregisteredUser(unregisteredUserRequest, servletResponse);
    }

    @PostMapping("logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        authService.logout(servletRequest, servletResponse);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refreshToken(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return authService.refreshToken(servletRequest, servletResponse);
    }

}
