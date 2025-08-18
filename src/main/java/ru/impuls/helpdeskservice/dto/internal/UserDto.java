package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String email,
        RoleDto role,
        @JsonProperty("isDisabled") boolean disabled
) {}
