package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.dto.response.SubgroupResponse;
import ru.impuls.helpdeskservice.dto.request.SubgroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditRequest;

import java.util.UUID;

public interface SubgroupService {

    SubgroupResponse create(UUID groupId, SubgroupCreateRequest subgroupRequest);

    SubgroupResponse update(UUID id, SubgroupEditRequest subgroupRequest);

    SubgroupResponse delete(UUID id);
}
