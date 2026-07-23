package ru.impuls.helpdeskservice.firebase.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.firebase.model.TokenRegistrationRequest;
import ru.impuls.helpdeskservice.firebase.service.FcmService;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/register-token")
    @ResponseStatus(HttpStatus.OK)
    public void registerToken(
            @Valid
            @RequestBody
            TokenRegistrationRequest tokenRegistrationRequest) {
        fcmService.registerToken(tokenRegistrationRequest);
    }

    @PostMapping("/send-notification")
    @ResponseStatus(HttpStatus.OK)
    public void sendNotificationToUser(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body
    ) {
        fcmService.sendNotificationByToken(token, title, body, null);
    }

    @PostMapping("/send-topic-notification")
    @ResponseStatus(HttpStatus.OK)
    public void sendNotificationTopic(
            @RequestParam String topic,
            @RequestParam String title,
            @RequestParam String body
    )  {
        fcmService.sendNotificationToTopic(topic, title, body);
    }
}
