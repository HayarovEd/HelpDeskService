package ru.impuls.helpdeskservice.dto.internal;

import java.util.UUID;

public record GroupForSubEventDto(
        UUID id,
        String name
) {}

