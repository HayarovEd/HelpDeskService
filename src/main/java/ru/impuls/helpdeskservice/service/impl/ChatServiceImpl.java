package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;
import ru.impuls.helpdeskservice.constant.ErrorMessage;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.request.MessageCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.entity.Subgroup;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.*;
import ru.impuls.helpdeskservice.service.ChatService;
import ru.impuls.helpdeskservice.service.ConnectionService;
import ru.impuls.helpdeskservice.util.AuthUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepo chatRepository;
    private final SubgroupRepo subgroupRepo;
    private final MessageRepo messageRepo;
    private final ChatMapper chatMapper;
    private final ConnectionService connectionService;
    private final MessageMapper messageMapper;
    private final ConnectionMapper connectionMapper;
    private final GroupRepo groupRepo;
    private final ConnectionRepo connectionRepo;
    private final UserRepo userRepo;

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

        /*// Валидация параметров
        validateParameters(count, groupId, subgroupId);

        // Получение текущего пользователя для фильтра isMy
        User currentUser = isMy != null && isMy ?
                AuthUtil.getAuthenticatedUser() : null;

        // Создание спецификации для фильтрации
        Specification<Chat> spec = Specification.where(null);

        // Фильтрация по подгруппе или группе
        if (subgroupId != null) {
            spec = spec.and(ChatSpecifications.hasSubgroupId(subgroupId));
        } else if (groupId != null) {
            spec = spec.and(ChatSpecifications.hasGroupId(groupId));
        }

        // Фильтрация по дате последнего сообщения
        if (to != null) {
            spec = spec.and(ChatSpecifications.hasLastMessageBefore(to));
        }

        // Фильтрация по поисковой строке
        if (search != null && !search.isEmpty()) {
            spec = spec.and(ChatSpecifications.matchesSearch(search));
        }

        // Фильтрация по статусу открытости
        if (isOpen != null) {
            spec = spec.and(ChatSpecifications.isOpen(isOpen));
        }

        // Фильтрация по статусу работы
        if (isWork != null) {
            spec = spec.and(ChatSpecifications.isWork(isWork));
        }

        // Фильтрация по участию пользователя
        if (currentUser != null) {
            spec = spec.and(ChatSpecifications.hasUser(currentUser));
        }

        // Сортировка и пагинация
        Pageable pageable = PageRequest.of(0, count, Sort.by("createdDate").descending());
        Page<Chat> chatPage = chatRepository.findAll(spec, pageable);

        // Для каждого чата загружаем только последнее подключение и сообщение
        List<Chat> chats = chatPage.getContent();
        for (Chat chat : chats) {
            loadLastConnectionAndMessage(chat);
        }

        return chatPage.map(chatMapper::toDto);*/

        //List<Chat> chatPage = chatRepository.findAll();

        return chatRepository.findAll().stream()
                .map(this::processChat)
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    private Chat processChat(Chat chat) {
        List<Connection> sortedConnections = chat.getConnections().stream()
                .sorted(Comparator.comparing(Connection::getStartDate).reversed())
                .map(this::processConnection)
                .collect(Collectors.toList());

        chat.setConnections(sortedConnections);
        return chat;
    }

    private Connection processConnection(Connection connection) {
        List<Message> sortedMessages = connection.getMessages().stream()
                .sorted(Comparator.comparing(Message::getCreatedDate).reversed())
                .collect(Collectors.toList());

        connection.setMessages(sortedMessages);
        return connection;
    }

    @Override
    public ChatDto create(UUID subgroupId, ChatCreateDto chatCreateDto) {
        Subgroup subgroup = subgroupRepo.findById(subgroupId)
                .orElseThrow(() -> new AppException(
                        "Subgroup not found",
                        HttpStatus.NOT_FOUND
                ));

        Chat chat = chatMapper.toEntity(chatCreateDto, subgroup);
        chatRepository.save(chat);

        Connection connection = connectionService.createInitialConnection(chat, chatCreateDto.getMessage());
        chat.setConnections(Collections.singletonList(connection));

        return chatMapper.toDto(chat);
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

        List<Connection> filteredConnections = connections.stream()
                .map(connection -> {
                    Connection filteredConnection = connectionMapper.toEntity(connection);

                    List<Message> filteredMessages;
                    if (to == null) {
                        filteredMessages = messageRepo.findTopNByConnectionId(
                                connection.getId(), count);
                    } else {
                        filteredMessages = messageRepo.findTopNByConnectionIdAndCreatedDate(
                                connection.getId(), to, count);
                    }

                    filteredConnection.setMessages(filteredMessages);
                    return filteredConnection;
                })
                .toList();

        Chat filteredChat  = chatMapper.toEntity(chat, filteredConnections);

        return chatMapper.toDto(filteredChat );
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

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        // Закрываем текущее соединение, если оно есть
        connectionService.closeCurrentConnection(chat);

        // Создаем новое соединение с поддержкой
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

        // Проверяем, что текущий пользователь является отвечающим
        connectionService.validateCurrentUserIsResponder(chat, userId);

        // Закрываем текущее соединение
        connectionService.closeCurrentConnection(chat);

        // Создаем новое соединение без поддержки
        connectionService.createClientConnection(chat);

        chat.setWork(false);
        chatRepository.save(chat);
    }

    @Override
    public void closeChat(UUID chatId) {
        var userId = AuthUtil.getAuthenticatedUser().getId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        // Проверяем, что текущий пользователь является отвечающим
        connectionService.validateCurrentUserIsResponder(chat, userId);

        // Закрываем текущее соединение
        connectionService.closeCurrentConnection(chat);

        chat.setOpen(false);
        chat.setWork(false);
        chatRepository.save(chat);
    }

    /*@Override
    public void sendClientMessage(UUID chatId, MessageCreateDto messageCreateDto) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.addClientMessage(chat, messageCreateDto.getText());
    }

    @Override
    public void sendSupportMessage(UUID chatId, UUID userId, MessageCreateDto messageCreateDto) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        connectionService.addSupportMessage(chat, userId, messageCreateDto.getText());
    }*/

    private void validateParameters(int count, UUID groupId, UUID subgroupId) {
        if (count < 0 || count > 1000) {
            throw new AppException("Count must be between 0 and 1000", HttpStatus.BAD_REQUEST);
        }

        if (subgroupId != null && !subgroupRepo.existsById(subgroupId)) {
            throw new AppException(ErrorMessage.SUBGROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (groupId != null && !groupRepo.existsById(groupId)) {
            throw new AppException(ErrorMessage.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
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
            }

            chat.setConnections(Collections.singletonList(lastConnection));
        }
    }
}
