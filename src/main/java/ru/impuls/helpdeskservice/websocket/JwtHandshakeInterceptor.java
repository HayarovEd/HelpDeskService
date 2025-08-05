package ru.impuls.helpdeskservice.websocket;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ru.impuls.helpdeskservice.auth.service.JwtService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request,
                                   @NotNull ServerHttpResponse response,
                                   @NotNull org.springframework.web.socket.WebSocketHandler wsHandler,
                                   @NotNull Map<String, Object> attributes) {

        // Получаем токен из параметров запроса или заголовков
        String token = extractToken(request);

        if (token == null || jwtService.isTokenExpired(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("expiration", jwtService.extractExpiration(token));
        attributes.put("role", jwtService.extractRole(token));

        return true;
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Проверяем параметр запроса
        if (request instanceof ServletServerHttpRequest) {
            String token = ((ServletServerHttpRequest) request)
                    .getServletRequest()
                    .getParameter("token");
            if (token != null) return token;
        }

        // 2. Проверяем заголовки
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.getFirst();
            if (header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        }

        return null;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request,
                               @NotNull ServerHttpResponse response,
                               @NotNull org.springframework.web.socket.WebSocketHandler wsHandler,
                               Exception exception) {
        // Опциональная пост-обработка
    }

}
