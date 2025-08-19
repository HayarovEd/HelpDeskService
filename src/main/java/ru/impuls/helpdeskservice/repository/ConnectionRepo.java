package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.Optional;
import java.util.UUID;

public interface ConnectionRepo extends JpaRepository<Connection, UUID> {

    @Query("SELECT c FROM Connection c " +
            "WHERE c.chat.id = :chatId " +
            "ORDER BY c.startDate DESC " +
            "LIMIT 1")
    Optional<Connection> findLastByChatId(@Param("chatId") UUID chatId);
}