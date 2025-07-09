package ru.impuls.helpdeskservice.auth.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.dto.request.LoginDto;
import ru.impuls.helpdeskservice.auth.dto.response.TokenDto;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.AuthService;
import ru.impuls.helpdeskservice.auth.service.JwtService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.CookieUtil;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;

    /************************ login ***************************/
    @Override
    @Transactional(readOnly = true)
    public TokenDto login(LoginDto loginDto, HttpServletResponse response) {
        var user = userRepo.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        validateUserForLogin(user, loginDto);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        cookieUtil.addRefreshTokenToCookie(response, refreshToken);

        return TokenDto.builder()
                .token(accessToken)
                .build();
    }

    /************************ logout ***************************/
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            cookieUtil.deleteRefreshTokenFromCookies(response);
        }
    }

    /************************ refresh ***************************/
    @Override
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new AppException(
                    ErrorMessage.REFRESH_NOT_FOUND_IN_COOKIE,
                    HttpStatus.UNAUTHORIZED
            );
        }

        var userId = jwtService.extractId(refreshToken);
        var user = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
        var newAccessToken = jwtService.generateAccessToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        cookieUtil.deleteRefreshTokenFromCookies(response);
        cookieUtil.addRefreshTokenToCookie(response, newRefreshToken);

        return TokenDto.builder()
                .token(newAccessToken)
                .build();
    }

    private void validateUserForLogin(User user, LoginDto loginDto) {
        if (!user.isEnabled()) {
            throw new AppException(
                    ErrorMessage.USER_BLOCKED,
                    HttpStatus.FORBIDDEN);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    ));
        } catch (BadCredentialsException e) {
            throw new AppException(
                    ErrorMessage.INVALID_LOGIN_OR_PASSWORD,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
}
