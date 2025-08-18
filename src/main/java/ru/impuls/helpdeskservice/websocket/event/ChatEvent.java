package ru.impuls.helpdeskservice.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.With;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.dto.internal.SubgroupDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatEvent (
        UUID id,
        String name,
        String email,
        String phone,
        @JsonProperty("isOpen") boolean open,
        @JsonProperty("isWork") boolean work,
        Instant createdDate,
        SubgroupDto subgroup,
        List<ConnectionDto> connections
) {}