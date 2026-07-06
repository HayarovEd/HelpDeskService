package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.ConnectionRepo;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.service.ConnectionService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepo connectionRepository;
    private final MessageRepo messageRepository;
    private final ConnectionMapper connectionMapper;
    private final MessageMapper messageMapper;

    @Override
    public ConnectionDto createInitialConnection(Chat chat, String messageText, User user) {
        var connection = connectionMapper.toEntity(chat, user);
        connectionRepository.save(connection);

        var message = messageMapper.toEntity(
                connection,
                messageText,
                false,
                false,
                Collections.emptyList()
        );
        messageRepository.save(message);
        connection.setMessages(Collections.singletonList(message));

        return connectionMapper.toDto(connection);
    }

    @Override
    public Connection closeCurrentConnection(Chat chat) {
        Connection currentConnection = getCurrentConnection(chat);
        if (currentConnection != null) {
            currentConnection.setEndDate(Instant.now());
            return connectionRepository.save(currentConnection);
        }
        return null;
    }

    @Override
    public Connection createSupportConnection(Chat chat, User user) {
        Connection connection = Connection.builder()
                .chat(chat)
                .user(user)
                .startDate(Instant.now())
                .build();
        return connectionRepository.save(connection);
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
            throw new AppException("You are not the responder of this chat", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public void addClientMessage(Chat chat, String text, User user) {
        Connection currentConnection;

        if (!chat.isOpen()) {
            createInitialConnection(chat, text, user);
            chat.setOpen(true);
            return;
        }

        currentConnection = getCurrentConnection(chat);
        Message message = Message.builder()
                .connection(currentConnection)
                .text(text)
                .isSupport(false)
                .isRead(false)
                .createdDate(Instant.now())
                .build();
        messageRepository.save(message);
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
                .isRead(false)
                .createdDate(Instant.now())
                .build();
        messageRepository.save(message);
    }

    @Override
    public void addSystemMessage(Connection connection, String text) {
        if (connection == null) return;

        Message message = Message.builder()
                .connection(connection)
                .text(text)
                .isRead(false)
                .isSupport(true) //// Treat system messages as support messages for styling
                .createdDate(Instant.now())
                .build();
        messageRepository.save(message);
    }

    private Connection getCurrentConnection(Chat chat) {
        List<Connection> connections = chat.getConnections();

        log.info("Close connection, Всего соединений: {}", connections.size());

        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            log.info("Close connection, {}. ID: {}", i, c.getId());
            log.info("Close connection,    endDate: {}", c.getEndDate());
            log.info("Close connection,    endDate == null? {}", c.getEndDate() == null);
            log.info("Close connection,    startDate: {}", c.getStartDate());
            log.info("Close connection, ---");
        }

        Connection result = connections.stream()
                .filter(c -> c.getEndDate() == null)
                .findFirst()
                .orElse(null);

        log.info("Close connection, Результат: {}", result != null ? result.getId() : "null");
        return result;
    }
}
