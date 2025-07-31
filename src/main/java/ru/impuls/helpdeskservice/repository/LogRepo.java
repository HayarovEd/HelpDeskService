package ru.impuls.helpdeskservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Log;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LogRepo extends JpaRepository<Log, UUID> {
    List<Log> findAllByCreatedDateBefore(Instant to, Pageable pageable);
}
