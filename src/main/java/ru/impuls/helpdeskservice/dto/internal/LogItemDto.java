package ru.impuls.helpdeskservice.dto.internal;

import java.time.Instant;
import java.util.UUID;

public record LogItemDto (
        UUID id,
        String text,
        Instant createdDate,
        UserForLogItemDto user
) {}
