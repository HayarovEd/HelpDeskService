package ru.impuls.helpdeskservice.dto.internal;

public record BillingCookie(
        String yiiCsrfToken,
        String nameSession,
        BillingSession session
) {}
