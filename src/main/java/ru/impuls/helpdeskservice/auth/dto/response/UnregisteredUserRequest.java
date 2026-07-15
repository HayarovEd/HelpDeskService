package ru.impuls.helpdeskservice.auth.dto.response;

import jakarta.validation.constraints.NotBlank;

public record UnregisteredUserRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotBlank  String phone
) {
}
