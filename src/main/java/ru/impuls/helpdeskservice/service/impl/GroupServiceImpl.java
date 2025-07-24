package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.GroupMapper;
import ru.impuls.helpdeskservice.repository.GroupRepo;
import ru.impuls.helpdeskservice.service.GroupService;
import ru.impuls.helpdeskservice.websocket.EventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepo groupRepo;
    private final GroupMapper groupMapper;
    private final WebSocketHandler webSocketHandler;

    @Override
    @Transactional(readOnly = true)
    public List<GroupDto> getAll() {
        return groupRepo.findAll().stream()
                .filter(Objects::nonNull)
                .map(groupMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDto getById(UUID id) {
        return groupRepo.findById(id)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.GROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    public GroupDto create(GroupEditDto groupDto) {
        if (groupRepo.existsByName(groupDto.getName())) {
            throw new AppException(
                    ErrorMessage.GROUP_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        var group = groupMapper.toEntity(groupDto);
        group = groupRepo.save(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEventDto(group), EventType.CREATED);

        return groupMapper.toDto(group);
    }

    @Override
    public GroupDto update(UUID id, GroupEditDto groupDto) {
        var group = groupRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.GROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        if (!group.getName().equals(groupDto.getName())
                && groupRepo.existsByName(groupDto.getName())) {
            throw new AppException(
                    ErrorMessage.GROUP_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        groupMapper.toEntity(group, groupDto);
        group = groupRepo.save(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEventDto(group), EventType.UPDATED);

        return groupMapper.toDto(group);
    }

    @Override
    public GroupDto delete(UUID id) {
        var group = groupRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.GROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        groupRepo.delete(group);
        webSocketHandler.notifyChange("/groups", groupMapper.toEventDto(group), EventType.DELETED);

        return groupMapper.toDto(group);
    }
}
