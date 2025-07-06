package ru.impuls.helpdeskservice.auth.service;

import org.springframework.security.core.GrantedAuthority;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;

import java.time.Instant;
import java.util.Collection;

public interface JwtService {

    String extractEmail(String jwt);

    String extractId(String jwt);

    String extractTokenType(String jwt);

    Role extractRole(String jwt);

    Instant extractExpiration(String token);

    Collection<? extends GrantedAuthority> extractAuthorities(String jwt);

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    boolean isTokenExpired(String token);

    boolean isTokenSignatureValid(String refreshToken);

    boolean isRefreshToken(String token);

    boolean isAccessToken(String token);
}
