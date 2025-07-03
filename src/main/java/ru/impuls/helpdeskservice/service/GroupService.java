package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    List<GroupDto> getAll();

    GroupDto getById(UUID id);

    GroupDto create(GroupEditDto groupDto);

    GroupDto update(UUID id, GroupEditDto groupDto);

    GroupDto delete(UUID id);
}
