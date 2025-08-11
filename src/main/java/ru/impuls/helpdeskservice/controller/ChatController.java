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
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.service.ChatService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public List<ChatDto> getChats(
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) UUID subgroupId,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isOpen,
            @RequestParam(required = false) Boolean isWork,
            @RequestParam(required = false) Boolean isMy) {

        return chatService.getChats(groupId, subgroupId, to, search, isOpen, isWork, isMy, count);
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ChatDto delete(@PathVariable UUID id) {
        return chatService.delete(id);
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void joinChat(@PathVariable UUID id) {
        chatService.joinChat(id);
    }

    @PostMapping("/{id}/exit")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void exitChat(@PathVariable UUID id) {
        chatService.exitChat(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void closeChat(@PathVariable UUID id) {
        chatService.closeChat(id);
    }

    /*@PostMapping("/{id}/message/client")
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