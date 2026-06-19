package ru.impuls.helpdeskservice.service;

import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.dto.request.GroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.GroupEditRequest;
import ru.impuls.helpdeskservice.dto.response.GroupResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    List<GroupResponse> getAll();

    GroupResponse getById(UUID id);

    @Transactional(readOnly = true)
    GroupResponse getByName(String name);

    GroupResponse create(GroupCreateRequest groupRequest);

    GroupResponse update(UUID id, GroupEditRequest groupRequest);

    GroupResponse delete(UUID id);
}
