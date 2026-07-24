package ru.impuls.helpdeskservice.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.entity.Platform;
import ru.impuls.helpdeskservice.firebase.service.FcmService;
import ru.impuls.helpdeskservice.firebase.service.HuaweiPushService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FcmService fcmService;
    private final HuaweiPushService hmsService;

    @Async
    @Override
    public void sendNotifications(Platform platform, List<String> tokens, String text, UUID chatId) {
        try {
            switch (platform) {
                case FCM -> fcmService.sendNotificationByTokens(
                        tokens,
                        "Новое сообщение",
                        text,
                        chatId.toString()
                );
                case HMS -> hmsService.sendNotification(
                        tokens,
                        "Новое сообщение",
                        text
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send notification for platform " + platform + ": " + e.getMessage());
        }
    }
}
