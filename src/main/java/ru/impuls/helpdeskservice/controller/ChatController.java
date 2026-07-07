package ru.impuls.helpdeskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.service.ChatService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ChatDto> getAll(
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isOpen,
            @RequestParam(required = false) Boolean isWork,
            @RequestParam(required = false) Boolean isMy) {

        return chatService.getAll(groupId, to, search, isOpen, isWork, isMy, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDto create(
            @RequestParam UUID groupId,
            @RequestBody @Valid ChatCreateRequest chatRequest) {
        return chatService.create(groupId, chatRequest);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ChatDto getById(
            @PathVariable UUID id,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int count) {

        return chatService.getById(id, to, count);
    }

    @GetMapping("/user_chats")
    @ResponseStatus(HttpStatus.OK)
    public List<ChatDto> getByUserLogin(
            @RequestParam(name = "login") String login) {
        return chatService.getByUserLogin(login);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ChatDto delete(@PathVariable UUID id) {
        return chatService.delete(id);
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void join(@PathVariable UUID id) {
        chatService.join(id);
    }

    @PostMapping("/{id}/exit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void exit(@PathVariable UUID id) {
        chatService.exit(id);
    }

    @PostMapping("/{id}/close")
    @ResponseStatus(HttpStatus.OK)
    public void close(@PathVariable UUID id) {
        chatService.close(id);
    }

    @PostMapping(
            value = "/{id}/message/client",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void sendClientMessage(
            @PathVariable UUID id,
            @RequestPart("data") @Valid MessageCreateDto messageCreateDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        chatService.sendClientMessage(id, messageCreateDto, files);
    }

    @PostMapping("/{id}/message/support")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPPORT')")
    public void sendSupportMessage(
            @PathVariable UUID id,
            @RequestPart("data") @Valid MessageCreateDto messageCreateDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        log.info("=== FILE UPLOAD DIAGNOSTICS ===");
        log.info("Chat ID: {}", id);

        if (files != null) {
            log.info("Number of files: {}", files.size());
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                log.info("File {}:", i + 1);
                log.info("  - Name: {}", file.getOriginalFilename());
                log.info("  - Size: {} MB", file.getSize() / 1024 / 1024);
                log.info("  - Size: {} bytes", file.getSize());
                log.info("  - ContentType: {}", file.getContentType());
            }

            // Проверяем общий размер
            long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
            log.info("Total size: {} MB", totalSize / 1024 / 1024);

            // Проверяем лимиты
            long maxFileSize = 500 * 1024 * 1024; // 500 MB
            if (totalSize > maxFileSize) {
                log.warn("Total size {} MB exceeds limit {} MB",
                        totalSize / 1024 / 1024, maxFileSize / 1024 / 1024);
                throw new RuntimeException("Total file size exceeds limit: " + totalSize);
            }

            for (MultipartFile file : files) {
                if (file.getSize() > maxFileSize) {
                    log.warn("File {} size {} MB exceeds limit {} MB",
                            file.getOriginalFilename(),
                            file.getSize() / 1024 / 1024,
                            maxFileSize / 1024 / 1024);
                    throw new RuntimeException("File too large: " + file.getOriginalFilename());
                }
            }
        }

        log.info("=== END DIAGNOSTICS ===");
        chatService.sendSupportMessage(id, messageCreateDto, files);
    }
}