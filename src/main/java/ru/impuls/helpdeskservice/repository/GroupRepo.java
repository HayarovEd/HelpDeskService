package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Group;

import java.util.UUID;

public interface GroupRepo extends JpaRepository<Group, UUID> {
    boolean existsByName(String name);
}
