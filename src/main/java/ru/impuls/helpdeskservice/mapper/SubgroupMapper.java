package ru.impuls.helpdeskservice.mapper;

import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;
import ru.impuls.helpdeskservice.dto.response.SubgroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.List;

public interface SubgroupMapper {

    SubgroupDto toDto(Subgroup subgroup);

    SubgroupEventDto toEventDto(Subgroup subgroup);

    Subgroup toEntity(SubgroupDto subgroupDto);

    void toEntity(Subgroup subgroup, SubgroupEditDto subgroupDto);

    Subgroup toEntity(SubgroupEditDto subgroupDto, Group group);

    List<SubgroupDto> toDtoList(List<Subgroup> subgroups);

    List<Subgroup> toEntityList(List<SubgroupDto> subgroupDtos);
}
