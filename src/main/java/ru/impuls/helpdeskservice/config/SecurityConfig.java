package ru.impuls.helpdeskservice.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    for (SecurityProperties.PublicEndpoint endpoint : securityProperties.getPublicEndpoints()) {
                        HttpMethod method = HttpMethod.valueOf(endpoint.getMethod().toUpperCase());
                        String path = endpoint.getPath();
                        auth.requestMatchers(method, path).permitAll();
                    }

                    auth.requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/api/ws/**").permitAll()
                            .requestMatchers("/websocket/**").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/unregistered*").permitAll()
                            .requestMatchers("/unregistered/**").permitAll()
                            .requestMatchers(
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/v3/api-docs",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/bc/ws/**",
                                    "/bc/api/ws/**",
                                    "/bc/websocket/**",
                                    "/bc/api/auth/**",
                                    "/bc/unregistered*",
                                    "/bc/unregistered/**",
                                    "/bc/swagger-ui.html",
                                    "/bc/swagger-ui/**",
                                    "/bc/v3/api-docs/**",
                                    "/bc/v3/api-docs",
                                    "/bc/swagger-resources/**",
                                    "/bc/webjars/**"
                            ).permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Конфигурация для WebSocket
        CorsConfiguration wsConfig = new CorsConfiguration();
        wsConfig.addAllowedOriginPattern("*");
        wsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        wsConfig.setAllowedHeaders(List.of("*"));
        wsConfig.setAllowCredentials(true);
        wsConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/ws/**", wsConfig);

        // Конфигурация для всех эндпоинтов (REST API и веб-виджетов поддержки)
        CorsConfiguration securedConfig = createSecuredConfig();
        source.registerCorsConfiguration("/**", securedConfig);

        return source;
    }

    private @NonNull CorsConfiguration createSecuredConfig() {
        CorsConfiguration securedConfig = new CorsConfiguration();
        securedConfig.addAllowedOriginPattern("*");
        List<String> allowedOrigins = securityProperties.getCorsAllowedOrigins();
        if (allowedOrigins != null) {
            for (String origin : allowedOrigins) {
                securedConfig.addAllowedOriginPattern(origin);
            }
        }
        securedConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        securedConfig.setAllowCredentials(true);
        securedConfig.setAllowedHeaders(List.of("*"));
        securedConfig.setExposedHeaders(List.of("Content-Disposition", "Authorization", "Set-Cookie"));
        securedConfig.setMaxAge(3600L);
        return securedConfig;
    }
}
