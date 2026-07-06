package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record GroupForSubDto(
        UUID id,
        String name,
        @JsonProperty("oper_id")
        Long operId
) {}

