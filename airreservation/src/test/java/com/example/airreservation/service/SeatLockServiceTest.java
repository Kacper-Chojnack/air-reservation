package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.lock.TemporarySeatLock;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import com.example.airreservation.repository.TemporarySeatLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockServiceTest {

    @Mock
    private TemporarySeatLockRepository lockRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private SeatLockService seatLockService;

    private Flight flight;
    private Passenger passenger;
    private TemporarySeatLock lock;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Airplane airplane = new Airplane("TestPlane", 100);
        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setEmail("test@user.com");
        passenger.setPassword("hashedPassword");
        flight = new Flight();
        flight.setId(10L);
        flight.setAirplane(airplane);
        flight.setDepartureDate(LocalDateTime.now().plusDays(1));
        flight.setCompleted(false);
        lock = new TemporarySeatLock(flight, 25, passenger, LocalDateTime.now().plus(SeatLockService.LOCK_DURATION));
        lock.setId(1L);
        reservation = new Reservation();
        reservation.setId(5L);
    }

    @Test
    void acquireLock_Success() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(reservationRepository.existsByFlightIdAndSeatNumber(10L, 25)).thenReturn(false);
        when(lockRepository.findByFlightIdAndSeatNumber(10L, 25)).thenReturn(Optional.empty());
        when(lockRepository.saveAndFlush(any(TemporarySeatLock.class))).thenReturn(lock);

        TemporarySeatLock acquiredLock = seatLockService.acquireLock(10L, 25, 1L);

        assertThat(acquiredLock).isNotNull();
        assertThat(acquiredLock.getSeatNumber()).isEqualTo(25);
        assertThat(acquiredLock.getPassenger()).isEqualTo(passenger);
        assertThat(acquiredLock.getFlight()).isEqualTo(flight);
        assertThat(acquiredLock.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(lockRepository).saveAndFlush(any(TemporarySeatLock.class));
    }

    @Test
    void acquireLock_ClearsExpiredLock_Success() {
        TemporarySeatLock expiredLock = new TemporarySeatLock(flight, 25, new Passenger(), LocalDateTime.now().minusMinutes(5));
        expiredLock.setId(99L);

        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(reservationRepository.existsByFlightIdAndSeatNumber(10L, 25)).thenReturn(false);
        when(lockRepository.findByFlightIdAndSeatNumber(10L, 25)).thenReturn(Optional.of(expiredLock));
        doNothing().when(lockRepository).delete(expiredLock);
        doNothing().when(lockRepository).flush();
        when(lockRepository.saveAndFlush(any(TemporarySeatLock.class))).thenReturn(lock);

        TemporarySeatLock acquiredLock = seatLockService.acquireLock(10L, 25, 1L);

        assertThat(acquiredLock).isNotNull();
        verify(lockRepository).delete(expiredLock);
        verify(lockRepository).flush();
        verify(lockRepository).saveAndFlush(any(TemporarySeatLock.class));
    }


    @Test
    void acquireLock_FlightNotFound_ThrowsException() {
        when(flightRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);
    }

    @Test
    void acquireLock_PassengerNotFound_ThrowsException() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSENGER_NOT_FOUND);
    }

    @Test
    void acquireLock_FlightNotAvailable_Departed_ThrowsException() {
        flight.setDepartureDate(LocalDateTime.now().minusHours(1));
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_AVAILABLE);
    }

    @Test
    void acquireLock_IncorrectSeat_ThrowsException() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 101, 1L)) // Seat 101 > 100
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INCORRECT_SEAT);
    }

    @Test
    void acquireLock_SeatAlreadyReserved_ThrowsException() {
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(reservationRepository.existsByFlightIdAndSeatNumber(10L, 25)).thenReturn(true);

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
    }

    @Test
    void acquireLock_SeatActivelyLocked_ThrowsException() {
        TemporarySeatLock activeLock = new TemporarySeatLock(flight, 25, new Passenger(), LocalDateTime.now().plusMinutes(30));
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(reservationRepository.existsByFlightIdAndSeatNumber(10L, 25)).thenReturn(false);
        when(lockRepository.findByFlightIdAndSeatNumber(10L, 25)).thenReturn(Optional.of(activeLock));

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_LOCKED_BY_ANOTHER_USER);
        verify(lockRepository, never()).delete(any());
        verify(lockRepository, never()).saveAndFlush(any());
    }

    @Test
    void acquireLock_DataIntegrityOnSave_ThrowsException() {
        DataIntegrityViolationException dbException = new DataIntegrityViolationException("Constraint violation");
        when(flightRepository.findById(10L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(reservationRepository.existsByFlightIdAndSeatNumber(10L, 25)).thenReturn(false);
        when(lockRepository.findByFlightIdAndSeatNumber(10L, 25)).thenReturn(Optional.empty());
        when(lockRepository.saveAndFlush(any(TemporarySeatLock.class))).thenThrow(dbException);

        assertThatThrownBy(() -> seatLockService.acquireLock(10L, 25, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_LOCKED_OR_RESERVED);
    }

    @Test
    void releaseLockById_Success() {
        doNothing().when(lockRepository).deleteById(1L);
        assertThatCode(() -> seatLockService.releaseLock(1L)).doesNotThrowAnyException();
        verify(lockRepository).deleteById(1L);
    }

    @Test
    void releaseLockByDetails_Success() {
        when(lockRepository.findByPassengerIdAndFlightIdAndSeatNumber(1L, 10L, 25)).thenReturn(Optional.of(lock));
        doNothing().when(lockRepository).delete(lock);

        seatLockService.releaseLock(1L, 10L, 25);

        verify(lockRepository).findByPassengerIdAndFlightIdAndSeatNumber(1L, 10L, 25);
        verify(lockRepository).delete(lock);
    }

    @Test
    void releaseLockByDetails_LockNotFound() {
        when(lockRepository.findByPassengerIdAndFlightIdAndSeatNumber(1L, 10L, 25)).thenReturn(Optional.empty());

        seatLockService.releaseLock(1L, 10L, 25);

        verify(lockRepository).findByPassengerIdAndFlightIdAndSeatNumber(1L, 10L, 25);
        verify(lockRepository, never()).delete(any());
    }

    @Test
    void finalizeReservationWithPassword_Success() {
        String rawPassword = "password123";
        when(lockRepository.findById(1L)).thenReturn(Optional.of(lock));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(reservationService.createReservationFromLock(any(ReservationDTO.class))).thenReturn(reservation);
        doNothing().when(lockRepository).delete(lock);

        Reservation finalReservation = seatLockService.finalizeReservationWithPassword(1L, rawPassword);

        assertThat(finalReservation).isEqualTo(reservation);
        verify(passwordEncoder).matches(rawPassword, "hashedPassword");
        verify(reservationService).createReservationFromLock(any(ReservationDTO.class));
        verify(lockRepository).delete(lock);
    }

    @Test
    void finalizeReservationWithPassword_LockNotFound_ThrowsException() {
        when(lockRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatLockService.finalizeReservationWithPassword(1L, "password"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.LOCK_NOT_FOUND);
    }

    @Test
    void finalizeReservationWithPassword_LockExpired_ThrowsException() {
        lock.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(lockRepository.findById(1L)).thenReturn(Optional.of(lock));
        doNothing().when(lockRepository).delete(lock);

        assertThatThrownBy(() -> seatLockService.finalizeReservationWithPassword(1L, "password"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.LOCK_EXPIRED);
        verify(lockRepository).delete(lock);
    }

    @Test
    void finalizeReservationWithPassword_InvalidPassword_ThrowsException() {
        String rawPassword = "wrongPassword";
        when(lockRepository.findById(1L)).thenReturn(Optional.of(lock));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> seatLockService.finalizeReservationWithPassword(1L, rawPassword))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_PASSWORD_FOR_RESERVATION);
        verify(lockRepository, never()).delete(any());
        verify(reservationService, never()).createReservationFromLock(any());
    }

    @Test
    void finalizeReservationWithPassword_ReservationServiceThrowsBusinessException_DeletesLockAndRethrows() {
        String rawPassword = "password123";
        BusinessException reservationException = ErrorType.SEAT_OCCUPIED.create(25);
        when(lockRepository.findById(1L)).thenReturn(Optional.of(lock));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(reservationService.createReservationFromLock(any(ReservationDTO.class))).thenThrow(reservationException);
        doNothing().when(lockRepository).delete(lock);

        assertThatThrownBy(() -> seatLockService.finalizeReservationWithPassword(1L, rawPassword))
                .isInstanceOf(BusinessException.class)
                .isEqualTo(reservationException);
        verify(lockRepository).delete(lock);
    }

    @Test
    void finalizeReservationWithPassword_ReservationServiceThrowsOtherException_DeletesLockAndThrowsRuntimeException() {
        String rawPassword = "password123";
        RuntimeException otherException = new RuntimeException("Unexpected DB error");
        when(lockRepository.findById(1L)).thenReturn(Optional.of(lock));
        when(passwordEncoder.matches(rawPassword, "hashedPassword")).thenReturn(true);
        when(reservationService.createReservationFromLock(any(ReservationDTO.class))).thenThrow(otherException);
        doNothing().when(lockRepository).delete(lock);

        assertThatThrownBy(() -> seatLockService.finalizeReservationWithPassword(1L, rawPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nieoczekiwany błąd podczas finalizacji rezerwacji.")
                .hasCause(otherException);
        verify(lockRepository).delete(lock);
    }
}
