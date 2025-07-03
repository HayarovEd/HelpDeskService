package ru.impuls.helpdeskservice.mapper;

import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.entity.Group;

import java.util.List;

public interface GroupMapper {

    GroupDto toDto(Group group);

    Group toEntity(GroupEditDto groupDto);

    void toEntity(Group group, GroupEditDto groupDto);

    List<GroupDto> toDtoList(List<Group> groups);

    List<Group> toEntityList(List<GroupDto> groupDtos);
}
