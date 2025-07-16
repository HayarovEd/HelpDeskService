package ru.impuls.helpdeskservice.mapper.impl;

import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.dto.GroupForSubDto;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;
import ru.impuls.helpdeskservice.dto.response.SubgroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;
import ru.impuls.helpdeskservice.mapper.SubgroupMapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SubgroupMapperImpl implements SubgroupMapper {

    @Override
    public SubgroupDto toDto(Subgroup subgroup) {
        if (subgroup == null) return null;

        return SubgroupDto.builder()
                .id(subgroup.getId())
                .name(subgroup.getName())
                .build();
    }

    @Override
    public SubgroupEventDto toEventDto(Subgroup subgroup) {
        if (subgroup == null) return null;

        var group = GroupForSubDto.builder()
                .id(subgroup.getGroup().getId())
                .name(subgroup.getGroup().getName())
                .build();

        return SubgroupEventDto.builder()
                .id(subgroup.getId())
                .name(subgroup.getName())
                .group(group)
                .build();
    }

    @Override
    public Subgroup toEntity(SubgroupDto subgroupDto) {
        return null;
    }

    @Override
    public void toEntity(Subgroup subgroup, SubgroupEditDto subgroupDto) {
        subgroup.setName(subgroupDto.getName());
    }

    @Override
    public Subgroup toEntity(SubgroupEditDto subgroupDto, Group group) {
        if(subgroupDto == null) return null;

        return Subgroup.builder()
                .name(subgroupDto.getName())
                .group(group)
                .build();
    }

    @Override
    public List<SubgroupDto> toDtoList(List<Subgroup> subgroups) {
        if (subgroups == null || subgroups.isEmpty()) {
            return Collections.emptyList();
        }

        return subgroups.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subgroup> toEntityList(List<SubgroupDto> subgroupDtos) {
        return List.of();
    }
}
