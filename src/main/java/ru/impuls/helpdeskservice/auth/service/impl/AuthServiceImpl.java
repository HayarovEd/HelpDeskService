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
import ru.impuls.helpdeskservice.auth.dto.request.LoginReqReqDto;
import ru.impuls.helpdeskservice.auth.dto.response.TokenResDto;
import ru.impuls.helpdeskservice.auth.entity.Token;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.TokenRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.auth.service.AuthService;
import ru.impuls.helpdeskservice.auth.service.JwtService;
import ru.impuls.helpdeskservice.auth.service.TokenService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.constant.RefreshTokenPosition;
import ru.impuls.helpdeskservice.constant.TokenType;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.CookieUtil;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;
    private final TokenRepo tokenRepo;

    /************************ login ***************************/
    @Override
    public TokenResDto login(LoginReqReqDto loginReqDto, HttpServletResponse response) {
        var user = userRepo.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        validateUserForLogin(user, loginReqDto);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken();

        tokenService.save(user, refreshToken, TokenType.REFRESH);
        cookieUtil.addRefreshTokenToCookie(response, refreshToken);

        return TokenResDto.builder()
                .token(accessToken)
                .build();
    }

    /************************ logout ***************************/
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            var token = fetchTokenFromDatabase(refreshToken, response);

            tokenService.delete(token);
            cookieUtil.deleteRefreshTokenFromCookies(response);
        }
    }

    /************************ refresh ***************************/
    @Override
    public TokenResDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new AppException(
                    ErrorMessage.REFRESH_NOT_FOUND_IN_COOKIE,
                    HttpStatus.UNAUTHORIZED
            );
        }

        var token = fetchTokenFromDatabase(refreshToken, response);
        validateTokenExpiration(token);

        var user = Optional.ofNullable(token.getUser())
                .orElseThrow(() -> new AppException(
                        "User associated with refresh token not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.isDisabled()) {
            cookieUtil.deleteRefreshTokenFromCookies(response);
            throw new AppException(
                    "Account with email " + user.getEmail() + " is blocked or not verified",
                    HttpStatus.FORBIDDEN
            );
        }

        List<Token> existingTokens = tokenRepo.findByUserAndTokenType(user, TokenType.REFRESH);

        if (token.getPosition() != null) {
            if (token.getPosition() == RefreshTokenPosition.LAST) {
                existingTokens.stream()
                        .filter(t -> t.getPosition() == RefreshTokenPosition.PREVIOUS)
                        .findFirst()
                        .ifPresent(tokenRepo::delete);

                token.setPosition(RefreshTokenPosition.PREVIOUS);
                tokenRepo.save(token);
            } else if (token.getPosition() == RefreshTokenPosition.PREVIOUS) {
                existingTokens.stream()
                        .filter(t -> t.getPosition() == RefreshTokenPosition.LAST)
                        .findFirst()
                        .ifPresent(tokenRepo::delete);
            } else {
                cookieUtil.deleteRefreshTokenFromCookies(response);
                throw new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }
        }

        var newAccessToken = jwtService.generateAccessToken(user);
        var newRefreshToken = jwtService.generateRefreshToken();

        tokenService.save(user, newRefreshToken, TokenType.REFRESH);
        cookieUtil.addRefreshTokenToCookie(response, newRefreshToken);

        return TokenResDto.builder()
                .token(newAccessToken)
                .build();
    }

    private void validateUserForLogin(User user, LoginReqReqDto loginReqDto) {
        if (!user.isEnabled()) {
            throw new AppException(
                    ErrorMessage.USER_BLOCKED,
                    HttpStatus.FORBIDDEN);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginReqDto.getEmail(),
                            loginReqDto.getPassword()
                    ));
        } catch (BadCredentialsException e) {
            throw new AppException(
                    ErrorMessage.INVALID_LOGIN_OR_PASSWORD,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private Token fetchTokenFromDatabase(String refreshToken, HttpServletResponse response) {
        return tokenRepo.findByTokenAndTokenType(refreshToken, TokenType.REFRESH)
                .orElseThrow(() -> {
                    cookieUtil.deleteRefreshTokenFromCookies(response);
                    return new AppException("Refresh token not found in database", HttpStatus.UNAUTHORIZED);
                });
    }

    private void validateTokenExpiration(Token token) {
        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new AppException("Refresh token expired. Please login again", HttpStatus.UNAUTHORIZED);
        }
    }
}
