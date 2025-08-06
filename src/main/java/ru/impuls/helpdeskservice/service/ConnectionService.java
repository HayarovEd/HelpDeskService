package ru.impuls.helpdeskservice.service;

import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.UUID;

public interface ConnectionService {

    Connection createInitialConnection(Chat chat, String messageText);

    void closeCurrentConnection(Chat chat);

    void createSupportConnection(Chat chat, User user);

    void createClientConnection(Chat chat);

    void validateCurrentUserIsResponder(Chat chat, UUID userId);

    void addClientMessage(Chat chat, String text);

    void addSupportMessage(Chat chat, UUID userId, String text);


}
