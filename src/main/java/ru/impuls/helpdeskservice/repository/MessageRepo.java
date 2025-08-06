package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Message;

import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message, UUID> {
}
