package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.response.LogResponse;

import java.time.Instant;
import java.util.UUID;

public interface LogService {
    LogResponse getAll(Instant to, int count);

    void create(LogActionType action,
                LogEntityType entityType,
                String entityName,
                UUID entityId
    );
}
