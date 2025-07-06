package ru.impuls.helpdeskservice.auth.dto;


import ru.impuls.helpdeskservice.auth.entity.Role;

import java.util.HashMap;
import java.util.Map;

public record TokenClaimDto(Role role, String tokenType, String email) {

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        if (email != null) {
            map.put("email", email);
        }
        if (role != null) {
            map.put("role", role.getName());
        }
        map.put("tokenType", tokenType);
        return map;
    }
}
