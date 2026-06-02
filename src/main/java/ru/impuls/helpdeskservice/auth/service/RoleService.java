package ru.impuls.helpdeskservice.auth.service;

import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;

import java.util.List;

public interface RoleService {
    List<RoleDto> getAllRoles();
}
