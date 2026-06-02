package ru.impuls.helpdeskservice.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;
import ru.impuls.helpdeskservice.auth.mapper.RoleMapper;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.service.RoleService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepo.findAll().stream()
                .map(roleMapper::toRoleDto)
                .collect(Collectors.toList());
    }
}
