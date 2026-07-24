package ru.impuls.helpdeskservice.firebase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.impuls.helpdeskservice.firebase.model.HmsAccessTokenRequest;
import ru.impuls.helpdeskservice.firebase.model.HmsTokenResponse;

@FeignClient(name = "huawei-auth-client", url = "${huawei.oauth-url}")
public interface HuaweiAuthClient {

    // Используем Spring-совместимый парсер форм через Spring MVC аннотации
    @PostMapping(
            value = "oauth2/v3/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    HmsTokenResponse getAccessToken(@RequestBody HmsAccessTokenRequest request);
}
