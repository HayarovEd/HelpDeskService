package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.dto.response.GroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDto toDto(final Group group);

    GroupEventDto toEventDto(final Group group);

    Group toEntity(final GroupEditDto groupDto);

    void toEntity(@MappingTarget final Group group, final GroupEditDto groupDto);

    default List<SubgroupDto> mapSubgroups(List<Subgroup> subgroups) {
        return subgroups == null ? Collections.emptyList() :
                subgroups.stream()
                        .map(this::mapSubgroup)
                        .collect(Collectors.toList());
    }

    SubgroupDto mapSubgroup(Subgroup subgroup);
}
