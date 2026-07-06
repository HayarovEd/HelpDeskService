package ru.impuls.helpdeskservice.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.service.ChatService;
import ru.impuls.helpdeskservice.service.MessageService;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.UUID;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepo messageRepository;
    private final WebSocketHandler webSocketHandler;
    private final ChatMapper chatMapper;
    private final ChatService chatService;

    public MessageServiceImpl(
            MessageRepo messageRepository,
            WebSocketHandler webSocketHandler,
            ChatMapper chatMapper,
            @Lazy ChatService chatService
    ) {
        this.messageRepository = messageRepository;
        this.webSocketHandler = webSocketHandler;
        this.chatMapper = chatMapper;
        this.chatService = chatService;
    }

    @Override
    @Transactional
    public void markAsRead(UUID messageId) {
        messageRepository.findById(messageId)
                .ifPresent(message -> {
                    if (message.getIsRead() == null || !message.getIsRead()) {
                        message.setIsRead(true);
                        messageRepository.save(message);

                        Chat chat = message.getConnection().getChat();
                        var chatDto = chatMapper.toDto(chat);

                        webSocketHandler.notifyChangeAfterCommit("/chats", chatMapper.toEvent(chatDto), UPDATED);
                        webSocketHandler.notifyChatMessages(chat.getId(), chatMapper.toEvent(chatDto));
                    }
                });
    }
}
