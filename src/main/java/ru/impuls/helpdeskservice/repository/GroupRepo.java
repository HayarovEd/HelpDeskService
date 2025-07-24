package ru.impuls.helpdeskservice.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Group;

import java.util.List;
import java.util.UUID;

public interface GroupRepo extends JpaRepository<Group, UUID> {
    @NotNull
    @EntityGraph(attributePaths = "subgroups")
    List<Group> findAll();
    boolean existsByName(String name);
}
