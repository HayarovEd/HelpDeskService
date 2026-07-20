package ru.impuls.helpdeskservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.impuls.helpdeskservice.dto.response.ValidateToken;

@FeignClient(
        name = "external-service",
        url = "${external.service.base-url}"  // читаем из application.yml
)
public interface ExternalServiceClient {


    @GetMapping("auth/is_valid")
    ValidateToken validateToken(@RequestParam("token") String token);
}
