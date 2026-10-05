package ru.impuls.helpdeskservice.dto.internal;

import java.time.Instant;

public record BillingSession(
        String hash,
        int userId,
        Instant timeLastLogin,
        String key,
        int ttl
) {}
