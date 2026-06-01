package ru.impuls.helpdeskservice.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;

import java.util.UUID;

public record UserResponse (
        UUID id,
        String name,
        String username,
        String email,
        RoleDto role,
        @JsonProperty("isDisabled") boolean disabled
) {}
