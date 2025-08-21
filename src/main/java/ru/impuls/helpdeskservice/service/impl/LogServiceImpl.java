package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.internal.LogItemDto;
import ru.impuls.helpdeskservice.dto.response.LogResponse;
import ru.impuls.helpdeskservice.entity.Log;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.LogMapper;
import ru.impuls.helpdeskservice.repository.LogRepo;
import ru.impuls.helpdeskservice.service.LogService;
import ru.impuls.helpdeskservice.util.AuthUtil;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.CREATED;

@Service
@Transactional
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepo logRepo;
    private final LogMapper logMapper;
    private final UserRepo userRepo;
    private final WebSocketHandler webSocketHandler;

    @Override
    @Transactional(readOnly = true)
    public LogResponse getAll(Instant to, int count) {
        if (count < 0 || count > 1000) {
            throw new AppException("Count must be between 0 and 1000", HttpStatus.BAD_REQUEST);
        }

        Instant firstDate = logRepo.findFirstByOrderByCreatedDateAsc()
                .map(Log::getCreatedDate)
                .orElse(null);

        Pageable pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());
        List<LogItemDto> items = to == null
                ? logRepo.findAll(pageable)
                    .map(logMapper::toDto)
                    .toList()
                : logRepo.findAllByCreatedDateBefore(to, pageable)
                    .map(logMapper::toDto)
                    .stream().toList();

        return new LogResponse(logRepo.count(), firstDate, items);
    }

    @Override
    public void create(LogActionType action,
                       LogEntityType entityType,
                       String entityName,
                       UUID entityId) {
        var authenticatedUser = AuthUtil.getAuthenticatedUser();
        var user = userRepo.findById(authenticatedUser.getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var message = formatLogMessage(action, entityType, entityName, entityId);
        var log = logMapper.toEntity(message, user);

        logRepo.save(log);
        webSocketHandler.notifyChange("/logs", logMapper.toEvent(log), CREATED);
    }

    private String formatLogMessage(LogActionType action,
                                    LogEntityType entityType,
                                    String entityName,
                                    UUID entityId) {
        return String.format("%s %s с именем '%s' и id '%s'",
                action.getDescription(),
                entityType.getDescription(),
                entityName,
                entityId);
    }
}
