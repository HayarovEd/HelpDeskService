package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
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
}