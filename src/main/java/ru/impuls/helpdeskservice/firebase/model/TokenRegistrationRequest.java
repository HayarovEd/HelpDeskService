package ru.impuls.helpdeskservice.firebase.model;

import jakarta.validation.constraints.NotBlank;

public record TokenRegistrationRequest(
        @NotBlank(message = "Device token is required")
        String token
) {}
