package com.example.airreservation.scheduler;

import com.example.airreservation.repository.TemporarySeatLockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatLockCleanupSchedulerTest {

    @Mock
    private TemporarySeatLockRepository lockRepository;

    @InjectMocks
    private SeatLockCleanupScheduler seatLockCleanupScheduler;

    @Test
    void cleanupExpiredSeatLocks_shouldCallRepositoryDeleteMethod() {
        when(lockRepository.deleteExpiredLocks(any(LocalDateTime.class))).thenReturn(5);

        seatLockCleanupScheduler.cleanupExpiredSeatLocks();

        verify(lockRepository).deleteExpiredLocks(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredSeatLocks_shouldHandleRepositoryException() {
        when(lockRepository.deleteExpiredLocks(any(LocalDateTime.class))).thenThrow(new RuntimeException("DB error"));

        assertThatCode(() -> seatLockCleanupScheduler.cleanupExpiredSeatLocks())
                .doesNotThrowAnyException();

        verify(lockRepository).deleteExpiredLocks(any(LocalDateTime.class));
    }
}
