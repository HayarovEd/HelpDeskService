package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.request.GroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.GroupEditRequest;
import ru.impuls.helpdeskservice.dto.response.GroupResponse;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.GroupMapper;
import ru.impuls.helpdeskservice.repository.GroupRepo;
import ru.impuls.helpdeskservice.service.GroupService;
import ru.impuls.helpdeskservice.service.LogService;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.CREATED;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepo groupRepo;
    private final GroupMapper groupMapper;
    private final WebSocketHandler webSocketHandler;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getAll() {
        return groupRepo.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .filter(Objects::nonNull)
            //    .peek(group -> group.getSubgroups().sort(Comparator.comparing(Subgroup::getName)))
                .map(groupMapper::toDto)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public GroupResponse getById(UUID id) {
        return groupRepo.findById(id)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public GroupResponse getByName(String name) {
        return groupRepo.findByName(name)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public GroupResponse create(GroupCreateRequest groupRequest) {
        if (groupRepo.existsByName(groupRequest.name())) {
            throw new AppException(ErrorMessage.GROUP_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        var group = groupMapper.toEntity(groupRequest);

        groupRepo.save(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEvent(group), CREATED);

        return groupMapper.toDto(group);
    }

    @Override
    public GroupResponse update(UUID id, GroupEditRequest groupRequest) {
        var group = groupRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (!group.getName().equals(groupRequest.name())
                && groupRepo.existsByName(groupRequest.name())) {
            throw new AppException(ErrorMessage.GROUP_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        groupMapper.toEntity(group, groupRequest);
        groupRepo.save(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEvent(group), WebsocketEventType.UPDATED);

        return groupMapper.toDto(group);
    }

    @Override
    public GroupResponse delete(UUID id) {
        var group = groupRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        groupRepo.delete(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEvent(group), WebsocketEventType.DELETED);
        logService.create(
                LogActionType.DELETED,
                LogEntityType.GROUP,
                group.getName(),
                group.getId()
        );

        return groupMapper.toDto(group);
    }
}
