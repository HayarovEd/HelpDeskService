package ru.impuls.helpdeskservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.service.JwtService;
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

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

        if (securityProperties.isPublicEndpoint(method, request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

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

            if (!jwtService.isAccessToken(jwt)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorMessage.ONLY_ACCESS_TOKENS_ALLOWED
                );
                return;
            }

            var role = jwtService.extractRole(jwt);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtService.isTokenExpired(jwt)) {
                    var userId = jwtService.extractId(jwt);
                    var authorities = jwtService.extractAuthorities(jwt);
                    var userEmail = "fhfshhs";//jwtService.extractEmail(jwt); //TODO
                    var authenticatedUser = User.builder()
                            .id(UUID.fromString(userId))
                            .email(userEmail)
                            .role(role)
                            .build();
                    var authToken = new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (AppException e) {
            response.setStatus(e.getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return securityProperties.getPublicEndpoints().stream()
                .anyMatch(endpoint ->
                        endpoint.getMethod().equalsIgnoreCase(method) &&
                                uri.matches(endpoint.getPath().replaceAll("\\*", ".*"))
                );
    }

}
