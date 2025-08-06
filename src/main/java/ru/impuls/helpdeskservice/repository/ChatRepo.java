package ru.impuls.helpdeskservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.impuls.helpdeskservice.entity.Chat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepo extends JpaRepository<Chat, UUID> {
    @Query("SELECT DISTINCT c FROM Chat c " +
            "LEFT JOIN c.connections conn " +
            "LEFT JOIN conn.messages m " +
            "WHERE (:groupId IS NULL OR c.subgroup.group.id = :groupId) " +
            "AND (:subgroupId IS NULL OR c.subgroup.id = :subgroupId) " +
            "AND (:to IS NULL OR m.createdDate < CAST(:to AS timestamp)) " +
            "AND (:search IS NULL OR c.name LIKE %:search% OR c.email LIKE %:search% OR c.phone LIKE %:search%) " +
            "AND (:isOpen IS NULL OR c.isOpen = :isOpen) " +
            "AND (:isWork IS NULL OR c.isWork = :isWork)")
    Page<Chat> findChatsWithFilters(
            @Param("groupId") UUID groupId,
            @Param("subgroupId") UUID subgroupId,
            @Param("to") LocalDateTime to,
            @Param("search") String search,
            @Param("isOpen") Boolean isOpen,
            @Param("isWork") Boolean isWork,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT c FROM Chat c
        LEFT JOIN FETCH c.connections conn
        LEFT JOIN FETCH conn.messages m
        WHERE c.id = :chatId
    """)
    Optional<Chat> findWithConnectionsAndMessages(@Param("chatId") UUID chatId);
}
