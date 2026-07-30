package ru.impuls.helpdeskservice.firebase.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.firebase.client.HuaweiAuthClient;
import ru.impuls.helpdeskservice.firebase.model.HmsAccessTokenRequest;
import ru.impuls.helpdeskservice.firebase.model.HmsTokenResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class HmsTokenService {

    private final HuaweiAuthClient authClient;


    @Value("${huawei.push.client-id}")
    private String clientId;

    @Value("${huawei.push.client-secret}")
    private String clientSecret;

    @Cacheable(value = "huaweiToken")
    public String getAccessToken() {
        HmsAccessTokenRequest request = new HmsAccessTokenRequest(
                "client_credentials",
                clientId,
                clientSecret
        );

        try {
            HmsTokenResponse response = authClient.getAccessToken(request);
            if (response != null && response.token() != null) {
                return response.token(); // Возвращаем чистую строку токена
            }
        } catch (Exception e) {
            log.error("Ошибка авторизации в Huawei OAuth через Feign: ", e);
        }
        return null;
    }

    /**
     * Автоматический сброс кэша каждые 55 минут.
     */
    @Scheduled(fixedRate = 3300000)
    @CacheEvict(value = "huaweiToken", allEntries = true)
    public void clearTokenCache() {
        log.info("Кэш токена Huawei автоматически сброшен по расписанию.");
    }
}
