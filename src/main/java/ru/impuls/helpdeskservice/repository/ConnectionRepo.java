package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Connection;

import java.util.UUID;

public interface ConnectionRepo extends JpaRepository<Connection, UUID> {
}