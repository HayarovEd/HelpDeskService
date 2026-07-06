package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record MessageDto (
        UUID id,
        String text,
        @JsonProperty("is_read")
        Boolean isRead,
        @JsonProperty("date")
        Instant createdDate,
        @JsonProperty("isSupport")
        boolean support
      //  @JsonProperty("files") List<FileDto> files
        ) {}
