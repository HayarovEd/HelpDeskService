package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.constant.LogActionType;
import ru.impuls.helpdeskservice.constant.LogEntityType;
import ru.impuls.helpdeskservice.dto.response.SubgroupResponse;
import ru.impuls.helpdeskservice.dto.request.SubgroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditRequest;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.SubgroupMapper;
import ru.impuls.helpdeskservice.repository.GroupRepo;
import ru.impuls.helpdeskservice.repository.SubgroupRepo;
import ru.impuls.helpdeskservice.service.LogService;
import ru.impuls.helpdeskservice.service.SubgroupService;
import ru.impuls.helpdeskservice.websocket.EventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SubgroupServiceImpl implements SubgroupService {

    private final SubgroupRepo subgroupRepo;
    private final SubgroupMapper subgroupMapper;
    private final GroupRepo groupRepo;
    private final WebSocketHandler webSocketHandler;
    private final LogService logService;

    @Override
    public SubgroupResponse create(UUID groupId, SubgroupCreateRequest subgroupRequest) {
        var group = groupRepo.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (subgroupRepo.existsByNameAndGroupId(subgroupRequest.name(), groupId)) {
            throw new AppException(ErrorMessage.SUBGROUP_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        var subgroup = subgroupMapper.toEntity(subgroupRequest, group);
        subgroup = subgroupRepo.save(subgroup);

        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.CREATED);

        return subgroupMapper.toDto(subgroup);

    }

    @Override
    public SubgroupResponse update(UUID id, SubgroupEditRequest subgroupRequest) {
        var subgroup = subgroupRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        var groupId = subgroup.getGroup().getId();

        if (!subgroup.getName().equals(subgroupRequest.name())
                && subgroupRepo.existsByNameAndGroupId(subgroupRequest.name(), groupId)) {
            throw new AppException(ErrorMessage.SUBGROUP_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        subgroupMapper.toEntity(subgroup, subgroupRequest);
        subgroup = subgroupRepo.save(subgroup);

        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.UPDATED);

        return subgroupMapper.toDto(subgroup);
    }

    @Override
    public SubgroupResponse delete(UUID id) {
        var subgroup = subgroupRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorMessage.SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        subgroupRepo.delete(subgroup);
        webSocketHandler.notifyChange("/subgroups", subgroupMapper.toEventDto(subgroup), EventType.DELETED);
        logService.create(
                LogActionType.DELETED,
                LogEntityType.SUBGROUP,
                subgroup.getName(),
                subgroup.getId()
        );

        return subgroupMapper.toDto(subgroup);
    }
}
