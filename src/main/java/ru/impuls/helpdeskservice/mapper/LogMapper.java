package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.Mapper;
import ru.impuls.helpdeskservice.dto.response.LogDto;
import ru.impuls.helpdeskservice.entity.Log;

@Mapper(componentModel = "spring")
public interface LogMapper {
    LogDto toDto(Log log);
}
