package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.impuls.helpdeskservice.dto.internal.MessageDto;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.FileMetaData;
import ru.impuls.helpdeskservice.entity.Message;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", source = "connection.startDate")
    @Mapping(target = "connection", source = "connection")
    @Mapping(target = "isRead", expression = "java(false)")
    Message toEntity(
            Connection connection,
            String text,
            boolean isSupport,
            boolean isRead,
            List<FileMetaData> files
            );

    MessageDto toDto(Message message);
}
