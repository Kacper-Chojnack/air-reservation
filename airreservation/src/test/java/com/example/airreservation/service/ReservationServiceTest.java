package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationAdminEditDTO;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import com.example.airreservation.repository.TemporarySeatLockRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PassengerRepository passengerRepository;
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private SpringTemplateEngine templateEngine;
    @Mock
    private TemporarySeatLockRepository temporarySeatLockRepository; // Mock for update check

    @InjectMocks
    private ReservationService reservationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl = "http://localhost:8080"; // Provide default for tests

    private ReservationDTO reservationDTO;
    private Reservation reservation;
    private Passenger passenger;
    private Flight flight;
    private Airplane airplane;
    private ReservationAdminEditDTO reservationAdminEditDTO;

    @BeforeEach
    void setUp() {
        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("Anna");
        passenger.setSurname("Nowak");
        passenger.setEmail("anna@test.com");
        passenger.setPhoneNumber("987654321");
        airplane = new Airplane();
        airplane.setId(10L);
        airplane.setTotalSeats(100);
        flight = new Flight();
        flight.setId(5L);
        flight.setAirplane(airplane);
        flight.setDepartureDate(LocalDateTime.now().plusHours(5));
        flight.setFlightNumber("FR456");
        flight.setFlightDuration(Duration.ofHours(1));
        flight.setCompleted(false);

        reservationDTO = new ReservationDTO();
        reservationDTO.setFlightId(5L);
        reservationDTO.setPassengerId(1L);
        reservationDTO.setSeatNumber(25);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setFlight(flight);
        reservation.setPassenger(passenger);
        reservation.setSeatNumber(25);
        reservation.setReservationNumber("TEST123XYZ");
        reservation.setEmail("anna@test.com");
        reservation.setFlightNumber("FR456");
        reservation.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        reservationAdminEditDTO = new ReservationAdminEditDTO();
        reservationAdminEditDTO.setId(1L);
        reservationAdminEditDTO.setSeatNumber(30); // Admin changes seat
    }

    @Test
    void saveReservation_Success() {
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.ReservationToReservationDTO(any(Reservation.class))).thenReturn(reservationDTO);

        ReservationDTO savedDto = reservationService.saveReservation(reservationDTO);

        assertThat(savedDto).isNotNull();
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationMapper).ReservationToReservationDTO(any(Reservation.class));
    }

    @Test
    void saveReservation_PassengerNotFound_ThrowsException() {
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(new Reservation());
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSENGER_NOT_FOUND);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_FlightNotFound_ThrowsException() {
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(new Reservation());
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_FlightDeparted_ThrowsException() {
        flight.setDepartureDate(LocalDateTime.now().minusHours(1));
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(new Reservation());
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_AVAILABLE);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_FlightCompleted_ThrowsException() {
        flight.setCompleted(true);
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(new Reservation());
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_AVAILABLE);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_SeatInvalid_TooHigh_ThrowsException() {
        reservationDTO.setSeatNumber(101);
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INCORRECT_SEAT);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_SeatOccupied_ExistsCheck_ThrowsException() {
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void saveReservation_DataIntegrityViolation_ThrowsSeatOccupied() {
        String constraintViolationMessage = "uk_reservation_flight_seat";
        DataIntegrityViolationException dbException = new DataIntegrityViolationException(constraintViolationMessage);
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenThrow(dbException);


        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void saveReservation_OtherDataIntegrityViolation_Rethrows() {
        String otherErrorMessage = "some other constraint";
        DataIntegrityViolationException dbException = new DataIntegrityViolationException(otherErrorMessage);
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenThrow(dbException);

        assertThatThrownBy(() -> reservationService.saveReservation(reservationDTO))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isEqualTo(dbException);
        verify(reservationRepository).save(any(Reservation.class));
    }


    @Test
    void createReservationFromLock_Success() {
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));


        Reservation createdReservation = reservationService.createReservationFromLock(reservationDTO);

        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation.getSeatNumber()).isEqualTo(25);
        verify(reservationRepository).save(any(Reservation.class));
        verify(templateEngine).process(eq("emails/reservation-confirmation-email"), any());
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void createReservationFromLock_SeatOccupiedBeforeSave_ThrowsException() {
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservationFromLock(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void createReservationFromLock_DataIntegrityOnSave_ThrowsSeatOccupied() {
        String constraintViolationMessage = "uk_reservation_flight_seat";
        DataIntegrityViolationException dbException = new DataIntegrityViolationException(constraintViolationMessage);
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenThrow(dbException);

        assertThatThrownBy(() -> reservationService.createReservationFromLock(reservationDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
        verify(reservationRepository).save(any(Reservation.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void createReservationFromLock_MailExceptionOccurs_LogsErrorButReturnsReservation() {
        when(reservationRepository.existsByFlightIdAndSeatNumber(5L, 25)).thenReturn(false);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(5L)).thenReturn(Optional.of(flight));
        when(reservationMapper.ReservationDTOTOReservation(any(ReservationDTO.class))).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new MailException("Test Mail Error") {
        }).when(mailSender).send(any(MimeMessage.class));

        Reservation createdReservation = reservationService.createReservationFromLock(reservationDTO);

        assertThat(createdReservation).isNotNull();
        verify(reservationRepository).save(any(Reservation.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendReservationConfirmationEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> reservationService.sendReservationConfirmationEmail(reservation))
                .doesNotThrowAnyException();

        verify(templateEngine).process(eq("emails/reservation-confirmation-email"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendReservationConfirmationEmail_NullData_LogsError() {
        assertThatCode(() -> reservationService.sendReservationConfirmationEmail(null))
                .doesNotThrowAnyException();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void findAllReservationsPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> expectedPage = new PageImpl<>(Collections.singletonList(reservation));
        when(reservationRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Reservation> actualPage = reservationService.findAllReservationsPaginated(pageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(1);
        verify(reservationRepository).findAll(pageable);
    }

    @Test
    void deleteReservation_Success() {
        when(reservationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(1L);

        assertThatCode(() -> reservationService.deleteReservation(1L)).doesNotThrowAnyException();

        verify(reservationRepository).existsById(1L);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void deleteReservation_NotFound_ThrowsException() {
        when(reservationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.deleteReservation(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.RESERVATION_NOT_FOUND);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    void findReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        Optional<Reservation> result = reservationService.findReservationById(1L);
        assertThat(result).isPresent().contains(reservation);
        verify(reservationRepository).findById(1L);
    }

    @Test
    void findReservationById_NotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Reservation> result = reservationService.findReservationById(99L);
        assertThat(result).isNotPresent();
        verify(reservationRepository).findById(99L);
    }

    @Test
    void updateReservationByAdmin_SeatChanged_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsByFlightIdAndSeatNumberAndNotId(5L, 30, 1L)).thenReturn(false);
        when(temporarySeatLockRepository.existsByFlightIdAndSeatNumberAndExpiresAtAfter(eq(5L), eq(30), any(LocalDateTime.class))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        reservationService.updateReservationByAdmin(reservationAdminEditDTO);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getSeatNumber()).isEqualTo(30);
    }

    @Test
    void updateReservationByAdmin_SeatNotChanged_Success() {
        reservationAdminEditDTO.setSeatNumber(25); // Set to current seat
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.updateReservationByAdmin(reservationAdminEditDTO);

        verify(reservationRepository, never()).existsByFlightIdAndSeatNumberAndNotId(anyLong(), anyInt(), anyLong());
        verify(temporarySeatLockRepository, never()).existsByFlightIdAndSeatNumberAndExpiresAtAfter(anyLong(), anyInt(), any(LocalDateTime.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void updateReservationByAdmin_NewSeatOccupied_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsByFlightIdAndSeatNumberAndNotId(5L, 30, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.updateReservationByAdmin(reservationAdminEditDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_OCCUPIED);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void updateReservationByAdmin_NewSeatLocked_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.existsByFlightIdAndSeatNumberAndNotId(5L, 30, 1L)).thenReturn(false);
        when(temporarySeatLockRepository.existsByFlightIdAndSeatNumberAndExpiresAtAfter(eq(5L), eq(30), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.updateReservationByAdmin(reservationAdminEditDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.SEAT_LOCKED_BY_ANOTHER_USER);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void updateReservationByAdmin_ReservationNotFound_ThrowsException() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.updateReservationByAdmin(reservationAdminEditDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.RESERVATION_NOT_FOUND);
    }

}
