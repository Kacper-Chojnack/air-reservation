package com.example.airreservation.scheduler;

import com.example.airreservation.repository.TemporarySeatLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class FlightStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FlightStatusScheduler.class);

    private TemporarySeatLockRepository temporarySeatLockRepository;

    public FlightStatusScheduler(TemporarySeatLockRepository temporarySeatLockRepository) {
        this.temporarySeatLockRepository = temporarySeatLockRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cleanupExpiredSeatLocks() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("SCHEDULER: Uruchamianie czyszczenia blokad wygasłych przed {}", now);
        try {
            int deletedCount = temporarySeatLockRepository.deleteExpiredLocks(now);
            if (deletedCount > 0) {
                logger.info("SCHEDULER: Usunięto {} wygasłych blokad miejsc.", deletedCount);
            } else {
                logger.debug("SCHEDULER: Nie znaleziono wygasłych blokad miejsc do usunięcia.");
            }
        } catch (Exception e) {
            logger.error("SCHEDULER: Błąd podczas czyszczenia wygasłych blokad!", e);
        }
    }
}