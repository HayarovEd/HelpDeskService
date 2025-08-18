package ru.impuls.helpdeskservice.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;

import java.util.UUID;

public record UserEvent (
        UUID id,
        String name,
        String email,
        RoleDto role,
        @JsonProperty("isDisabled") boolean disabled
) {}