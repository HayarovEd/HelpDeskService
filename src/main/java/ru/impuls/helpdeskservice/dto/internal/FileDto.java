package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileDto(
        @JsonProperty("image_url")
        String storedFileName,
        @JsonProperty("file_size")
        Long fileSize,
        @JsonProperty("mime_type")
        String mimeType
) {}
