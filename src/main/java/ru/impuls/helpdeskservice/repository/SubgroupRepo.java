package ru.impuls.helpdeskservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.impuls.helpdeskservice.entity.Group;
import ru.impuls.helpdeskservice.entity.Subgroup;

import java.util.UUID;

public interface SubgroupRepo extends JpaRepository<Subgroup, UUID> {
    boolean existsByNameAndGroupId(String name, UUID groupId);
}
