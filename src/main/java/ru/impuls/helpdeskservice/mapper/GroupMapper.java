package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.dto.response.GroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GroupMapper {

    GroupDto toDto(final Group group);

    GroupEventDto toEventDto(final Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subgroups", ignore = true)
    Group toEntity(final GroupEditDto groupDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subgroups", ignore = true)
    void toEntity(@MappingTarget final Group group, final GroupEditDto groupDto);

    default List<SubgroupDto> mapSubgroups(List<Subgroup> subgroups) {
        return subgroups == null ? Collections.emptyList() :
                subgroups.stream()
                        .map(this::mapSubgroup)
                        .toList();
    }

    SubgroupDto mapSubgroup(Subgroup subgroup);
}
