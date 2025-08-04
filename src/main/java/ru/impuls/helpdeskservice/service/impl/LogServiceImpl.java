package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.response.LogDto;
import ru.impuls.helpdeskservice.entity.Log;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.LogMapper;
import ru.impuls.helpdeskservice.repository.LogRepo;
import ru.impuls.helpdeskservice.service.LogService;
import ru.impuls.helpdeskservice.util.AuthUtil;
import ru.impuls.helpdeskservice.websocket.EventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    public List<LogDto> findAllWithFilter(Instant to, int count) {

        if (count < 0 || count > 1000) {
            throw new AppException(
                    "Count must be between 0 and 1000",
                    HttpStatus.BAD_REQUEST
            );
        }

        Pageable pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());

        if (to == null) {
            return logRepo.findAll(pageable)
                    .stream()
                    .map(logMapper::toDto)
                    .toList();
        } else {
            return logRepo.findAllByCreatedDateBefore(to, pageable)
                    .stream()
                    .map(logMapper::toDto)
                    .toList();
        }
    }

    @Override
    public void create(LogActionType action,
                       LogEntityType entityType,
                       String entityName,
                       UUID entityId) {
        var authenticatedUser = AuthUtil.getAuthenticatedUser();
        var user = userRepo.findById(authenticatedUser.getId())
                .orElseThrow(() -> new AppException(
                        ErrorMessage.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        var message = formatLogMessage(action, entityType, entityName, entityId);
        var log = logMapper.toEntity(message, user);

        log = logRepo.save(log);
        webSocketHandler.notifyChange("/logs", logMapper.toDto(log), EventType.CREATED);
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
