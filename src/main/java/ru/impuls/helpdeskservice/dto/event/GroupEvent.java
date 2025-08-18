package ru.impuls.helpdeskservice.dto.event;

import ru.impuls.helpdeskservice.dto.internal.SubgroupDto;

import java.util.List;
import java.util.UUID;

public record GroupEvent (
        UUID id,
        String name,
        List<SubgroupDto> subgroups
) {}
