package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationAdminEditDTO;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String appBaseUrl;


    private String generateReservationNumber(Flight flight) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public ReservationDTO saveReservation(ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.ReservationDTOTOReservation(reservationDTO);

        Passenger passenger = passengerRepository.findById(reservationDTO.getPassengerId())
                .orElseThrow(ErrorType.PASSENGER_NOT_FOUND::create);

        Flight flight = flightRepository.findById(reservationDTO.getFlightId())
                .orElseThrow(ErrorType.FLIGHT_NOT_FOUND::create);

        if (flight.isDeparted() || flight.isCompleted()) {
            throw ErrorType.FLIGHT_NOT_AVAILABLE.create();
        }

        populateReservationFields(reservation, passenger, flight, reservationDTO);

        int totalSeats = flight.getAirplane().getTotalSeats();
        if (reservation.getSeatNumber() == null || reservation.getSeatNumber() > totalSeats || reservation.getSeatNumber() < 1) {
            throw ErrorType.INCORRECT_SEAT.create(totalSeats);
        }

        if (reservationRepository.existsByFlightIdAndSeatNumber(
                reservationDTO.getFlightId(),
                reservationDTO.getSeatNumber()
        )) {
            throw ErrorType.SEAT_OCCUPIED.create(reservationDTO.getSeatNumber());
        }

        try {
            Reservation savedReservation = reservationRepository.save(reservation);
            return reservationMapper.ReservationToReservationDTO(savedReservation);
        } catch (DataIntegrityViolationException e) {

            if (e.getMessage() != null && e.getMessage().contains("uk_reservation_flight_seat")) {
                throw ErrorType.SEAT_OCCUPIED.create(reservationDTO.getSeatNumber());
            } else {
                throw e;
            }
        }
    }

    private void populateReservationFields(Reservation reservation, Passenger passenger, Flight flight, ReservationDTO reservationDTO) {
        reservation.setDeparted(false);
        reservation.setEmail(passenger.getEmail());
        reservation.setFlight(flight);
        reservation.setFullName(passenger.getName() + " " + passenger.getSurname());
        reservation.setPhoneNumber(passenger.getPhoneNumber());
        reservation.setReservationNumber(generateReservationNumber(flight));
        reservation.setFlightNumber(flight.getFlightNumber());
        reservation.setPassenger(passenger);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setSeatNumber(reservationDTO.getSeatNumber());
    }

    @Transactional
    public Reservation createReservationFromLock(ReservationDTO reservationDTO) {
        if (reservationRepository.existsByFlightIdAndSeatNumber(reservationDTO.getFlightId(), reservationDTO.getSeatNumber())) {
            logger.warn("Miejsce {} dla lotu {} zostało zajęte tuż przed finalizacją!", reservationDTO.getSeatNumber(), reservationDTO.getFlightId());
            throw ErrorType.SEAT_OCCUPIED.create(reservationDTO.getSeatNumber());
        }
        Passenger passenger = passengerRepository.findById(reservationDTO.getPassengerId())
                .orElseThrow(() -> new IllegalStateException("Pasażer nie znaleziony podczas finalizacji, ID: " + reservationDTO.getPassengerId()));
        Flight flight = flightRepository.findById(reservationDTO.getFlightId())
                .orElseThrow(() -> new IllegalStateException("Lot nie znaleziony podczas finalizacji, ID: " + reservationDTO.getFlightId()));

        Reservation reservation = reservationMapper.ReservationDTOTOReservation(reservationDTO);
        populateReservationFields(reservation, passenger, flight, reservationDTO);

        try {
            Reservation savedReservation = reservationRepository.save(reservation);
            logger.info("Utworzono trwałą rezerwację: {}", savedReservation.getReservationNumber());


            try {
                sendReservationConfirmationEmail(savedReservation);
            } catch (Exception e) {

                logger.error("Nie udało się wysłać emaila potwierdzającego rezerwację {} dla {}: {}",
                        savedReservation.getReservationNumber(), savedReservation.getEmail(), e.getMessage());
            }


            return savedReservation;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_reservation_flight_seat")) {
                logger.warn("Konflikt zapisu rezerwacji (miejsce zajęte?) dla miejsca {} lotu {}", reservationDTO.getSeatNumber(), reservationDTO.getFlightId(), e);
                throw ErrorType.SEAT_OCCUPIED.create(reservationDTO.getSeatNumber());
            } else {
                logger.error("Nieoczekiwany błąd DataIntegrityViolationException podczas finalizacji", e);
                throw e;
            }
        }
    }


    @Async
    public void sendReservationConfirmationEmail(Reservation reservation) {
        if (reservation == null || reservation.getPassenger() == null || reservation.getFlight() == null) {
            logger.error("Nie można wysłać emaila potwierdzającego - brakujące dane w obiekcie Reservation (ID: {})", reservation != null ? reservation.getId() : "null");
            return;
        }

        String recipientAddress = reservation.getEmail();
        String subject = "Potwierdzenie Twojej Rezerwacji Lotu - AirReservation #" + reservation.getReservationNumber();
        String profileUrl = appBaseUrl + "/profile";


        Context context = new Context();
        context.setVariable("passengerName", reservation.getPassenger().getName());
        context.setVariable("reservationNumber", reservation.getReservationNumber());
        context.setVariable("flightNumber", reservation.getFlightNumber());
        context.setVariable("departureAirport", reservation.getFlight().getDepartureAirport() != null ? reservation.getFlight().getDepartureAirport().getName() : "Brak danych");
        context.setVariable("arrivalAirport", reservation.getFlight().getArrivalAirport() != null ? reservation.getFlight().getArrivalAirport().getName() : "Brak danych");
        context.setVariable("departureDateTime", reservation.getFlight().getDepartureDate());
        context.setVariable("seatNumber", reservation.getSeatNumber());
        context.setVariable("profileUrl", profileUrl);

        try {

            String htmlContent = templateEngine.process("emails/reservation-confirmation-email", context);


            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Wysłano email potwierdzający rezerwację {} do: {}", reservation.getReservationNumber(), recipientAddress);

        } catch (MessagingException e) {
            logger.error("Błąd podczas tworzenia wiadomości MIME dla potwierdzenia rezerwacji {} ({}): {}", reservation.getReservationNumber(), recipientAddress, e.getMessage());
        } catch (MailException e) {
            logger.error("Nie udało się wysłać maila potwierdzającego rezerwację (MailException) {} do {}: {}", reservation.getReservationNumber(), recipientAddress, e.getMessage());
        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas wysyłania emaila potwierdzającego rezerwację {} do {}: {}", reservation.getReservationNumber(), recipientAddress, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Reservation> findAllReservationsPaginated(Pageable pageable) {

        return reservationRepository.findAll(pageable);
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw ErrorType.RESERVATION_NOT_FOUND.create(id);
        }

        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> findReservationById(Long id) {
        return reservationRepository.findById(id);
    }


    @Transactional
    public void updateReservationByAdmin(ReservationAdminEditDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getId())
                .orElseThrow(() -> ErrorType.RESERVATION_NOT_FOUND.create(dto.getId()));

        Integer newSeatNumber = dto.getSeatNumber();
        Integer currentSeatNumber = reservation.getSeatNumber();
        Long flightId = reservation.getFlight().getId();


        if (!newSeatNumber.equals(currentSeatNumber)) {

            if (reservationRepository.existsByFlightIdAndSeatNumberAndNotId(flightId, newSeatNumber, reservation.getId())) {
                throw ErrorType.SEAT_OCCUPIED.create(newSeatNumber);
            }


            reservation.setSeatNumber(newSeatNumber);
            reservationRepository.save(reservation);
            logger.info("Admin zaktualizował miejsce dla rezerwacji ID {} na {}", dto.getId(), newSeatNumber);
        } else {
            logger.info("Miejsce dla rezerwacji ID {} nie zostało zmienione.", dto.getId());
        }
    }
}