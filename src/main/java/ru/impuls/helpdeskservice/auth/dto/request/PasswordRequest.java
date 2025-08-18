package ru.impuls.helpdeskservice.auth.dto.request;

import ru.impuls.helpdeskservice.validation.annotation.ValidPassword;

public record PasswordRequest(@ValidPassword String password) {}
