package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;
import ru.impuls.helpdeskservice.dto.internal.MessageDto;
import ru.impuls.helpdeskservice.dto.request.ChatCreateRequest;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.firebase.service.FcmService;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.*;
import ru.impuls.helpdeskservice.service.ChatService;
import ru.impuls.helpdeskservice.service.ConnectionService;
import ru.impuls.helpdeskservice.util.AuthUtil;
import ru.impuls.helpdeskservice.util.ChatSpecifications;
import ru.impuls.helpdeskservice.websocket.WebSocketHandler;

import java.time.Instant;
import java.util.*;

import static ru.impuls.helpdeskservice.constant.ErrorMessage.*;
import static ru.impuls.helpdeskservice.constant.WebsocketEventType.CREATED;
import static ru.impuls.helpdeskservice.constant.WebsocketEventType.UPDATED;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepo chatRepository;
    // private final SubgroupRepo subgroupRepo;
    private final MessageRepo messageRepo;
    private final ChatMapper chatMapper;
    private final ConnectionService connectionService;
    private final WebSocketHandler webSocketHandler;
    private final ConnectionMapper connectionMapper;
    private final GroupRepo groupRepo;
    private final ConnectionRepo connectionRepo;
    private final UserRepo userRepo;
    private final MessageMapper messageMapper;
    private final FcmService fcmService;

    @Override
    @Transactional(readOnly = true)
    public List<ChatDto> getAll(
            UUID groupId,
            //  UUID subgroupId,
            Instant to,
            String search,
            Boolean isOpen,
            Boolean isWork,
            Boolean isMy,
            int count) {

        validateParameters(count, groupId);

        var currentUser = (isMy != null && isMy) ? AuthUtil.getAuthenticatedUser() : null;
        var spec = buildSpecification(groupId, to, search, isOpen, isWork, currentUser);
        var pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());
        var chatPage = chatRepository.findAll(spec, pageable);
        var chats = chatPage.getContent();
        var newChatDtos = new ArrayList<ChatDto>();

        for (Chat chat : chats) {
            var newChat = chatMapper.toDto(chat);
            loadLastConnectionAndMessage(newChat);
            newChatDtos.add(newChat);
        }

        return newChatDtos;
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
    public ChatDto create(UUID groupId, ChatCreateRequest chatRequest) {
        var group = groupRepo.findById(groupId)
                .orElseThrow(() -> new AppException(GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        User currentUser = userRepo.findById(AuthUtil.getAuthenticatedUser().getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        var chat = chatMapper.toEntity(chatRequest, group);
        chatRepository.save(chat);

        var connectionDto = connectionService.createInitialConnection(chat, chatRequest.message(), currentUser);
        webSocketHandler.notifyChangeAfterCommit("/chats", chatMapper.toEvent(chat, connectionDto), CREATED);

        return chatMapper.toDto(chat, connectionDto);
    }

    @Override
    public ChatDto delete(UUID id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new AppException("Chat not found", HttpStatus.NOT_FOUND));

        chatRepository.delete(chat);

        return chatMapper.toDto(chat);
    }

    @Override
    public void join(UUID chatId) {
        User currentUser = userRepo.findById(AuthUtil.getAuthenticatedUser().getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        Chat chat = chatRepository.findById(chatId)
                .map(c -> {
                    if (!c.isOpen()) {
                        throw new AppException(ErrorMessage.CHAT_IS_CLOSED, HttpStatus.BAD_REQUEST);
                    }
                    if (c.isWork()) {
                        throw new AppException(ErrorMessage.CHAT_IS_WORKED_BY_ANOTHER_USER, HttpStatus.BAD_REQUEST);
                    }
                    return c;
                })
                .orElseThrow(() -> new AppException(CHAT_NOT_FOUND, HttpStatus.NOT_FOUND));

        connectionService.closeCurrentConnection(chat);
        Connection newConnection = connectionService.createSupportConnection(chat, currentUser);
        connectionService.addSystemMessage(newConnection, "К чату подключился " + currentUser.getName());

        chat.setWork(true);
        messageRepo.markAllAsReadByChatId(chatId);
        Chat updatedChat = chatRepository.save(chat);
        notifyChatUpdated(updatedChat);
    }

    @Override
    public void exit(UUID chatId) {
        User currentUser = userRepo.findById(AuthUtil.getAuthenticatedUser().getId())
                .orElseThrow(() -> new AppException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(CHAT_NOT_FOUND, HttpStatus.NOT_FOUND));

        connectionService.validateCurrentUserIsResponder(chat, currentUser.getId());
        Connection closedConnection = connectionService.closeCurrentConnection(chat);
        connectionService.addSystemMessage(closedConnection, "Из чата вышел " + currentUser.getName());
        connectionService.createClientConnection(chat);

        chat.setWork(false);
        Chat updatedChat = chatRepository.save(chat);
        notifyChatUpdated(updatedChat);
    }

    @Override
    public void close(UUID chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(CHAT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (!chat.isOpen() && !chat.isWork()) {
            throw new AppException(ErrorMessage.CHAT_IS_CLOSED, HttpStatus.BAD_REQUEST);
        }

        Connection closedConnection = connectionService.closeCurrentConnection(chat);
        connectionService.addSystemMessage(closedConnection, "Чат был закрыт");
        chat.setOpen(false);
        chat.setWork(false);
        chat.setActive(false);
        Chat updatedChat = chatRepository.save(chat);
        notifyChatUpdated(updatedChat);
    }

    @Override
    public void sendClientMessage(UUID chatId, MessageCreateDto messageCreateDto, List<MultipartFile> files) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(CHAT_NOT_FOUND, HttpStatus.NOT_FOUND));

        var user = AuthUtil.getAuthenticatedUser();
        Message newMessage = connectionService.addClientMessage(chat, messageCreateDto.getText(), user, files);
        setActiveChat(chat);
        notifyChatUpdated(chat, newMessage);
    }

    @Override
    public void sendSupportMessage(UUID chatId, MessageCreateDto messageCreateDto, List<MultipartFile> files) {
        var userId = AuthUtil.getAuthenticatedUser().getId();
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(CHAT_NOT_FOUND, HttpStatus.NOT_FOUND));
        Message newMessage = connectionService.addSupportMessage(chat, userId, messageCreateDto.getText(), files);
        setActiveChat(chat);
        var users = chat.getConnections().stream()
                .map(Connection::getUser)
                .filter(Objects::nonNull)
                .distinct() // Если нужны уникальные пользователи
                .toList();
        users.forEach(user -> {
                    if (user.getId() != userId) {
                        user.getFirebaseTokens().forEach(fcmToken -> {
                                    fcmService.sendNotificationByToken(
                                            fcmToken.getToken(),
                                            "Новое сообщение",
                                            messageCreateDto.getText(),
                                            chat.getId()
                                    );
                                }
                        );
                    }
                }
        );

        notifyChatUpdated(chat, newMessage);
    }

    @Override
    public List<ChatDto> getByUserLogin(String login) {
        var chats = chatRepository.findOpenChatsByName(login);
        var newChatDtos = new ArrayList<ChatDto>();

        for (Chat chat : chats) {
            var newChat = chatMapper.toDto(chat);
            loadLastConnectionAndMessage(newChat);
            newChatDtos.add(newChat);
        }

        return newChatDtos;
    }

    private Specification<Chat> buildSpecification(UUID groupId,
                                                   Instant to, String search,
                                                   Boolean isOpen, Boolean isWork,
                                                   User currentUser) {
        return Specification.allOf(
                //  subgroupId != null ? ChatSpecifications.hasSubgroupId(subgroupId) : null,
                /*subgroupId == null && */groupId != null ? ChatSpecifications.hasGroupId(groupId) : null,
                to != null ? ChatSpecifications.hasLastMessageBefore(to) : null,
                search != null && !search.isEmpty() ? ChatSpecifications.matchesSearch(search) : null,
                isOpen != null ? ChatSpecifications.isOpen(isOpen) : null,
                isWork != null ? ChatSpecifications.isWork(isWork) : null,
                currentUser != null ? ChatSpecifications.hasUser(currentUser) : null
        );
    }

    private void loadLastConnectionAndMessage(ChatDto chatDto) {
        var lastConnection = connectionRepo
                .findLastByChatId(chatDto.getId())
                .stream()
                .findFirst()
                .orElse(null);

        if (lastConnection == null) {
            chatDto.setConnections(Collections.emptyList());
            return;
        }

        var lastMessage = messageRepo.findLastByChatId(chatDto.getId()).orElse(null);

        if (lastMessage != null) {
            MessageDto messageDto = messageMapper.toDto(lastMessage);
            ConnectionDto connectionDto = connectionMapper.toDto(lastMessage.getConnection(), messageDto);
            chatDto.setConnections(List.of(connectionDto));
        } else {
            ConnectionDto connectionDto = connectionMapper.toDto(lastConnection);
            chatDto.setConnections(List.of(new ConnectionDto(
                    connectionDto.id(),
                    connectionDto.startDate(),
                    connectionDto.endDate(),
                    connectionDto.user(),
                    Collections.emptyList()
            )));
        }
    }

    private void validateParameters(int count, UUID groupId) {
        if (count < 0 || count > 1000) {
            throw new AppException("Count must be between 0 and 1000", HttpStatus.BAD_REQUEST);
        }

       /* if (subgroupId != null && !subgroupRepo.existsById(subgroupId)) {
            throw new AppException(SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }*/

        if (groupId != null && !groupRepo.existsById(groupId)) {
            throw new AppException(GROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    private void setActiveChat(Chat chat) {
        chat.setActive(true);
        chatRepository.save(chat);
    }

    private void notifyChatUpdated(Chat chat) {
        var chatDto = chatMapper.toDto(chat);
        loadLastConnectionAndMessage(chatDto);
        webSocketHandler.notifyChangeAfterCommit("/chats", chatMapper.toEvent(chatDto), UPDATED);
        webSocketHandler.notifyChatMessages(chat.getId(), chatMapper.toEvent(chatDto));
    }

    private void notifyChatUpdated(Chat chat, Message message) {
        var chatDto = chatMapper.toDto(chat);
        loadLastConnectionAndMessage(chatDto);
        if (message != null) {
            MessageDto messageDto = messageMapper.toDto(message);
            ConnectionDto connectionDto = connectionMapper.toDto(message.getConnection(), messageDto);
            chatDto.setConnections(List.of(connectionDto));
        }
        var chatEvent = chatMapper.toEvent(chatDto);
        webSocketHandler.notifyChangeAfterCommit("/chats", chatEvent, UPDATED);
        webSocketHandler.notifyChatMessages(chat.getId(), chatEvent);
    }
}
