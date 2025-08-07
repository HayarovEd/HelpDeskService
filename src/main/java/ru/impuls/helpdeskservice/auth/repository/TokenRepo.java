package ru.impuls.helpdeskservice.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.auth.entity.Token;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.constant.TokenType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepo extends JpaRepository<Token, UUID> {
    Optional<Token> findByTokenAndTokenType(String refreshToken, TokenType tokenType);

    void deleteAllByUserIdAndTokenType(UUID id, TokenType tokenType);

    List<Token> findByUserAndTokenType(User user, TokenType tokenType);
}
