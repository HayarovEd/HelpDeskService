package ru.impuls.helpdeskservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.impuls.helpdeskservice.auth.service.JwtService;
import ru.impuls.helpdeskservice.auth.service.UserService;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Пропускаем OPTIONS запросы (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Пропускаем WebSocket handshake запросы
        if (isWebSocketPath(path)) {
            System.out.println("JwtFilter: Пропускаем WebSocket handshake: " + method + " " + path);

            // Опционально: извлекаем токен из параметров и сохраняем для WebSocket сессии
            String token = request.getParameter("token");
            if (token != null) {
                request.setAttribute("ws_token", token);
                System.out.println("JwtFilter: Найден токен в параметрах: " + token.substring(0, Math.min(20, token.length())) + "...");
            }

            filterChain.doFilter(request, response);
            return;
        }

        // 3. Проверяем публичные endpoints из конфигурации
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            if (securityProperties.isPublicEndpoint(httpMethod, path)) {
                System.out.println("JwtFilter: Пропускаем публичный endpoint: " + path);
                filterChain.doFilter(request, response);
                return;
            }
        } catch (IllegalArgumentException e) {
            // Неизвестный метод, продолжаем проверку
        }

        // 4. Для остальных запросов проверяем JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        try {
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                throw new AppException(
                        ErrorMessage.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED
                );
            }

            jwt = authHeader.substring(7);

            var userId = jwtService.extractId(jwt);
            var jwtTokenVersion = jwtService.extractTokenVersion(jwt);
            var userTokenVersion = userService.getTokenVersionByUserId(UUID.fromString(userId));

            if(userTokenVersion != null
                    && !userTokenVersion.isEmpty()
                    && !userTokenVersion.equals(jwtTokenVersion)){
                throw new AppException(
                        "invalid_token",
                        HttpStatus.UNAUTHORIZED
                );
            }

            if (!jwtService.isTokenExpired(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authToken = jwtService.getAuthentication(jwt);

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (AppException e) {
            response.setStatus(e.getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/ws") ||
                path.startsWith("/api/ws") ||
                path.startsWith("/websocket") ||
                path.startsWith("/api/auth");
    }


    private boolean isWebSocketPath(String path) {
        return path.startsWith("/ws") ||
                path.startsWith("/api/ws") ||
                path.startsWith("/websocket") ||
                path.startsWith("/stomp") ||
                path.startsWith("/socket") ||
                path.contains("/ws/") ||
                path.endsWith("/ws");
    }
}
