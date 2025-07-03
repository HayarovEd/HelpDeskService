package ru.impuls.helpdeskservice.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.mapper.GroupMapper;
import ru.impuls.helpdeskservice.mapper.SubgroupMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupMapperImpl implements GroupMapper {

    private final SubgroupMapper subgroupMapper;

    @Override
    public GroupDto toDto(Group group) {
        if (group == null) return null;

        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .subgroups(subgroupMapper.toDtoList(group.getSubgroups()))
                .build();
    }

    @Override
    public Group toEntity(GroupEditDto groupDto) {
        return Group.builder()
                .name(groupDto.getName())
                .build();
    }

    @Override
    public void toEntity(Group group, GroupEditDto groupDto) {
        group.setName(groupDto.getName());
    }

    @Override
    public List<GroupDto> toDtoList(List<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }

        return groups.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> toEntityList(List<GroupDto> groupDtos) {
        return List.of();
    }
}
