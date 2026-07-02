package ru.impuls.helpdeskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import ru.impuls.helpdeskservice.validation.annotation.ValidEmail;

public record ChatCreateRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Login is required") String login,
        @ValidEmail String email,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Message is required") String message
) {}
