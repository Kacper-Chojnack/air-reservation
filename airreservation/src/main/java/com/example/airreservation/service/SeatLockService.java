package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.lock.TemporarySeatLock;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import com.example.airreservation.repository.TemporarySeatLockRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final Logger logger = LoggerFactory.getLogger(SeatLockService.class);
    public static final Duration LOCK_DURATION = Duration.ofMinutes(1);

    private final TemporarySeatLockRepository lockRepository;
    private final ReservationRepository reservationRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationService reservationService;

    @Transactional
    public TemporarySeatLock acquireLock(Long flightId, Integer seatNumber, Long passengerId) {
        LocalDateTime now = LocalDateTime.now();
        Flight flight = flightRepository.findById(flightId).orElseThrow(ErrorType.FLIGHT_NOT_FOUND::create);
        Passenger passenger = passengerRepository.findById(passengerId).orElseThrow(ErrorType.PASSENGER_NOT_FOUND::create);


        if (flight.isDeparted() || flight.isCompleted()) {
            throw ErrorType.FLIGHT_NOT_AVAILABLE.create();
        }
        if (seatNumber == null || seatNumber < 1 || seatNumber > flight.getAirplane().getTotalSeats()) {
            throw ErrorType.INCORRECT_SEAT.create(flight.getAirplane().getTotalSeats());
        }
        if (reservationRepository.existsByFlightIdAndSeatNumber(flightId, seatNumber)) {
            throw ErrorType.SEAT_OCCUPIED.create(seatNumber);
        }


        Optional<TemporarySeatLock> existingLockOpt = lockRepository.findByFlightIdAndSeatNumber(flightId, seatNumber);
        if (existingLockOpt.isPresent()) {
            TemporarySeatLock existingLock = existingLockOpt.get();

            if (existingLock.getExpiresAt().isBefore(now)) {
                logger.info("Znaleziono i usunięto wygasłą blokadę (ID: {}) dla miejsca {} lotu {} podczas próby nowej blokady przez pasażera {}.",
                        existingLock.getId(), seatNumber, flightId, passengerId);
                lockRepository.delete(existingLock);
                lockRepository.flush();
            } else {

                logger.warn("Pasażer {} próbował zablokować aktywnie zablokowane miejsce {} dla lotu {}", passengerId, seatNumber, flightId);

                throw ErrorType.SEAT_LOCKED_BY_ANOTHER_USER.create(seatNumber);
            }
        }


        LocalDateTime expiryTime = now.plus(LOCK_DURATION);
        TemporarySeatLock newLock = new TemporarySeatLock(flight, seatNumber, passenger, expiryTime);
        try {
            TemporarySeatLock savedLock = lockRepository.saveAndFlush(newLock);
            logger.info("Utworzono nową blokadę (ID: {}) dla miejsca {} lotu {} przez pasażera {}, wygasa: {}",
                    savedLock.getId(), seatNumber, flightId, passengerId, expiryTime);
            return savedLock;
        } catch (DataIntegrityViolationException e) {


            logger.warn("Konflikt (DataIntegrityViolation) podczas próby utworzenia nowej blokady dla miejsca {} lotu {} przez pasażera {}", seatNumber, flightId, passengerId, e);
            throw ErrorType.SEAT_LOCKED_OR_RESERVED.create(seatNumber);
        }
    }

    @Transactional
    public void releaseLock(Long lockId) {
        lockRepository.deleteById(lockId);
        logger.info("Zwolniono blokadę o ID: {}", lockId);
    }


    @Transactional
    public void releaseLock(Long passengerId, Long flightId, Integer seatNumber) {
        lockRepository.findByPassengerIdAndFlightIdAndSeatNumber(passengerId, flightId, seatNumber)
                .ifPresent(lock -> {
                    lockRepository.delete(lock);
                    logger.info("Zwolniono blokadę dla pasażera {}, lotu {}, miejsce {}", passengerId, flightId, seatNumber);
                });
    }

    @Transactional
    public Reservation finalizeReservationWithPassword(Long lockId, String rawPassword) {
        LocalDateTime now = LocalDateTime.now();
        TemporarySeatLock lock = lockRepository.findById(lockId)
                .orElseThrow(() -> ErrorType.LOCK_NOT_FOUND.create());

        if (lock.getExpiresAt().isBefore(now)) {
            lockRepository.delete(lock);
            logger.warn("Próba finalizacji rezerwacji dla wygasłej blokady ID: {}", lockId);
            throw ErrorType.LOCK_EXPIRED.create();
        }

        Passenger passenger = lock.getPassenger();
        if (!passwordEncoder.matches(rawPassword, passenger.getPassword())) {
            logger.warn("Nieudana próba finalizacji rezerwacji - błędne hasło dla pasażera {}", passenger.getEmail());
            throw ErrorType.INVALID_PASSWORD_FOR_RESERVATION.create();
        }

        try {
            ReservationDTO dto = new ReservationDTO();
            dto.setFlightId(lock.getFlight().getId());
            dto.setPassengerId(passenger.getId());
            dto.setSeatNumber(lock.getSeatNumber());


            Reservation finalReservation = reservationService.createReservationFromLock(dto);


            lockRepository.delete(lock);
            logger.info("Pomyślnie sfinalizowano rezerwację i zwolniono blokadę ID: {}", lockId);
            return finalReservation;

        } catch (BusinessException e) {
            logger.error("Błąd biznesowy podczas finalizacji rezerwacji (po weryfikacji hasła) dla blokady ID: {}", lockId, e);

            lockRepository.delete(lock);
            throw e;
        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas finalizacji rezerwacji dla blokady ID: {}", lockId, e);

            lockRepository.delete(lock);
            throw new RuntimeException("Nieoczekiwany błąd podczas finalizacji rezerwacji.", e);
        }
    }
}