package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.entity.Token;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.constant.TokenType;

public interface TokenService {

    void save(User user, String generatedToken, TokenType tokenTypeName);

    void delete(Token token);

    void deleteAll(User user, TokenType tokenTypeName);
}