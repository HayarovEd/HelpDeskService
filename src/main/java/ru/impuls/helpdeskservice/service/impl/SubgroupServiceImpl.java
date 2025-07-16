package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.entity.Subgroup;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.SubgroupMapper;
import ru.impuls.helpdeskservice.repository.GroupRepo;
import ru.impuls.helpdeskservice.repository.SubgroupRepo;
import ru.impuls.helpdeskservice.service.SubgroupService;
import ru.impuls.helpdeskservice.websocket.EventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.UUID;

import static ru.impuls.helpdeskservice.websocket.EventType.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SubgroupServiceImpl implements SubgroupService {

    private final SubgroupRepo subgroupRepo;
    private final SubgroupMapper subgroupMapper;
    private final GroupRepo groupRepo;
    private final WebSocketHandler webSocketHandler;

    @Override
    public SubgroupDto create(UUID groupId, SubgroupEditDto subgroupDto) {
        var group = groupRepo.findById(groupId)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.GROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        if (subgroupRepo.existsByNameAndGroupId(subgroupDto.getName(), groupId)) {
            throw new AppException(
                    ErrorMessage.SUBGROUP_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        var subgroup = subgroupMapper.toEntity(subgroupDto, group);
        subgroup = subgroupRepo.save(subgroup);
        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.CREATED);

        return subgroupMapper.toDto(subgroup);

    }

    @Override
    public SubgroupDto update(UUID id, SubgroupEditDto subgroupDto) {
        var subgroup = subgroupRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.SUBGROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
        var groupId = subgroup.getGroup().getId();

        if (!subgroup.getName().equals(subgroupDto.getName())
                && subgroupRepo.existsByNameAndGroupId(subgroupDto.getName(), groupId)) {
            throw new AppException(
                    ErrorMessage.SUBGROUP_ALREADY_EXISTS,
                    HttpStatus.CONFLICT
            );
        }

        subgroupMapper.toEntity(subgroup, subgroupDto);
        subgroup = subgroupRepo.save(subgroup);
        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.UPDATED);

        return subgroupMapper.toDto(subgroup);
    }

    @Override
    public SubgroupDto delete(UUID id) {
        var subgroup = subgroupRepo.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorMessage.SUBGROUP_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        subgroupRepo.delete(subgroup);
        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.DELETED);

        return subgroupMapper.toDto(subgroup);
    }
}
