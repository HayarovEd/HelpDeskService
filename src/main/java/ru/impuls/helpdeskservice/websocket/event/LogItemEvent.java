package ru.impuls.helpdeskservice.websocket.event;

import ru.impuls.helpdeskservice.dto.internal.UserDto;

import java.time.Instant;
import java.util.UUID;

public record LogItemEvent(
        UUID id,
        String text,
        Instant createdDate,
        UserDto user
) {}
