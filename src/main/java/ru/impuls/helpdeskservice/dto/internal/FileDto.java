package ru.impuls.helpdeskservice.dto.internal;

public record FileDto(
        String storedFileName,
        Long fileSize,
        String mimeType
) {}
