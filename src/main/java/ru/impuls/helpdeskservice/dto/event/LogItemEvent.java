package ru.impuls.helpdeskservice.dto.event;

import ru.impuls.helpdeskservice.dto.internal.UserForLogItemDto;

import java.time.Instant;
import java.util.UUID;

public record LogItemEvent(
        UUID id,
        String text,
        Instant createdDate,
        UserForLogItemDto user
) {}
