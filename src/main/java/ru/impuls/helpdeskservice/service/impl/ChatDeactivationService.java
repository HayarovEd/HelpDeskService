package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.repository.ChatRepo;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatDeactivationService {

    private final ChatRepo chatRepository;
    private final WebSocketHandler webSocketHandler;
    private final ChatMapper chatMapper;

    // Run every 30 minutes
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void deactivateInactiveChats() {
        log.info("Running scheduled task to deactivate inactive chats...");

        Instant thirtyMinutesAgo = Instant.now().minus(30, ChronoUnit.MINUTES);

        List<Chat> chatsToDeactivate = chatRepository.findChatsToDeactivate(thirtyMinutesAgo);

        if (chatsToDeactivate.isEmpty()) {
            log.info("No chats to deactivate.");
            return;
        }

        log.info("Found {} chats to deactivate: {}", chatsToDeactivate.size(), chatsToDeactivate.stream().map(Chat::getId).toList());

        for (Chat chat : chatsToDeactivate) {
            chat.setActive(false);
            var chatDto = chatMapper.toDto(chat);
            webSocketHandler.notifyChangeAfterCommit("/chats", chatMapper.toEvent(chatDto), UPDATED);
        }

        chatRepository.saveAll(chatsToDeactivate);
        log.info("Successfully deactivated {} chats.", chatsToDeactivate.size());

    }
}
