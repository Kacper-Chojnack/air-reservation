package com.example.airreservation.service;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private FlightService flightService; // używany tylko do konstrukcji

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Pomocnicza metoda – tworzy poprawny obiekt Flight z wymaganymi polami
    private Flight createValidFlight(Long flightId, int totalSeats) {
        Flight flight = new Flight();
        flight.setId(flightId);
        flight.setFlightNumber("FL" + flightId);
        flight.setDepartureDate(LocalDateTime.now().plusDays(1));
        flight.setFlightDuration(Duration.ofHours(2).plusMinutes(30));
        // Jeśli klasa Flight nie posiada pola isDeparted, załóżmy, że mamy metody isDeparted() i isCompleted(),
        // które obliczają stan na podstawie daty; dla testów użyjemy spy i stubujemy je:
        Flight flightSpy = spy(flight);
        doReturn(false).when(flightSpy).isDeparted();
        doReturn(false).when(flightSpy).isCompleted();

        Airplane airplane = new Airplane();
        airplane.setId(30L);
        airplane.setTotalSeats(totalSeats);
        flightSpy.setAirplane(airplane);
        return flightSpy;
    }

    // --- Test zapisu rezerwacji – scenariusz pozytywny ---
    @Test
    void testSaveReservation_Success() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        dto.setSeatNumber(5);

        // Upewniamy się, że mapper ustawi pole seatNumber (symulujemy poprawne mapowanie)
        when(reservationMapper.ReservationDTOTOReservation(dto))
                .thenAnswer(invocation -> {
                    Reservation r = new Reservation();
                    r.setSeatNumber(dto.getSeatNumber());
                    return r;
                });

        Passenger passenger = new Passenger();
        passenger.setId(10L);
        passenger.setEmail("john@example.com");
        passenger.setName("John");
        passenger.setSurname("Doe");
        passenger.setPhoneNumber("123456789");

        Flight flight = createValidFlight(20L, 100);

        when(passengerRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(20L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightIdAndSeatNumber(20L, 5)).thenReturn(false);

        Reservation savedReservation = new Reservation();
        savedReservation.setSeatNumber(5);
        savedReservation.setReservationNumber("ABC123DEF456");
        savedReservation.setFlightNumber("FL20");
        savedReservation.setCreatedAt(LocalDateTime.now());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.ReservationToReservationDTO(savedReservation)).thenReturn(dto);

        ReservationDTO result = reservationService.saveReservation(dto);

        assertNotNull(result);
        assertEquals(5, result.getSeatNumber());
        verify(passengerRepository, times(1)).findById(10L);
        verify(flightRepository, times(1)).findById(20L);
        verify(reservationRepository, times(1)).existsByFlightIdAndSeatNumber(20L, 5);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    // --- Test: Pasażer nie znaleziony ---
    @Test
    void testSaveReservation_PassengerNotFound() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        dto.setSeatNumber(5);

        when(passengerRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.saveReservation(dto));
        verify(passengerRepository, times(1)).findById(10L);
        verify(flightRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }

    // --- Test: Lot nie znaleziony ---
    @Test
    void testSaveReservation_FlightNotFound() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        dto.setSeatNumber(5);

        Passenger passenger = new Passenger();
        when(passengerRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservationService.saveReservation(dto));
        verify(flightRepository, times(1)).findById(20L);
        verify(reservationRepository, never()).save(any());
    }

    // --- Test: Lot niedostępny (lot wystartował lub ukończony) ---
    @Test
    void testSaveReservation_FlightNotAvailable() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        dto.setSeatNumber(5);

        Passenger passenger = new Passenger();
        when(passengerRepository.findById(10L)).thenReturn(Optional.of(passenger));

        Flight flight = createValidFlight(20L, 100);
        doReturn(true).when(flight).isDeparted(); // lot jest niedostępny
        when(flightRepository.findById(20L)).thenReturn(Optional.of(flight));

        assertThrows(RuntimeException.class, () -> reservationService.saveReservation(dto));
        verify(flightRepository, times(1)).findById(20L);
        verify(reservationRepository, never()).save(any());
    }

    // --- Test: Niepoprawny numer siedzenia (poza zakresem) ---
    @Test
    void testSaveReservation_IncorrectSeat() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        // Numer siedzenia 150 przy samolocie z 100 miejscami
        dto.setSeatNumber(150);

        Passenger passenger = new Passenger();
        passenger.setEmail("test@example.com");
        passenger.setName("Test");
        passenger.setSurname("User");
        passenger.setPhoneNumber("111222333");

        Flight flight = createValidFlight(20L, 100);
        when(passengerRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(20L)).thenReturn(Optional.of(flight));

        assertThrows(RuntimeException.class, () -> reservationService.saveReservation(dto));
        verify(reservationRepository, never()).save(any());
    }

    // --- Test: Miejsce zajęte ---
    @Test
    void testSaveReservation_SeatOccupied() {
        ReservationDTO dto = new ReservationDTO();
        dto.setPassengerId(10L);
        dto.setFlightId(20L);
        dto.setSeatNumber(5);

        Passenger passenger = new Passenger();
        passenger.setEmail("test@example.com");
        passenger.setName("Test");
        passenger.setSurname("User");
        passenger.setPhoneNumber("111222333");

        Flight flight = createValidFlight(20L, 100);
        when(passengerRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(flightRepository.findById(20L)).thenReturn(Optional.of(flight));
        // Symulujemy, że miejsce 5 już jest zajęte
        when(reservationRepository.existsByFlightIdAndSeatNumber(20L, 5)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> reservationService.saveReservation(dto));
        verify(reservationRepository, times(1)).existsByFlightIdAndSeatNumber(20L, 5);
        verify(reservationRepository, never()).save(any());
    }

    // --- Test metody getAvailableSeats (scenariusz pozytywny) ---
    @Test
    void testGetAvailableSeats_Success() {
        Long flightId = 20L;
        Flight flight = createValidFlight(flightId, 10); // Samolot o 10 miejscach

        // Symulujemy, że miejsca 1, 3 i 5 są zarezerwowane
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(reservationRepository.findSeatNumberByFlightIdAndActiveTrue(flightId)).thenReturn(List.of(1, 3, 5));

        List<Integer> availableSeats = reservationService.getAvailableSeats(flightId);
        // Oczekujemy dostępnych miejsc: 2,4,6,7,8,9,10 – czyli 7 wolnych miejsc
        assertEquals(7, availableSeats.size());
        assertFalse(availableSeats.contains(1));
        assertTrue(availableSeats.contains(2));
    }

    // --- Test metody getAvailableSeats, gdy lot nie istnieje ---
    @Test
    void testGetAvailableSeats_FlightNotFound() {
        Long flightId = 99L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());
        // Jeśli ErrorType.create rzuca BusinessException, to test oczekuje takiego wyjątku.
        assertThrows(RuntimeException.class, () -> reservationService.getAvailableSeats(flightId));
    }

    // --- Test metody cancelReservation – sukces ---
    @Test
    void testCancelReservation_Success() {
        Long reservationId = 100L;
        Reservation reservation = new Reservation();
        reservation.setActive(true);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        reservationService.cancelReservation(reservationId);

        assertFalse(reservation.isActive());
        verify(reservationRepository, times(1)).save(reservation);
    }

    // --- Test metody cancelReservation, gdy rezerwacja nie istnieje ---
    @Test
    void testCancelReservation_ReservationNotFound() {
        Long reservationId = 101L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> reservationService.cancelReservation(reservationId));
        verify(reservationRepository, never()).save(any());
    }
}
