package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.impuls.helpdeskservice.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m " +
            "WHERE m.connection.id = :connectionId " +
            "ORDER BY m.createdDate DESC " +
            "LIMIT :count")
    List<Message> findTopNByConnectionId(
            @Param("connectionId") UUID connectionId,
            @Param("count") int count);

    @Query("SELECT m FROM Message m WHERE m.connection.id = :connectionId " +
            "AND m.createdDate < :to " +
            "ORDER BY m.createdDate DESC " +
            "LIMIT :count")
    List<Message> findTopNByConnectionIdAndCreatedDate(
            @Param("connectionId") UUID connectionId,
            @Param("to") Instant to,
            @Param("count") int count);

    @Query("SELECT m FROM Message m " +
            "WHERE m.connection.id = :connectionId " +
            "ORDER BY m.createdDate DESC " +
            "LIMIT 1")
    Optional<Message> findLastByConnectionId(@Param("connectionId") UUID connectionId);

    @Query("SELECT count(m) FROM Message m WHERE m.connection.chat.id = :chatId AND m.isRead = false AND m.isSupport = false")
    long countUnreadByChatId(@Param("chatId") UUID chatId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.connection.chat.id = :chatId AND m.isRead = false AND m.isSupport = false")
    void markAllAsReadByChatId(@Param("chatId") UUID chatId);
}
