package ru.impuls.helpdeskservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.impuls.helpdeskservice.auth.repository.TokenRepo;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {
    private final TokenRepo tokenRepo;

    @Scheduled(cron = "0 0 */1 * * *") // Каждый час
    @Transactional
    public void cleanupExpiredTokens() {
        var countDeleted = tokenRepo.deleteExpiredTokens(Instant.now());
        log.info("Deleted {} expired tokens", countDeleted);
    }
}
