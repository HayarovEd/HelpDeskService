package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.dto.LogItemDto;
import ru.impuls.helpdeskservice.entity.Log;

@Mapper(componentModel = "spring")
public interface LogMapper {
    LogItemDto toDto(Log log);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    Log toEntity(String text, User user);
}
