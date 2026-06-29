package ru.impuls.helpdeskservice.dto.response;

import java.util.UUID;

public record GroupResponse (
        UUID id,
        String name
       // List<SubgroupDto> subgroups
) {}
