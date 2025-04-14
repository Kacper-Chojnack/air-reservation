package com.example.airreservation.service;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.repository.AirplaneRepository;
import com.example.airreservation.repository.AirportRepository;
import com.example.airreservation.repository.FlightRepository;
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

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private FlightMapper flightMapper;
    @Mock
    private AirportRepository airportRepository;
    @Mock
    private AirplaneRepository airplaneRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test poprawnego zapisu lotu
    @Test
    void testSaveFlight_Successful() {
        // Przygotowanie danych
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(3L);
        flightDTO.setFlightNumber("FL123");
        flightDTO.setFlightDurationHours(2);
        flightDTO.setFlightDurationMinutes(30);
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));

        Flight flight = new Flight();
        // Załóżmy, że mapper przekłada FlightDTO na Flight
        when(flightMapper.flightDTOToFlight(flightDTO)).thenReturn(flight);

        Airport departureAirport = new Airport();
        departureAirport.setId(1L);
        departureAirport.setName("Airport A");

        Airport arrivalAirport = new Airport();
        arrivalAirport.setId(2L);
        arrivalAirport.setName("Airport B");

        Airplane airplane = new Airplane();
        airplane.setId(3L);
        airplane.setName("Boeing 737");
        airplane.setTotalSeats(189);

        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(airplaneRepository.findById(3L)).thenReturn(Optional.of(airplane));

        Flight savedFlight = new Flight();
        savedFlight.setFlightNumber("FL123");
        savedFlight.setDepartureAirport(departureAirport);
        savedFlight.setArrivalAirport(arrivalAirport);
        savedFlight.setAirplane(airplane);
        savedFlight.setFlightDuration(Duration.ofHours(2).plusMinutes(30));
        // symulujemy zapis encji
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);
        when(flightMapper.flightToFlightDTO(savedFlight)).thenReturn(flightDTO);

        // Wywołanie metody
        FlightDTO result = flightService.saveFlight(flightDTO);

        // Weryfikacja
        assertNotNull(result);
        assertEquals("FL123", result.getFlightNumber());
        verify(airportRepository, times(1)).findById(1L);
        verify(airportRepository, times(1)).findById(2L);
        verify(airplaneRepository, times(1)).findById(3L);
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    // Test, gdy lotnisko wylotu nie zostanie znalezione
    @Test
    void testSaveFlight_DepartureAirportNotFound() {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(3L);
        flightDTO.setFlightNumber("FL123");
        flightDTO.setFlightDurationHours(1);
        flightDTO.setFlightDurationMinutes(0);
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));

        when(airportRepository.findById(1L)).thenReturn(Optional.empty());
        Airport arrivalAirport = new Airport();
        arrivalAirport.setId(2L);
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));

        AirportNotFoundException exception = assertThrows(
                AirportNotFoundException.class,
                () -> flightService.saveFlight(flightDTO)
        );
        assertNotNull(exception);
        verify(airportRepository, times(1)).findById(1L);
    }

    // Test, gdy lotnisko przylotu nie zostanie znalezione
    @Test
    void testSaveFlight_ArrivalAirportNotFound() {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(3L);
        flightDTO.setFlightNumber("FL123");
        flightDTO.setFlightDurationHours(1);
        flightDTO.setFlightDurationMinutes(0);
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));

        Airport departureAirport = new Airport();
        departureAirport.setId(1L);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class,
                () -> flightService.saveFlight(flightDTO));
        verify(airportRepository, times(1)).findById(2L);
    }

    // Test, gdy samolot nie zostanie znaleziony
    @Test
    void testSaveFlight_AirplaneNotFound() {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(3L);
        flightDTO.setFlightNumber("FL123");
        flightDTO.setFlightDurationHours(1);
        flightDTO.setFlightDurationMinutes(0);
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));

        Airport departureAirport = new Airport();
        departureAirport.setId(1L);
        Airport arrivalAirport = new Airport();
        arrivalAirport.setId(2L);

        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(airplaneRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AirplaneNotFoundException.class,
                () -> flightService.saveFlight(flightDTO));
        verify(airplaneRepository, times(1)).findById(3L);
    }

    // Test, gdy oba lotniska są takie same – rzucenie wyjątku InvalidFlightException
    @Test
    void testSaveFlight_SameAirports() {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(1L); // Same airport IDs
        flightDTO.setAirplane(3L);
        flightDTO.setFlightNumber("FL123");
        flightDTO.setFlightDurationHours(1);
        flightDTO.setFlightDurationMinutes(0);
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));

        Airport airport = new Airport();
        airport.setId(1L);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(airport));

        assertThrows(InvalidFlightException.class,
                () -> flightService.saveFlight(flightDTO));

        verify(airportRepository, times(2)).findById(1L);
    }

    // Test metody getAvailableSeats - scenariusz poprawny
    @Test
    void testGetAvailableSeats() {
        Long flightId = 10L;
        Flight flight = new Flight();
        flight.setId(flightId);
        Airplane airplane = new Airplane();
        airplane.setId(5L);
        airplane.setTotalSeats(100);
        flight.setAirplane(airplane);

        // Załóżmy, że rezerwacje zajęły miejsca: 1, 2, 3, 50
        List<Integer> reservedSeats = List.of(1, 2, 3, 50);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(reservationRepository.findSeatNumberByFlightIdAndActiveTrue(flightId)).thenReturn(reservedSeats);

        List<Integer> availableSeats = flightService.getAvailableSeats(flightId);

        // Spodziewamy się 96 wolnych miejsc
        assertEquals(96, availableSeats.size());
        // Przykładowo, sprawdź, czy miejsce 1 jest zarezerwowane, a miejsce 4 dostępne:
        assertFalse(availableSeats.contains(1));
        assertTrue(availableSeats.contains(4));
    }

    // Test metody getAvailableSeats, gdy lot nie istnieje
    @Test
    void testGetAvailableSeats_FlightNotFound() {
        Long flightId = 99L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class,
                () -> flightService.getAvailableSeats(flightId));
    }

    // Test metody getAvailableFlights, sprawdzając delegację do repozytorium
    @Test
    void testGetAvailableFlights() {
        LocalDateTime now = LocalDateTime.now();
        List<Flight> flights = List.of(new Flight(), new Flight());
        when(flightRepository.findVisibleFlights(now)).thenReturn(flights);

        List<Flight> result = flightService.getAvailableFlights(now);

        assertEquals(2, result.size());
        verify(flightRepository, times(1)).findVisibleFlights(now);
    }
}
