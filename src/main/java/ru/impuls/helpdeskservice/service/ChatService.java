package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatDto> getChats(
            UUID groupId,
            UUID subgroupId,
            Instant to,
            String search,
            Boolean isOpen,
            Boolean isWork,
            Boolean isMy,
            int count
    );

    ChatDto create(UUID subgroupId, ChatCreateDto chatCreateDto);

    ChatDto getById(UUID id, Instant to, int count);

    ChatDto delete(UUID id);

    void joinChat(UUID chatId);

    void exitChat(UUID chatId);

    /*void closeChat(UUID chatId, UUID userId);

    void sendClientMessage(UUID chatId, MessageCreateDto messageCreateDto);

    void sendSupportMessage(UUID chatId, UUID userId, MessageCreateDto messageCreateDto);*/
}
