package ru.impuls.helpdeskservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        String method = request.getMethod();

        // 1. Пропускаем OPTIONS запросы (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Пропускаем WebSocket handshake запросы
        if (isWebSocketPath(path) || isWebSocketPath(request.getRequestURI())) {
            log.debug("JwtFilter: Пропускаем WebSocket handshake: {} {}", method, path);

            String token = request.getParameter("token");
            if (token != null) {
                request.setAttribute("ws_token", token);
            }

            filterChain.doFilter(request, response);
            return;
        }

        // 3. Проверяем, является ли эндпоинт публичным
        boolean isPublicEndpoint = false;
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            isPublicEndpoint = securityProperties.isPublicEndpoint(httpMethod, path)
                    || securityProperties.isPublicEndpoint(httpMethod, request.getRequestURI());
        } catch (IllegalArgumentException e) {
            // Неизвестный метод
        }

        final String authHeader = request.getHeader("Authorization");

        // 4. Если заголовка Authorization нет (или не Bearer)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublicEndpoint) {
                log.debug("JwtFilter: Пропускаем неавторизованный запрос к публичному endpoint: {}", path);
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + ErrorMessage.UNAUTHORIZED + "\"}");
            response.getWriter().flush();
            return;
        }

        // 5. Заголовок передан (включая публичные эндпоинты) — проверяем JWT и устанавливаем аутентификацию
        try {
            String jwt = authHeader.substring(7);

            var userId = jwtService.extractId(jwt);
            var jwtTokenVersion = jwtService.extractTokenVersion(jwt);
            var userTokenVersion = userService.getTokenVersionByUserId(UUID.fromString(userId));

            if (userTokenVersion != null
                    && !userTokenVersion.isEmpty()
                    && !userTokenVersion.equals(jwtTokenVersion)) {
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
        } catch (Exception e) {
            log.error("JwtFilter: Ошибка при обработке токена", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + ErrorMessage.UNAUTHORIZED + "\"}");
            response.getWriter().flush();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        return path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                isWebSocketPath(path) ||
                isWebSocketPath(request.getRequestURI()) ||
                path.startsWith("/api/auth") ||
                request.getRequestURI().contains("/api/auth") ||
                path.contains("unregistered");
    }

    private boolean isWebSocketPath(String path) {
        if (path == null) return false;
        return path.startsWith("/ws") ||
                path.startsWith("/api/ws") ||
                path.startsWith("/websocket") ||
                path.startsWith("/stomp") ||
                path.startsWith("/socket") ||
                path.contains("/ws/") ||
                path.endsWith("/ws");
    }
}
