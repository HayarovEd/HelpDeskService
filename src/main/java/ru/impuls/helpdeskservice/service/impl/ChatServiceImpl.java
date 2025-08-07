package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.dto.request.ChatCreateDto;
import ru.impuls.helpdeskservice.dto.response.ChatDto;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;
import ru.impuls.helpdeskservice.entity.Subgroup;
import ru.impuls.helpdeskservice.exception.AppException;
import ru.impuls.helpdeskservice.mapper.ChatMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapper;
import ru.impuls.helpdeskservice.mapper.ConnectionMapperImpl;
import ru.impuls.helpdeskservice.mapper.MessageMapper;
import ru.impuls.helpdeskservice.repository.ChatRepo;
import ru.impuls.helpdeskservice.repository.MessageRepo;
import ru.impuls.helpdeskservice.repository.SubgroupRepo;
import ru.impuls.helpdeskservice.service.ChatService;
import ru.impuls.helpdeskservice.service.ConnectionService;

import java.time.Instant;
import java.util.*;

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

    /*@Override
    @Transactional(readOnly = true)
    public Page<ChatDto> getChats(
            UUID groupId,
            UUID subgroupId,
            Instant to,
            String search,
            Boolean isOpen,
            Boolean isWork,
            int count) {

        if (count < 0 || count > 1000) {
            throw new AppException(
                    "Count must be between 0 and 1000",
                    HttpStatus.BAD_REQUEST);
        }
        if (!groupRepo.existsById(groupId)) {
            throw new AppException(
                    ErrorMessage.GROUP_NOT_FOUND,
                    HttpStatus.NOT_FOUND
            );
        }
        if (!subgroupRepo.existsById(subgroupId)) {
            throw new AppException(
                    ErrorMessage.SUBGROUP_NOT_FOUND,
                    HttpStatus.NOT_FOUND
            );
        }

        PageRequest pageable = PageRequest.of(
                0,
                count,
                Sort.by("createdDate").descending()
        );

        // TODO: Реализовать фильтрацию по isMy (чаты, в которых участвовал пользователь)

        LocalDateTime toDateTime = to != null ?
                LocalDateTime.ofInstant(to, ZoneId.systemDefault()) : null;

        Page<Chat> chats = chatRepository.findChatsWithFilters(
                groupId, subgroupId, toDateTime, search, isOpen, isWork, pageable);

        return chats.map(chatMapper::toDto);*/

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

        List<Connection> connections = chat.getConnections();

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

    /*@Override
    public void joinChat(UUID chatId, UUID userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AppException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND
                ));

        User user = userRepository.findById(userId)
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
    public void exitChat(UUID chatId, UUID userId) {
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
    public void closeChat(UUID chatId, UUID userId) {
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

    @Override
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
}
