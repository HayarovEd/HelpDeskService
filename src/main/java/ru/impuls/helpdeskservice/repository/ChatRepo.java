package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.impuls.helpdeskservice.entity.Chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatRepo extends JpaRepository<Chat, UUID>, JpaSpecificationExecutor<Chat> {

    @Query("SELECT c FROM Chat c WHERE c.active = true AND " +
           "(SELECT COUNT(m) FROM Message m WHERE m.connection.chat = c AND (m.isRead = false OR m.isRead IS NULL) AND m.isSupport = false) = 0 AND " +
           "(SELECT MAX(m.createdDate) FROM Message m WHERE m.connection.chat = c) < :thirtyMinutesAgo")
    List<Chat> findChatsToDeactivate(@Param("thirtyMinutesAgo") Instant thirtyMinutesAgo);
}
