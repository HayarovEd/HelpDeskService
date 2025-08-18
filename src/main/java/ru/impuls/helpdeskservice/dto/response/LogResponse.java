package ru.impuls.helpdeskservice.dto.response;

import ru.impuls.helpdeskservice.dto.internal.LogItemDto;

import java.time.Instant;
import java.util.List;

public record LogResponse (
        long count,
        Instant firstDate,
        List<LogItemDto> items
) {}