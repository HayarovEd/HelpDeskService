package ru.impuls.helpdeskservice.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.impuls.helpdeskservice.auth.dto.request.LoginReqDto;
import ru.impuls.helpdeskservice.auth.dto.response.TokenResDto;

public interface AuthService {

    TokenResDto login(LoginReqDto login, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    TokenResDto refreshToken(HttpServletRequest request, HttpServletResponse response);
}
