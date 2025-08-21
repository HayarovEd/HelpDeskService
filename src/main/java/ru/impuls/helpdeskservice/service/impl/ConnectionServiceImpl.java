package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.ConnectionRepo;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.service.ConnectionService;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Service
@Transactional
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepo connectionRepository;
    private final MessageRepo messageRepository;
    private final ConnectionMapper connectionMapper;
    private final MessageMapper messageMapper;
    private final WebSocketHandler webSocketHandler;
    private final ChatMapper chatMapper;

    @Override
    public ConnectionDto createInitialConnection(Chat chat, String messageText) {

        var connection = connectionMapper.toEntity(chat);
        connectionRepository.save(connection);

        var message = messageMapper.toEntity(connection, messageText, false);
        messageRepository.save(message);
        connection.setMessages(Collections.singletonList(message));

        notifyInCreateConnection(chat, connection);

        return connectionMapper.toDto(connection);
    }

    @Override
    public void closeCurrentConnection(Chat chat) {
        Connection currentConnection = getCurrentConnection(chat);
        if (currentConnection != null) {
            currentConnection.setEndDate(Instant.now());
            connectionRepository.save(currentConnection);
        }
    }

    @Override
    public void createSupportConnection(Chat chat, User user) {
        Connection connection = Connection.builder()
                .chat(chat)
                .user(user)
                .startDate(Instant.now())
                .build();

        connectionRepository.save(connection);
        notifyInCreateConnection(chat, connection);
    }

    @Override
    public void createClientConnection(Chat chat) {
        Connection connection = Connection.builder()
                .chat(chat)
                .startDate(Instant.now())
                .build();

        connectionRepository.save(connection);
        notifyInCreateConnection(chat, connection);
    }

    @Override
    public void validateCurrentUserIsResponder(Chat chat, UUID userId) {
        Connection currentConnection = getCurrentConnection(chat);
        if (currentConnection == null
                || currentConnection.getUser() == null
                || !currentConnection.getUser().getId().equals(userId)) {
            throw new AppException("You are not the responder of this chat", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public void addClientMessage(Chat chat, String text) {
        Connection currentConnection;

        if (!chat.isOpen()) {
            createInitialConnection(chat, text);
            chat.setOpen(true);
            return;
        }

        currentConnection = getCurrentConnection(chat);
        Message message = Message.builder()
                .connection(currentConnection)
                .text(text)
                .isSupport(false)
                .createdDate(Instant.now())
                .build();

        saveMessageAndNotifyChange(chat, message, currentConnection);
    }

    @Override
    public void addSupportMessage(Chat chat, UUID userId, String text) {

        if (!chat.isOpen()) {
            throw new AppException(ErrorMessage.CHAT_IS_CLOSED, HttpStatus.BAD_REQUEST);
        }

        Connection currentConnection = getCurrentConnection(chat);
        validateCurrentUserIsResponder(chat, userId);
        Message message = Message.builder()
                .connection(currentConnection)
                .text(text)
                .isSupport(true)
                .createdDate(Instant.now())
                .build();

        saveMessageAndNotifyChange(chat, message, currentConnection);
    }

    private void notifyInCreateConnection(Chat chat, Connection connection) {
        var connectionDto = connectionMapper.toDto(connection);
        var chatEvent = chatMapper.toEvent(chat, connectionDto);

        webSocketHandler.notifyChange("/chats/connections", chatEvent, UPDATED);
        webSocketHandler.notifyChatConnections(chat.getId(), chatEvent);
    }

    private void saveMessageAndNotifyChange(Chat chat, Message message, Connection connection) {
        messageRepository.save(message);

        var messageDto = messageMapper.toDto(message);
        var connectionDto = connectionMapper.toDto(connection, messageDto);
        var notificationChat = chatMapper.toEvent(chat, connectionDto);

        webSocketHandler.notifyChange("/chats/messages", notificationChat, UPDATED);
        webSocketHandler.notifyChatMessages(chat.getId(), notificationChat);
    }

    private Connection getCurrentConnection(Chat chat) {
        return chat.getConnections().stream()
                .filter(c -> c.getEndDate() == null)
                .findFirst()
                .orElse(null);
    }
}
