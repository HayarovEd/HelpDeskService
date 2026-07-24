package ru.impuls.helpdeskservice.service.notification;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(String token, String text, UUID chatId);
}
