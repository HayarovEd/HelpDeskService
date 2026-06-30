package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.entity.*;
import ru.impuls.helpdeskservice.websocket.event.ChatEvent;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.response.ChatDto;

import java.util.Collections;
import java.util.List;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {Collections.class})
public interface ChatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "connections", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "name", source = "chatRequest.name")
    @Mapping(target = "group", source = "group")
    @Mapping(target = "open", expression = "java(true)")
    @Mapping(target = "work", expression = "java(false)")
    @Mapping(target = "active", expression = "java(true)")
    Chat toEntity(ChatCreateRequest chatRequest, Group group);

    ChatDto toDto(Chat chat);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "name", source = "chat.name")
    @Mapping(target = "email", source = "chat.email")
    @Mapping(target = "phone", source = "chat.phone")
    @Mapping(target = "group", source = "chat.group")
    @Mapping(target = "createdDate", source = "chat.createdDate")
    @Mapping(target = "connections", source = "connections")
    Chat toEntity(Chat chat, List<Connection> connections);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "connections", expression = "java(Collections.singletonList(connectionDto))")
    ChatEvent toEvent(Chat chat, ConnectionDto connectionDto);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "connections", expression = "java(Collections.singletonList(connectionDto))")
    ChatDto toDto(Chat chat, ConnectionDto connectionDto);

    ChatEvent toEvent(ChatDto newChat);
}
