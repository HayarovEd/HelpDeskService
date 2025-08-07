package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.List;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ChatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "connections", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "name", source = "chatDto.name")
    @Mapping(target = "subgroup", source = "subgroup")
    @Mapping(target = "isOpen", expression = "java(true)")
    @Mapping(target = "isWork", expression = "java(false)")
    Chat toEntity(ChatCreateDto chatDto, Subgroup subgroup);

    ChatDto toDto(Chat chat);

    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "name", source = "chat.name")
    @Mapping(target = "email", source = "chat.email")
    @Mapping(target = "phone", source = "chat.phone")
    @Mapping(target = "isOpen", source = "chat.open")
    @Mapping(target = "isWork", source = "chat.work")
    @Mapping(target = "subgroup", source = "chat.subgroup")
    @Mapping(target = "createdDate", source = "chat.createdDate")
    @Mapping(target = "connections", source = "connections")
    Chat toEntity(Chat chat, List<Connection> connections);
}
