package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.dto.internal.MessageDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.Collections;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = Collections.class)
public interface ConnectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "chat", source = "chat")
    @Mapping(target = "startDate", source = "chat.createdDate")
    Connection toEntity(Chat chat);

    @Mapping(target = "messages", ignore = true)
    Connection toEntity(Connection connection);

    ConnectionDto toDto(Connection connection);

    @Mapping(target = "id", source = "connection.id")
    @Mapping(target = "messages", expression = "java(Collections.singletonList(messageDto))")
    ConnectionDto toDto(Connection connection, MessageDto messageDto);
}