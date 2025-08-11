package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.ConnectionRepo;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.service.ConnectionService;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepo connectionRepository;
    private final MessageRepo messageRepository;
    private final ConnectionMapper connectionMapper;
    private final MessageMapper messageMapper;
    private final UserRepo userRepo;

    @Override
    public Connection createInitialConnection(Chat chat, String messageText) {

        Connection connection = connectionMapper.toEntity(chat);
        connectionRepository.save(connection);

        Message message = messageMapper.toEntity(connection, messageText, false);
        messageRepository.save(message);

        connection.setMessages(Collections.singletonList(message));

        return connection;
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
                .build();

        connectionRepository.save(connection);
    }

    @Override
    public void createClientConnection(Chat chat) {
        Connection connection = Connection.builder()
                .chat(chat)
                .startDate(Instant.now())
                .build();

        connectionRepository.save(connection);
    }

    @Override
    public void validateCurrentUserIsResponder(Chat chat, UUID userId) {
        Connection currentConnection = getCurrentConnection(chat);
        if (currentConnection == null
                || currentConnection.getUser() == null
                || !currentConnection.getUser().getId().equals(userId)) {
            throw new AppException(
                    "You are not the responder of this chat",
                    HttpStatus.FORBIDDEN);
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
                .build();

        messageRepository.save(message);
    }

    @Override
    public void addSupportMessage(Chat chat, UUID userId, String text) {

        if (!chat.isOpen()) {
            throw new AppException(
                    ErrorMessage.CHAT_IS_CLOSED,
                    HttpStatus.BAD_REQUEST
            );
        }

        Connection currentConnection = getCurrentConnection(chat);
        validateCurrentUserIsResponder(chat, userId);
        Message message = Message.builder()
                .connection(currentConnection)
                .text(text)
                .isSupport(true)
                .build();

        messageRepository.save(message);
    }

    private Connection getCurrentConnection(Chat chat) {
        return chat.getConnections().stream()
                .filter(c -> c.getEndDate() == null)
                .findFirst()
                .orElse(null);
    }
}
