package ru.impuls.helpdeskservice.mapper;

import org.mapstruct.*;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Subgroup;


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
}
