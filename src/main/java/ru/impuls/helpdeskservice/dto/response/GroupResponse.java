package ru.impuls.helpdeskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record GroupResponse (
        UUID id,
        String name,
        @JsonProperty("oper_id")
        Long operId
       // List<SubgroupDto> subgroups
) {}
