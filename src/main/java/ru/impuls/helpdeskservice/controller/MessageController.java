package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.service.MessageService;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{messageId}/read")
    @ResponseStatus(HttpStatus.OK)
    public void markMessageAsRead(@PathVariable UUID messageId) {
        messageService.markAsRead(messageId);
    }
}
