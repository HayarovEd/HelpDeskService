package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;
import ru.impuls.helpdeskservice.dto.response.SubgroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SubgroupMapper {

    SubgroupDto toDto(Subgroup subgroup);

    @Mapping(source = "group.id", target = "group.id")
    @Mapping(source = "group.name", target = "group.name")
    SubgroupEventDto toEventDto(Subgroup subgroup);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    void toEntity(@MappingTarget Subgroup subgroup, SubgroupEditDto subgroupDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "subgroupDto.name")
    @Mapping(target = "group", source = "group")
    Subgroup toEntity(SubgroupEditDto subgroupDto, Group group);
}
