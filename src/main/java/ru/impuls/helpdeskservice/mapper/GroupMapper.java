package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.internal.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.GroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.GroupEditRequest;
import ru.impuls.helpdeskservice.dto.response.GroupResponse;
import ru.impuls.helpdeskservice.dto.response.GroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GroupMapper {

    default GroupResponse toDto(Group group) {
        if (group == null) {
            return null;
        }
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getSubgroups() == null ? Collections.emptyList() : mapSubgroups(group.getSubgroups())
        );
    }

    GroupEventDto toEventDto(Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subgroups", ignore = true)
    Group toEntity(GroupCreateRequest groupRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subgroups", ignore = true)
    void toEntity(@MappingTarget final Group group, final GroupEditRequest groupDto);

    default List<SubgroupDto> mapSubgroups(List<Subgroup> subgroups) {
        return subgroups == null ? Collections.emptyList() :
                subgroups.stream()
                        .map(this::mapSubgroup)
                        .toList();
    }

    SubgroupDto mapSubgroup(Subgroup subgroup);
}
