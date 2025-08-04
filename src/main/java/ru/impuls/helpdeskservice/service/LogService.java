package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.response.LogDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LogService {
    List<LogDto> findAllWithFilter(Instant to, int count);
    void create(LogActionType action, LogEntityType entityType, String entityName, UUID entityId);
}
