package ru.impuls.helpdeskservice.websocket;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.websocket.event.LogItemEvent;

import static ru.impuls.helpdeskservice.constant.WebsocketEventType.CONNECTED;
import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();

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

        String path = Objects.requireNonNull(session.getUri()).getPath();
        if (path != null) {
            try {
                UUID chatId = extractChatIdFromPath(path);
                if (chatId != null) {
                    if (path.contains("/connections")) {
                        chatConnectionsSubscribers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                                .add(session);
                    } else if (path.contains("/messages")) {
                        chatMessagesSubscribers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                                .add(session);
                    }
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

        chatConnectionsSubscribers.values().forEach(set -> set.remove(session));
        chatMessagesSubscribers.values().forEach(set -> set.remove(session));
    }

    public <T> void notifyChangeAfterCommit(String path, T data, WebsocketEventType eventType) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyChange(path, data, eventType);
            }
        });
    }

    public <T> void notifyChange(String path, T data, WebsocketEventType eventType) {
        String message = createMessage(path, data, eventType);

        sessions.forEach(session -> {
            try {
                if (session.isOpen() && shouldReceiveMessage(session, path) && shouldSendMessage(session, data)) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Error sending WebSocket message", e);
            }
        });
    }

    private boolean shouldReceiveMessage(WebSocketSession session, String messagePath) {
        String sessionPath = getSessionPath(session);
        if ("/ws".equals(sessionPath)) {
            return true;
        }
        if (sessionPath != null && sessionPath.contains("/chats/")) {
            UUID sessionChatId = extractChatIdFromPath(sessionPath);
            UUID messageChatId = extractChatIdFromPath(messagePath);
            return sessionChatId != null && sessionChatId.equals(messageChatId);
        }
        return false;
    }

    private String getSessionPath(WebSocketSession session) {
        try {
            return Objects.requireNonNull(session.getUri()).getPath();
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
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
                log.info(e.getMessage());
            }
        }
    }

    private record WebSocketEvent<T>(String path, String event, T data) {}

    private <T> boolean shouldSendMessage(WebSocketSession session, T data) {
        String userRole = (String) session.getAttributes().get("role");
        if (data instanceof LogItemEvent) {
            return "ADMIN".equals(userRole);
        }
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

    public <T> void notifyChatConnections(UUID chatId, T data) {
        Set<WebSocketSession> subscribers = chatConnectionsSubscribers.getOrDefault(chatId, Collections.emptySet());
        String message = createMessage("/chats/" + chatId + "/connections", data, CONNECTED);
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
        if (path == null) return null;
        Pattern pattern = Pattern.compile("/chats/([0-9a-fA-F-]{36})");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group(1));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format in path: {}", path);
            }
        }
        return null;
    }
}
