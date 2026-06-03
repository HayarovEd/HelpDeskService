package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.entity.Chat;

import java.util.UUID;

public interface ConnectionService {
    ConnectionDto createInitialConnection(Chat chat, String messageText, User user);

    void closeCurrentConnection(Chat chat);

    void createSupportConnection(Chat chat, User user);

    void createClientConnection(Chat chat);

    void validateCurrentUserIsResponder(Chat chat, UUID userId);

    void addClientMessage(Chat chat, String text, User user);

    void addSupportMessage(Chat chat, UUID userId, String text);

    void addSystemMessage(Chat chat, String text);
}
