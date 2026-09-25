package ru.impuls.helpdeskservice.config;

import lombok.RequiredArgsConstructor;
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
                            .requestMatchers("/api/ws/**").permitAll()  // Добавьте это
                            .requestMatchers("/websocket/**").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
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

        // Остальные — с ограничениями
        CorsConfiguration securedConfig = new CorsConfiguration();
        //securedConfig.setAllowedOrigins(securityProperties.getCorsAllowedOrigins()); // для прод
        securedConfig.setAllowedOriginPatterns(List.of("*")); // для тестирования вместо setAllowedOrigins
        securedConfig.setAllowedMethods(List.of("*"));
        securedConfig.setAllowCredentials(true);
        securedConfig.setAllowedHeaders(List.of("*"));
        securedConfig.setExposedHeaders(List.of("Content-Disposition", "Authorization"));
        securedConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", securedConfig);

        return source;
    }
}
