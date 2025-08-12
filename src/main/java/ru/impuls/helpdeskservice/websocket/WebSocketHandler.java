package ru.impuls.helpdeskservice.websocket;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.impuls.helpdeskservice.auth.dto.response.UserResDto;
import ru.impuls.helpdeskservice.dto.LogItemDto;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public WebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        sessions.add(session);
        UUID userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        sessions.remove(session);
        UUID userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
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

    public <T> void notifyUser(UUID userId, String path, T data, EventType eventType) {
        WebSocketSession userSession = userSessions.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                String message = createMessage(path, data, eventType);
                userSession.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                // Логирование ошибки
            }
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
        } else if (data instanceof UserResDto) {
            return "ADMIN".equals(userRole);
        }

        // Для других типов данных отправляем всем
        return true;
    }

    private UUID extractUserIdFromSession(WebSocketSession session) {
        try {
            return  UUID.fromString((String) session.getAttributes().get("userId"));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }
}