package ru.impuls.helpdeskservice.dto.internal;


import org.springframework.core.io.Resource;

public record FileResource(
        Resource resource,
        String contentType
) {
}
