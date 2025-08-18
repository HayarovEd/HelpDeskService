package ru.impuls.helpdeskservice.websocket;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.dto.LogItemDto;
import ru.impuls.helpdeskservice.dto.event.UserEvent;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // Add these new maps to track chat-specific subscriptions
    private final Map<UUID, Set<WebSocketSession>> chatConnectionsSubscribers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<WebSocketSession>> chatMessagesSubscribers = new ConcurrentHashMap<>();

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

        // Check if this is a chat-specific subscription
        String path = Objects.requireNonNull(session.getUri()).getPath();
        if (path != null) {
            try {
                if (path.contains("/chats/") && path.contains("/connections")) {
                    UUID chatId = extractChatIdFromPath(path);
                    chatConnectionsSubscribers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                            .add(session);
                } else if (path.contains("/chats/") && path.contains("/messages")) {
                    UUID chatId = extractChatIdFromPath(path);
                    chatMessagesSubscribers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                            .add(session);
                }
            } catch (Exception e) {
                log.error("Error processing WebSocket connection for path: {}", path, e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        sessions.remove(session);
        UUID userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }

        // Remove from chat-specific subscriptions
        chatConnectionsSubscribers.values().forEach(set -> set.remove(session));
        chatMessagesSubscribers.values().forEach(set -> set.remove(session));
    }

    public <T> void notifyChange(String path, T data, WebsocketEventType eventType) {
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

    private <T> String createMessage(String path, T data, WebsocketEventType eventType) {
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

    public <T> void notifyUser(UUID userId, String path, T data, WebsocketEventType eventType) {
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

        String userRole = (String) session.getAttributes().get("role");

        if (data instanceof LogItemDto) {
            return "ADMIN".equals(userRole);
        } else if (data instanceof UserEvent) {
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

    // Add new methods to notify specific chat subscribers
    public <T> void notifyChatConnections(UUID chatId, T data) {
        Set<WebSocketSession> subscribers = chatConnectionsSubscribers.getOrDefault(chatId, Collections.emptySet());
        String message = createMessage("/chats/" + chatId + "/connections", data, UPDATED);
        sendToSessions(subscribers, message);
    }

    public <T> void notifyChatMessages(UUID chatId, T data) {
        Set<WebSocketSession> subscribers = chatMessagesSubscribers.getOrDefault(chatId, Collections.emptySet());
        String message = createMessage("/chats/" + chatId + "/messages", data, UPDATED);
        sendToSessions(subscribers, message);
    }

    private void sendToSessions(Set<WebSocketSession> sessions, String message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Error sending WebSocket message", e);
            }
        });
    }

    private UUID extractChatIdFromPath(String path) {
        // Extract UUID from path like "/ws/chats/123e4567-e89b-12d3-a456-426614174000/connections"
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("chats") && i + 1 < parts.length) {
                try {
                    return UUID.fromString(parts[i + 1]);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid chat ID in path: {}", path);
                    throw e;
                }
            }
        }
        throw new IllegalArgumentException("No chat ID found in path: " + path);
    }
}