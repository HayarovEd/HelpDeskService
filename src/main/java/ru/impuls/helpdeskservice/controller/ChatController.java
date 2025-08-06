package ru.impuls.helpdeskservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.service.ChatService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    //@PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Page<ChatDto>> getChats(
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID subgroupId,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isOpen,
            @RequestParam(required = false) Boolean isWork,
            @RequestParam(required = false) Boolean isMy) {

        /*Page<ChatDto> chats = chatService.getChats(
                groupId, subgroupId, to, search, isOpen, isWork, count);*/

        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ChatDto create(
            @RequestParam UUID subgroupId,
            @RequestBody ChatCreateDto chatCreateDto) {
        return chatService.create(subgroupId, chatCreateDto);
    }

    @GetMapping("/{id}")
    public ChatDto getById(
            @PathVariable UUID id,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count) {

        return chatService.getById(id, to, count);
    }

    /*@PostMapping("/{id}/join")
    @PreAuthorize("hasRole('SUPPORT')")
    @Operation(
            summary = "Присоединиться к чату по ID",
            description = "Роль: SUPPORT\n\nМожно присоединиться, только если у чата нет отвечающего. Данные берутся из JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Присоединиться к чату"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Чат не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> joinChat(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.joinChat(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/exit")
    @PreAuthorize("hasAuthority('SUPPORT')")
    public ResponseEntity<Void> exitChat(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.exitChat(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('SUPPORT')")
    public ResponseEntity<Void> closeChat(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.closeChat(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/message/client")
    public ResponseEntity<Void> sendClientMessage(
            @PathVariable UUID id,
            @RequestBody MessageCreateDto messageCreateDto) {
        chatService.sendClientMessage(id, messageCreateDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/message/support")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> sendSupportMessage(
            @PathVariable UUID id,
            @RequestBody MessageCreateDto messageCreateDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatService.sendSupportMessage(id, userDetails.getId(), messageCreateDto);
        return ResponseEntity.ok().build();
    }*/
}