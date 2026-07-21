package ru.impuls.helpdeskservice.firebase.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@RequiredArgsConstructor
public class FcmService {
    private final FirebaseMessaging firebaseMessaging;
    private final FireBaseRepo tokenRepo;
    private final UserRepo userRepo;
    private final FcmMapper fcmMapper;


    public void registerToken(TokenRegistrationRequest tokenRegistrationRequest) {
        var user = AuthUtil.getAuthenticatedUser();
        User currentUser = userRepo.findById(user.getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        var fcmToken = fcmMapper.toEntity(tokenRegistrationRequest.token(), currentUser);
        tokenRepo.save(fcmToken);
    }


    public void sendNotificationByToken(
            String token,
            String title,
            String body,
            @Nullable UUID chatId
    ) throws ExecutionException, InterruptedException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
        Map<String, String> chatMap = new java.util.HashMap<>(Collections.emptyMap());
        if (chatId != null) {
            chatMap.put("chat_id", chatId.toString());
        }

        Message message = Message.builder()
                .setFid(token)
                .putAllData(chatMap)
                .setNotification(notification)
                .build();

        firebaseMessaging.sendAsync(message).get();
    }

    public void sendNotificationToTopic(
            String topic,
            String title,
            String body
    ) throws ExecutionException, InterruptedException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .build();

        firebaseMessaging.sendAsync(message).get();
    }
}
