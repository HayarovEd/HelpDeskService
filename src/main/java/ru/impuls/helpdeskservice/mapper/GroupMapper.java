package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.dto.response.GroupEventDto;
import ru.impuls.helpdeskservice.entity.Group;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDto toDto(final Group group);

    GroupEventDto toEventDto(final Group group);

    Group toEntity(final GroupEditDto groupDto);

    void toEntity(@MappingTarget final Group group, final GroupEditDto groupDto);

    List<GroupDto> toDtoList(final List<Group> groups);
}
