package ru.impuls.helpdeskservice.service.notification;

import ru.impuls.helpdeskservice.entity.Platform;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void sendNotifications(Platform platform, List<String> tokens, String text, UUID chatId);
}
