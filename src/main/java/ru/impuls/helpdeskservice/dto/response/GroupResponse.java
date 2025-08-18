package ru.impuls.helpdeskservice.dto.response;

import ru.impuls.helpdeskservice.dto.internal.SubgroupDto;

import java.util.List;
import java.util.UUID;

public record GroupResponse (
        UUID id,
        String name, 
        List<SubgroupDto> subgroups
) {}
