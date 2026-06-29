package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.service.MessageService;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepo messageRepository;
    private final WebSocketHandler webSocketHandler;
    private final MessageMapper messageMapper;

    @Override
    public void markAsRead(UUID messageId) {
        messageRepository.findById(messageId)
                .ifPresent(message -> {
                    if (message.getIsRead() == null || !message.getIsRead()) {
                        message.setIsRead(true);
                        messageRepository.save(message);
                        
                        webSocketHandler.notifyChangeAfterCommit(
                            "/messages/read", 
                            messageMapper.toDto(message), 
                            WebsocketEventType.UPDATED
                        );
                    }
                }
                );
    }
}
