package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import ru.impuls.helpdeskservice.validation.annotation.ValidEmail;

public record ChatCreateRequest(
        String name,
        String login,
        @ValidEmail String email,
        String phone,
        @NotBlank(message = "Message is required") String message
) {}
