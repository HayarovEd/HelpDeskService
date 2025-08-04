package ru.impuls.helpdeskservice.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Log;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogRepo extends JpaRepository<Log, UUID> {
    Page<Log> findAllByCreatedDateBefore(Instant to, Pageable pageable);

    long countByCreatedDateBefore(Instant to);

    Optional<Log> findFirstByOrderByCreatedDateAsc();
}
