package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.repository.*;
import ru.impuls.helpdeskservice.service.ChatService;
import ru.impuls.helpdeskservice.service.ConnectionService;
import ru.impuls.helpdeskservice.util.AuthUtil;
import ru.impuls.helpdeskservice.util.ChatSpecifications;
import ru.impuls.helpdeskservice.constant.WebsocketEventType;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.impuls.helpdeskservice.constant.ErrorMessage.SUBGROUP_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepo chatRepository;
    private final SubgroupRepo subgroupRepo;
    private final MessageRepo messageRepo;
    private final ChatMapper chatMapper;
    private final ConnectionService connectionService;
    private final WebSocketHandler webSocketHandler;
    private final ConnectionMapper connectionMapper;
    private final GroupRepo groupRepo;
    private final ConnectionRepo connectionRepo;
    private final UserRepo userRepo;

    @Override
    public ChatDto create(UUID subgroupId, ChatCreateRequest chatRequest) {
        var subgroup = subgroupRepo.findById(subgroupId)
                .orElseThrow(() -> new AppException(SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        var chat = chatMapper.toEntity(chatRequest, subgroup);
        chatRepository.save(chat);

        var connectionDto = connectionService.createInitialConnection(chat, chatRequest.message());

        webSocketHandler.notifyChange("/chats", chatMapper.toEvent(chat, connectionDto), WebsocketEventType.CREATED);

        return chatMapper.toDto(chat, connectionDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatDto getById(UUID chatId, Instant to, int count) {
        if (count < 0 || count > 1000) {
            throw new AppException("Count must be between 0 and 1000", HttpStatus.BAD_REQUEST);
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException("Chat not found", HttpStatus.NOT_FOUND));

        List<Connection> connections = chat.getConnections().stream()
                .sorted(Comparator.comparing(Connection::getStartDate).reversed())
                .toList();

        int remaining = count;
        List<Connection> filteredConnections = new ArrayList<>();

        for (Connection connection : connections) {
            if (remaining <= 0) {
                break;
            }

            /*if (to != null && connection.getStartDate().isBefore(to)) {
                continue;
            }*/

            List<Message> messages;
            if (to == null) {
                messages = messageRepo.findTopNByConnectionId(connection.getId(), remaining);
            } else {
                messages = messageRepo.findTopNByConnectionIdAndCreatedDate(connection.getId(), to, remaining);
            }

            Connection connWithMessages = connectionMapper.toEntity(connection);
            if (!messages.isEmpty()) {
                connWithMessages.setMessages(messages);
                remaining -= messages.size();
            } else {
                connWithMessages.setMessages(Collections.emptyList());
            }
            filteredConnections.add(connWithMessages);
        }

        Chat filteredChat = chatMapper.toEntity(chat, filteredConnections);
        return chatMapper.toDto(filteredChat);
    }

    @Override
    public ChatDto delete(UUID id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new AppException("Chat not found", HttpStatus.NOT_FOUND));

        chatRepository.delete(chat);

        return chatMapper.toDto(chat);
    }

    @Override
    public void joinChat(UUID chatId) {
        var userId = AuthUtil.getAuthenticatedUser().getId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        if (!chat.isOpen()) {
            throw new AppException(
                    ErrorMessage.CHAT_IS_CLOSED,
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.closeCurrentConnection(chat);
        connectionService.createSupportConnection(chat, user);

        chat.setWork(true);
        chatRepository.save(chat);
    }

    @Override
    public void exitChat(UUID chatId) {
        var userId = AuthUtil.getAuthenticatedUser().getId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.validateCurrentUserIsResponder(chat, userId);
        connectionService.closeCurrentConnection(chat);
        connectionService.createClientConnection(chat);

        chat.setWork(false);
        chatRepository.save(chat);
    }

    @Override
    public void closeChat(UUID chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.closeCurrentConnection(chat);
        chat.setOpen(false);
        chat.setWork(false);

        chatRepository.save(chat);

        notifyChatUpdated(chat);
    }

    @Override
    public void sendClientMessage(UUID chatId, MessageCreateDto messageCreateDto) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        boolean wasClosed = !chat.isOpen();
        connectionService.addClientMessage(chat, messageCreateDto.getText());

        if (wasClosed && chat.isOpen()) {
            notifyChatUpdated(chat);
        }
    }

    @Override
    public void sendSupportMessage(UUID chatId, MessageCreateDto messageCreateDto) {
        var userId = AuthUtil.getAuthenticatedUser().getId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.addSupportMessage(chat, userId, messageCreateDto.getText());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto> getChats(
            UUID groupId,
            UUID subgroupId,
            Instant to,
            String search,
            Boolean isOpen,
            Boolean isWork,
            Boolean isMy,
            int count) {

        validateParameters(count, groupId, subgroupId);

        var currentUser = (isMy != null && isMy) ? AuthUtil.getAuthenticatedUser() : null;
        var spec = buildSpecification(groupId, subgroupId, to, search, isOpen, isWork, currentUser);
        var pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());
        var chatPage = chatRepository.findAll(spec, pageable);
        var chats = chatPage.getContent();

        for (Chat chat : chats) {
            loadLastConnectionAndMessage(chat);
        }

        return chats.stream()
                .map(chatMapper::toDto)
                .toList();
    }

    private Specification<Chat> buildSpecification(UUID groupId, UUID subgroupId,
                                                   Instant to, String search,
                                                   Boolean isOpen, Boolean isWork,
                                                   User currentUser) {
        return Specification.allOf(
                subgroupId != null ? ChatSpecifications.hasSubgroupId(subgroupId) : null,
                subgroupId == null && groupId != null ? ChatSpecifications.hasGroupId(groupId) : null,
                to != null ? ChatSpecifications.hasLastMessageBefore(to) : null,
                search != null && !search.isEmpty() ? ChatSpecifications.matchesSearch(search) : null,
                isOpen != null ? ChatSpecifications.isOpen(isOpen) : null,
                isWork != null ? ChatSpecifications.isWork(isWork) : null,
                currentUser != null ? ChatSpecifications.hasUser(currentUser) : null
        );
    }

    private void loadLastConnectionAndMessage(Chat chat) {
        // Получаем последнее подключение для чата
        Pageable connectionPageable = PageRequest.of(0, 1, Sort.by("startDate").descending());
        Page<Connection> connections = connectionRepo.findByChatId(chat.getId(), connectionPageable);

        if (!connections.isEmpty()) {
            Connection lastConnection = connections.getContent().getFirst();

            // Получаем последнее сообщение для подключения
            Pageable messagePageable = PageRequest.of(0, 1, Sort.by("createdDate").descending());
            Page<Message> messages = messageRepo.findByConnectionId(lastConnection.getId(), messagePageable);

            if (!messages.isEmpty()) {
                lastConnection.setMessages(Collections.singletonList(messages.getContent().getFirst()));
            } else {
                lastConnection.setMessages(Collections.emptyList());
            }

            chat.setConnections(Collections.singletonList(lastConnection));
        } else {
            chat.setConnections(Collections.emptyList());
        }
    }

    private void validateParameters(int count, UUID groupId, UUID subgroupId) {
        if (count < 0 || count > 1000) {
            throw new AppException("Count must be between 0 and 1000", HttpStatus.BAD_REQUEST);
        }

        if (subgroupId != null && !subgroupRepo.existsById(subgroupId)) {
            throw new AppException(SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (groupId != null && !groupRepo.existsById(groupId)) {
            throw new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    private void notifyChatUpdated(Chat chat) {
        loadLastConnectionAndMessage(chat);

        ChatDto chatDto = chatMapper.toDto(chat);
        webSocketHandler.notifyChange("/chats", chatDto, WebsocketEventType.UPDATED);
    }
}
