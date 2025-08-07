package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.impuls.helpdeskservice.entity.Chat;

import java.util.UUID;

public interface ChatRepo extends
        JpaRepository<Chat, UUID>,
        JpaSpecificationExecutor<Chat> {
}
