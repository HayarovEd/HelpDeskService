package ru.impuls.helpdeskservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.impuls.helpdeskservice.auth.entity.Role;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.auth.repository.RoleRepo;
import ru.impuls.helpdeskservice.auth.repository.UserRepo;

@Component
@RequiredArgsConstructor
public class AdminInitConfig implements CommandLineRunner {

    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        var adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("ADMIN")
                                .build()
                ));

        if (!userRepository.existsByUsername("Admin")) {
            var admin = User.builder()
                    .name("Admin")
                    .username("Admin")
                    .email("admin@mail.ru")
                    .password(passwordEncoder.encode(adminPassword))
                    .disabled(false)
                    .role(adminRole)
                    .build();

            userRepository.save(admin);
        }
    }
}