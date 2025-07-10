package ru.impuls.helpdeskservice.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.auth.dto.TokenClaimDto;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.service.JwtService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.util.KeyProviderUtil;

import java.nio.file.NoSuchFileException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.access-token.expiration}")
    private int accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Value("${application.security.jwt.keys.public}")
    private String publicKeyFromFile;

    @Value("${application.security.jwt.keys.private}")
    private String privateKeyFromFile;

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @Override
    public String extractEmail(String jwt) {
        try {
            return extractClaim(jwt, claims -> claims.get("email", String.class));
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public Role extractRole(String jwt) {
        try {
            String roleName = extractClaim(jwt, claims -> claims.get("role", String.class));
            return new Role(roleName);
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN + "\n" + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public String extractId(String jwt) {
        try {
            return extractClaim(jwt, Claims::getSubject);
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public String extractTokenType(String jwt) {
        try {
            return extractClaim(jwt, claims -> claims.get("tokenType", String.class));
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public Instant extractExpiration(String token) {
        try {
            return extractClaim(token, claims ->
                    claims.getExpiration().toInstant());
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> extractAuthorities(String jwt) {
        try {
            Role role = extractRole(jwt);

            if (role == null) {
                throw new AppException(
                        ErrorMessage.MISSING_JWT_ROLES,
                        HttpStatus.UNAUTHORIZED
                );
            }

            return Collections.singletonList(
                    new SimpleGrantedAuthority(role.getName())
            );
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public String generateAccessToken(User user) {
        try {
            TokenClaimDto claims = new TokenClaimDto(
                    user.getRole(),
                    TOKEN_TYPE_ACCESS,
                    user.getEmail(),
                    user.getName()
            );
            return generateToken(user.getId(), accessTokenExpiration, claims.toMap());
        } catch (Exception e) {
            throw new AppException(ErrorMessage.JWT_GENERATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String generateRefreshToken(User user) {
        try {
            TokenClaimDto claims = new TokenClaimDto(
                    null,
                    TOKEN_TYPE_REFRESH,
                    null,
                    null
            );
            return generateToken(user.getId(), refreshTokenExpiration, claims.toMap());
        } catch (Exception e) {
            throw new AppException(ErrorMessage.JWT_GENERATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateToken(UUID sub,
                                 long expirationMinutes,
                                 Map<String, Object> customClaims) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(Duration.ofMinutes(expirationMinutes)));

        try {
            PrivateKey privateKey = KeyProviderUtil.getPrivateKey(this.privateKeyFromFile);

            return Jwts.builder()
                    .subject(String.valueOf(sub))
                    .claims(customClaims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(privateKey)
                    .compact();
        } catch (NoSuchFileException ex) {
            throw new AppException(ErrorMessage.PRIVATE_KEY_FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new AppException(ErrorMessage.JWT_SIGNING_ERROR, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).isBefore(Instant.now());
        } catch (AppException e) {
            if (e.getMessage().equals(ErrorMessage.TOKEN_EXPIRED)) {
                return true;
            }
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public UsernamePasswordAuthenticationToken getAuthentication(String jwt) {
        var userId = this.extractId(jwt);
        var role = this.extractRole(jwt);
        var userEmail = this.extractEmail(jwt);
        var authorities = this.extractAuthorities(jwt);
        var authenticatedUser = User.builder()
                .id(UUID.fromString(userId))
                .email(userEmail)
                .role(role)
                .build();

        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authorities
        );
    }

    private Claims extractAllClaims(String token) {
        try {
            PublicKey publicKey = KeyProviderUtil.getPublicKey(this.publicKeyFromFile);
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (NoSuchFileException e) {
            throw new AppException(ErrorMessage.PUBLIC_KEY_FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorMessage.TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            throw new AppException(ErrorMessage.INVALID_JWT_SIGNATURE, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorMessage.INVALID_JWT_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }
}