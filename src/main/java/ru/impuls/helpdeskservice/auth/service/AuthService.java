package ru.impuls.helpdeskservice.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.impuls.helpdeskservice.auth.dto.request.LoginRequest;
import ru.impuls.helpdeskservice.auth.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest loginRequest, HttpServletResponse servletResponse);

    void logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse);

    TokenResponse refreshToken(HttpServletRequest servletRequest, HttpServletResponse servletResponse);
}
