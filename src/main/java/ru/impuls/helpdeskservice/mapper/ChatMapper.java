package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.dto.internal.GroupForSubDto;
import ru.impuls.helpdeskservice.dto.response.GroupResponse;
import ru.impuls.helpdeskservice.entity.*;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.websocket.event.ChatEvent;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {Collections.class})
public abstract class ChatMapper {

    @Autowired
    protected MessageRepo messageRepo;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "connections", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "name", source = "chatRequest.name")
    @Mapping(target = "group", source = "group")
    @Mapping(target = "open", expression = "java(true)")
    @Mapping(target = "work", expression = "java(false)")
    @Mapping(target = "active", expression = "java(true)")
    public abstract Chat toEntity(ChatCreateRequest chatRequest, Group group);

    @Mapping(source = "chat.id", target = "id")
    @Mapping(target = "unreadMessagesCount", expression = "java(messageRepo.countUnreadByChatId(chat.getId()))")
    public abstract ChatDto toDto(Chat chat);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "name", source = "chat.name")
    @Mapping(target = "email", source = "chat.email")
    @Mapping(target = "phone", source = "chat.phone")
    @Mapping(target = "group", source = "chat.group")
    @Mapping(target = "createdDate", source = "chat.createdDate")
    @Mapping(target = "connections", source = "connections")
    public abstract Chat toEntity(Chat chat, List<Connection> connections);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "connections", expression = "java(Collections.singletonList(connectionDto))")
    @Mapping(target = "unreadMessagesCount", expression = "java(messageRepo.countUnreadByChatId(chat.getId()))")
    public abstract ChatEvent toEvent(Chat chat, ConnectionDto connectionDto);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "connections", expression = "java(Collections.singletonList(connectionDto))")
    @Mapping(target = "unreadMessagesCount", expression = "java(messageRepo.countUnreadByChatId(chat.getId()))")
    public abstract ChatDto toDto(Chat chat, ConnectionDto connectionDto);

    @Mapping(source = "group", target = "group")
    public abstract ChatEvent toEvent(ChatDto newChat);

    public abstract GroupForSubDto toGroupForSubDto(GroupResponse groupResponse);

    public abstract GroupForSubDto toGroupForSubDto(Group group);
}
