package ru.impuls.helpdeskservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<String> corsAllowedOrigins = new ArrayList<>();
    private List<PublicEndpoint> publicEndpoints = new ArrayList<>();

    @Value("${application.api.base-path:/api}")
    private String apiBasePath;

    public boolean isPublicEndpoint(HttpMethod method, String uri) {
        return publicEndpoints.stream()
                .anyMatch(endpoint ->
                        matchesMethod(endpoint.getMethod(), method) &&
                                pathMatcher.match(apiBasePath + endpoint.getPath(), uri)
                );
    }

    private boolean matchesMethod(String configMethod, HttpMethod requestMethod) {
        return configMethod.equalsIgnoreCase(requestMethod.name());
    }

    @Getter
    @Setter
    public static class PublicEndpoint {
        private String method;
        private String path;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PublicEndpoint that = (PublicEndpoint) o;
            return method.equalsIgnoreCase(that.method) &&
                    path.equals(that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method.toLowerCase(), path);
        }
    }
}
