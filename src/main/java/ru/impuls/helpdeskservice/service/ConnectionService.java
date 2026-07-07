package ru.impuls.helpdeskservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.List;
import java.util.UUID;

public interface ConnectionService {
    ConnectionDto createInitialConnection(Chat chat, String messageText, User user);

    Connection closeCurrentConnection(Chat chat);

    Connection createSupportConnection(Chat chat, User user);

    void createClientConnection(Chat chat);

    void validateCurrentUserIsResponder(Chat chat, UUID userId);

    void addClientMessage(Chat chat, String text, User user, List<MultipartFile> files);

    void addSupportMessage(Chat chat, UUID userId, String text, List<MultipartFile> files);

    void addSystemMessage(Connection connection, String text);
}
