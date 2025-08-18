package ru.impuls.helpdeskservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConnectionDto (
        UUID id,
        @JsonProperty("start") Instant startDate,
        @JsonProperty("end") Instant endDate,
        UserDto user,
        List<MessageDto> messages
) {}
