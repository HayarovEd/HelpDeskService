package ru.impuls.helpdeskservice.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;
import ru.impuls.helpdeskservice.auth.entity.Role;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    RoleDto toRoleDto(Role role);
}
