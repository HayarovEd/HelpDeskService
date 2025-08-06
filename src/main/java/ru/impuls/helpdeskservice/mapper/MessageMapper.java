package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.dto.MessageDto;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "connection", source = "connection")
    Message toEntity(Connection connection, String text, boolean isSupport);

    //@Mapping(target = "isSupport", source = "isSupport")
    MessageDto toDto(Message message);
}
