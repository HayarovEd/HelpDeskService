package ru.impuls.helpdeskservice.dto.internal;

import java.util.UUID;

public record GroupForSubDto(
        UUID id,
        String name,
        Long operId
) {}

