package ru.impuls.helpdeskservice.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import ru.impuls.helpdeskservice.validation.annotation.ValidEmail;

public record LoginRequest (@ValidEmail String email, @NotBlank String password) {}
