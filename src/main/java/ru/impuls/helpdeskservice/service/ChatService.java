package ru.impuls.helpdeskservice.service;

import org.springframework.data.domain.Page;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;

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

    /*void joinChat(UUID chatId, UUID userId);

    void exitChat(UUID chatId, UUID userId);

    void closeChat(UUID chatId, UUID userId);

    void sendClientMessage(UUID chatId, MessageCreateDto messageCreateDto);

    void sendSupportMessage(UUID chatId, UUID userId, MessageCreateDto messageCreateDto);*/
}
