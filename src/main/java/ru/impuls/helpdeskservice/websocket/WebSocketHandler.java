package ru.impuls.helpdeskservice.websocket;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
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
}