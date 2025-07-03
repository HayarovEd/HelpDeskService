package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;

import java.util.UUID;

public interface SubgroupService {

    SubgroupDto create(UUID groupId, SubgroupEditDto subgroupDto);

    SubgroupDto update(UUID id, SubgroupEditDto subgroupDto);

    SubgroupDto delete(UUID id);
}
