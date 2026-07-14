package ru.impuls.helpdeskservice.auth.dto.response;


import jakarta.validation.constraints.NotBlank;

public record UserBillingRequest(
        String name,
        @NotBlank  String username,
        String email,
        @NotBlank  String password,
        @NotBlank  String token
) {
}
