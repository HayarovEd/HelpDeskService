package ru.impuls.helpdeskservice.service;

import java.util.UUID;

public interface MessageService {
    void markAsRead(UUID messageId);
}
