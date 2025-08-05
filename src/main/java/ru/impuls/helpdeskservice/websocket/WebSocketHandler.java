package ru.impuls.helpdeskservice.websocket;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.impuls.helpdeskservice.auth.dto.response.UserDto;
import ru.impuls.helpdeskservice.dto.LogItemDto;
import ru.impuls.helpdeskservice.dto.response.GroupEventDto;
import ru.impuls.helpdeskservice.dto.response.SubgroupEventDto;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public WebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        sessions.remove(session);
    }

    public <T> void notifyChange(String path, T data, EventType eventType) {
        String message = createMessage(path, data, eventType);

        sessions.forEach(session -> {
            try {
                if (shouldSendMessage(session, data)) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (IOException e) {
                // Логирование ошибки
            }
        });
    }

    private <T> String createMessage(String path, T data, EventType eventType) {
        try {
            return objectMapper.writeValueAsString(new WebSocketEvent<>(
                    path,
                    eventType.name(),
                    data
            ));
        } catch (Exception e) {
            return String.format("{\"path\":\"%s\",\"event\":\"%s\",\"data\":{}}",
                    path, eventType);
        }
    }

    private record WebSocketEvent<T>(String path, String event, T data) {}

    private <T> boolean shouldSendMessage(WebSocketSession session, T data) {
        // Проверяем expiration
        /*Instant expiration = (Instant) session.getAttributes().get("expiration");
        if (expiration == null || expiration.isBefore(Instant.now())) {
            return false;
        }*/

        String userRole = (String) session.getAttributes().get("role");

        if (data instanceof LogItemDto) {
            return "ADMIN".equals(userRole);
        } else if (data instanceof UserDto) {
            return "ADMIN".equals(userRole);
        }

        // Для других типов данных отправляем всем
        return true;
    }
}