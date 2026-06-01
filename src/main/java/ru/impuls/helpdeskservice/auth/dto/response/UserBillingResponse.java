package ru.impuls.helpdeskservice.auth.dto.response;


public record UserBillingResponse(
        String name,
        String username,
        String email,
        String password
) {
}
