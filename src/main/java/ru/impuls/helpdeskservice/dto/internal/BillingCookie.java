package ru.impuls.helpdeskservice.dto.internal;

import java.time.LocalDateTime;

public record BillingCookie(
        int uid,
        LocalDateTime timeLastLogin,
        String sessionToken,
        int durationSeconds
) {}
