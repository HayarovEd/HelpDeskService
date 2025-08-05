package ru.impuls.helpdeskservice.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.Token;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.TokenRepo;
import ru.impuls.helpdeskservice.auth.service.TokenService;
import ru.impuls.helpdeskservice.constant.TokenType;
import ru.impuls.helpdeskservice.exception.AppException;

import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepo tokenRepository;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public void save(User user, String generatedToken, TokenType tokenType) {

        Instant createdDate = Instant.now();
        Instant expiresDate = calculateExpirationDate(tokenType, createdDate);

        var token = Token.builder()
                .token(generatedToken)
                .createdAt(createdDate)
                .expiresAt(expiresDate)
                .tokenType(tokenType)
                .user(user)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public void delete(Token token) {
        tokenRepository.delete(token);
    }

    @Override
    public void deleteAll(User user, TokenType tokenType) {
        tokenRepository.deleteAllByUserIdAndTokenType(user.getId(), tokenType);
    }

    /**
     * Рассчитывает срок действия токена в зависимости от его типа.
     */
    private Instant calculateExpirationDate(TokenType tokenTypeName, Instant createdDate) {
        Duration tokenDuration = switch (tokenTypeName) {
            case REFRESH -> Duration.ofMinutes(refreshTokenExpiration);
            case ACCESS -> throw new AppException(
                    "ACCESS token type should not have an expiration managed here.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        };

        return createdDate.plus(tokenDuration);
    }
}

