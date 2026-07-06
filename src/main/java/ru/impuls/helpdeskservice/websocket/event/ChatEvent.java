package ru.impuls.helpdeskservice.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.dto.internal.GroupForSubDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatEvent (
        UUID id,
        String name,
        String email,
        String phone,
        String login,
        @JsonProperty("is_open")
        boolean open,
        @JsonProperty("is_work")
        boolean work,
        @JsonProperty("is_active")
        boolean active,
        @JsonProperty("unread_messages_count")
        long unreadMessagesCount,
        @JsonProperty("created_date")
        Instant createdDate,
        @JsonProperty("group")
        GroupForSubDto group,
        List<ConnectionDto> connections
) {}