package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileDto(
        @JsonProperty("image_url")
        String storedFileName,
        @JsonProperty("file_size")
        Long fileSize,
        @JsonProperty("original_name")
        String originalFileName,
        @JsonProperty("mime_type")
        String mimeType
) {}
