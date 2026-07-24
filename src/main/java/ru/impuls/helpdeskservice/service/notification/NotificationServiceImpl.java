package ru.impuls.helpdeskservice.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.firebase.service.FcmService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FcmService fcmService;

    @Async
    @Override
    public void sendNotification(String token, String text, UUID chatId) {
        try {
            fcmService.sendNotificationByToken(
                    token,
                    "Новое сообщение",
                    text,
                    chatId
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
