package ru.impuls.helpdeskservice.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;
import ru.impuls.helpdeskservice.validation.annotation.ValidEmail;

public record UserCreateRequest (
        @NotBlank(message = "Name is required") String name,
        @ValidEmail String email,
        @NotBlank String password,
        @JsonProperty("isDisabled") boolean disabled,
        RoleDto role
) {}
