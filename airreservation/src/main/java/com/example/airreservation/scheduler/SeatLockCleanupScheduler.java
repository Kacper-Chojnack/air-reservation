package com.example.airreservation.scheduler;

import com.example.airreservation.repository.TemporarySeatLockRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SeatLockCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SeatLockCleanupScheduler.class);
    private final TemporarySeatLockRepository lockRepository;


    @Scheduled(fixedRate = 900_000)
    @Transactional
    public void cleanupExpiredSeatLocks() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("SCHEDULER (Garbage Collection): Uruchamianie czyszczenia blokad wygasłych przed {}", now);
        try {
            int deletedCount = lockRepository.deleteExpiredLocks(now);
            if (deletedCount > 0) {
                logger.info("SCHEDULER (Garbage Collection): Usunięto {} porzuconych/wygasłych blokad miejsc.", deletedCount);
            } else {
                logger.debug("SCHEDULER (Garbage Collection): Nie znaleziono porzuconych/wygasłych blokad do usunięcia.");
            }
        } catch (Exception e) {
            logger.error("SCHEDULER (Garbage Collection): Błąd podczas czyszczenia wygasłych blokad!", e);
        }
    }
}