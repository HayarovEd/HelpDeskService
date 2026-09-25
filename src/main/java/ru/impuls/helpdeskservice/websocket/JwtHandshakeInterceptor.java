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

        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        System.out.println("WS handshake: path=" + path + ", query=" + query);

        if (path.matches(".*/ws/chats/[a-fA-F0-9-]{36}/connections")
                || path.matches(".*/ws/chats/[a-fA-F0-9-]{36}/messages")) {
            return true;
        }

        String token = extractToken(request);
        System.out.println("WS handshake: token=" + (token != null ? "present" : "null"));

        if (token == null || jwtService.isTokenExpired(token)) {
            System.out.println("WS handshake: token invalid or expired, rejecting");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("expiration", jwtService.extractExpiration(token));
        attributes.put("role", jwtService.extractRole(token).getName());
        attributes.put("userId", jwtService.extractId(token));

        System.out.println("WS handshake: accepted");
        return true;
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Извлекаем токен из query string
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
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
    }

}
