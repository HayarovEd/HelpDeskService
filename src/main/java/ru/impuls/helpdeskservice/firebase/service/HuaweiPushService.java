package ru.impuls.helpdeskservice.firebase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.firebase.client.HuaweiPushClient;
import ru.impuls.helpdeskservice.firebase.model.HmsMessage;
import ru.impuls.helpdeskservice.firebase.model.HmsNotification;
import ru.impuls.helpdeskservice.firebase.model.HmsPushRequest;
import ru.impuls.helpdeskservice.firebase.model.HmsPushResponse;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HuaweiPushService {
    private final HmsTokenService tokenService;
    private final HuaweiPushClient pushClient;

    @Value("${huawei.push.app-id}")
    private String appId;

    public void sendNotification(List<String> deviceTokens, String title, String body) {
        String token = tokenService.getAccessToken();
        if (token == null) {
            log.error("Отмена отправки: токен авторизации пустой.");
            return;
        }

        // Построение строго типизированного запроса
        HmsNotification notification = new HmsNotification(title, body);

        HmsMessage message = new HmsMessage(notification, deviceTokens);
        HmsPushRequest request = new HmsPushRequest(false, message);

        try {
            String authHeader = "Bearer " + token;
            HmsPushResponse response = pushClient.sendPush(appId, authHeader, request);

            if (response != null) {
                log.info("Ответ Huawei API. Код: {}, Текст: {}", response.code(), response.msg());
            }
        } catch (Exception e) {
            log.error("Критическая ошибка при отправке пуша через OpenFeign: ", e);
        }
    }
}
