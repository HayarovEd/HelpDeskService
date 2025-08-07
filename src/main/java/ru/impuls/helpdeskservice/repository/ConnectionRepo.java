package ru.impuls.helpdeskservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.UUID;

public interface ConnectionRepo extends JpaRepository<Connection, UUID> {
    Page<Connection> findByChatId(UUID id, Pageable connectionPageable);
}