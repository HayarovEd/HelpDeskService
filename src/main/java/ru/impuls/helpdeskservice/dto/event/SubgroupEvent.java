package ru.impuls.helpdeskservice.dto.event;

import ru.impuls.helpdeskservice.dto.internal.GroupForSubDto;

import java.util.UUID;

public record SubgroupEvent (
        UUID id,
        String name,
        GroupForSubDto group
) {}
