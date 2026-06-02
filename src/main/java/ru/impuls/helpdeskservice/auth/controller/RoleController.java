package ru.impuls.helpdeskservice.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.impuls.helpdeskservice.auth.dto.internal.RoleDto;
import ru.impuls.helpdeskservice.auth.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles();
    }
}
