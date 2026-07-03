package ru.impuls.helpdeskservice.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.impuls.helpdeskservice.auth.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String name);

    boolean existsByUsername(String email);
    List<User> findByUsernameIn(Collection<String> usernames);

    @Query("SELECT u FROM User u WHERE u.role.name IN :roleNames")
    List<User> findByRoleNameIn(Collection<String> roleNames);
}
