package ru.impuls.helpdeskservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatService {

    List<ChatDto> getAll(
            UUID groupId,
           // UUID subgroupId,
            Instant to,
            String search,
            Boolean isOpen,
            Boolean isWork,
            Boolean isMy,
            int count
    );

    ChatDto getById(UUID id, Instant to, int count);

    ChatDto create(UUID groupId, ChatCreateRequest chatRequest);

    ChatDto delete(UUID id);

    void join(UUID chatId);

    void exit(UUID chatId);

    void close(UUID chatId);

    void sendClientMessage(
            UUID chatId,
            MessageCreateDto messageCreateDto,
            List<MultipartFile> files
    );

    void sendSupportMessage(UUID chatId, MessageCreateDto messageCreateDto);

    List<ChatDto> getByUserLogin(String login);
}
