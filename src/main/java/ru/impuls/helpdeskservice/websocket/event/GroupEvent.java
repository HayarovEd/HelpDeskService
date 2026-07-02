package ru.impuls.helpdeskservice.websocket.event;



import java.util.UUID;

public record GroupEvent (
        UUID id,
        String name,
        Long operId
     //   List<SubgroupDto> subgroups
) {}
