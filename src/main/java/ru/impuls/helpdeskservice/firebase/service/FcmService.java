package ru.impuls.helpdeskservice.firebase.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.firebase.mapper.FcmMapper;
import ru.impuls.helpdeskservice.firebase.model.TokenRegistrationRequest;
import ru.impuls.helpdeskservice.repository.FireBaseRepo;
import ru.impuls.helpdeskservice.util.AuthUtil;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    private final FirebaseMessaging firebaseMessaging;
    private final FireBaseRepo tokenRepo;
    private final UserRepo userRepo;
    private final FcmMapper fcmMapper;


    public void registerToken(TokenRegistrationRequest tokenRegistrationRequest) {
        var user = AuthUtil.getAuthenticatedUser();
        User currentUser = userRepo.findById(user.getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var fcmToken = fcmMapper.toEntity(tokenRegistrationRequest, currentUser);
        tokenRepo.save(fcmToken);
    }


    public void sendNotificationByToken(
            String token,
            String title,
            String body,
            @Nullable UUID chatId
    ) {
        log.info("Sending FCM notification: token={}, title={}, chatId={}", token, title, chatId);

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Map<String, String> chatMap = new java.util.HashMap<>();
        if (chatId != null) {
            chatMap.put("chat_id", chatId.toString());
        }

        Message message = Message.builder()
                .setToken(token)
                .putAllData(chatMap)
                .setNotification(notification)
                .build();

        try {
            String response = firebaseMessaging.send(message); // синхронный вызов, проще
            log.info("FCM sent successfully to token={}: {}", token, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM failed for token={}: code={}, message={}",
                    token, e.getMessagingErrorCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending FCM to token={}: {}", token, e.getMessage(), e);
        }
    }

    public void sendNotificationByTokens(
            List<String> tokens,
            String title,
            String body,
            @Nullable String chatId
    ) {
        log.info("FCM: sending to {} tokens, chatId={}", tokens.size(), chatId);

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        if (chatId != null) {
            data.put("chat_id", chatId);
        }

        // убираем дубли токенов
        Set<String> uniqueTokens = new HashSet<>(tokens);

        uniqueTokens.forEach(token -> {
            Message message = Message.builder()
                    .setToken(token)
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send notification to token {}: {}", token, e.getMessage());
            }
        });
    }

    public void sendNotificationToTopic(
            String topic,
            String title,
            String body
    ) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.sendAsync(message).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send notification to topic {}: {}", topic, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
