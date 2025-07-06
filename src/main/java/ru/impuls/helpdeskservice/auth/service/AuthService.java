package ru.impuls.helpdeskservice.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.impuls.helpdeskservice.auth.dto.request.LoginDto;
import ru.impuls.helpdeskservice.auth.dto.response.TokenDto;

public interface AuthService {

    TokenDto login(LoginDto login, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response);
}
