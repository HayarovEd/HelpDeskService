package ru.impuls.helpdeskservice.firebase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.impuls.helpdeskservice.firebase.model.HmsPushRequest;
import ru.impuls.helpdeskservice.firebase.model.HmsPushResponse;

@FeignClient(name = "huawei-push-client", url = "${huawei.push-url}")
public interface HuaweiPushClient {

    @PostMapping(
            value = "v1/{appId}/messages:send",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    HmsPushResponse sendPush(
            @PathVariable("appId") String appId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody HmsPushRequest request
    );
}