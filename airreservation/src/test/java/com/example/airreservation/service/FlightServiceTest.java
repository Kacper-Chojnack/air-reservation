package com.example.airreservation.service;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private TemporarySeatLockRepository temporarySeatLockRepository;

    @InjectMocks
    private FlightService flightService;

    private FlightDTO flightDTO;
    private Flight flight;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private Airplane airplane;
    private Flight pastFlight;
    private Flight completedFlight;


    @BeforeEach
    void setUp() {
        departureAirport = new Airport();
        departureAirport.setId(1L);
        departureAirport.setName("WAW");
        arrivalAirport = new Airport();
        arrivalAirport.setId(2L);
        arrivalAirport.setName("JFK");
        airplane = new Airplane();
        airplane.setId(10L);
        airplane.setName("Boeing 737");
        airplane.setTotalSeats(150);

        flightDTO = new FlightDTO();
        flightDTO.setId(1L);
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(10L);
        flightDTO.setFlightNumber("LO123");
        flightDTO.setDepartureDate(LocalDateTime.now().plusDays(1));
        flightDTO.setFlightDurationHours(2);
        flightDTO.setFlightDurationMinutes(30);
        flightDTO.setRoundTrip(false);

        flight = new Flight();
        flight.setId(1L);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setAirplane(airplane);
        flight.setFlightNumber("LO123");
        flight.setDepartureDate(flightDTO.getDepartureDate());
        flight.setFlightDuration(Duration.ofHours(2).plusMinutes(30));
        flight.setCompleted(false);

        pastFlight = new Flight();
        pastFlight.setId(2L);
        pastFlight.setDepartureDate(LocalDateTime.now().minusHours(1));
        pastFlight.setFlightDuration(Duration.ofHours(1));
        pastFlight.setCompleted(false);
        pastFlight.setDepartureAirport(departureAirport);
        pastFlight.setArrivalAirport(arrivalAirport);

        completedFlight = new Flight();
        completedFlight.setId(3L);
        completedFlight.setDepartureDate(LocalDateTime.now().plusHours(2));
        completedFlight.setFlightDuration(Duration.ofHours(1));
        completedFlight.setCompleted(true);
        completedFlight.setDepartureAirport(departureAirport);
        completedFlight.setArrivalAirport(arrivalAirport);
    }

    @Test
    void saveFlight_Success() {
        when(flightMapper.flightDTOToFlight(any(FlightDTO.class))).thenReturn(flight);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(airplaneRepository.findById(10L)).thenReturn(Optional.of(airplane));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);
        when(flightMapper.flightToFlightDTO(any(Flight.class))).thenReturn(flightDTO);

        FlightDTO savedDto = flightService.saveFlight(flightDTO);

        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getFlightNumber()).isEqualTo(flightDTO.getFlightNumber());
        verify(airportRepository).findById(1L);
        verify(airportRepository).findById(2L);
        verify(airplaneRepository).findById(10L);
        verify(flightRepository).save(flight);
    }

    @Test
    void saveFlight_DepartureAirportNotFound_ThrowsException() {
        when(flightMapper.flightDTOToFlight(any(FlightDTO.class))).thenReturn(new Flight());
        when(airportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.saveFlight(flightDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.AIRPORT_NOT_FOUND);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void saveFlight_SameAirport_ThrowsException() {
        flightDTO.setArrivalAirportId(1L);
        when(flightMapper.flightDTOToFlight(any(FlightDTO.class))).thenReturn(flight);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airplaneRepository.findById(10L)).thenReturn(Optional.of(airplane));

        assertThatThrownBy(() -> flightService.saveFlight(flightDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.AIRPORT_SAME_ERROR);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void saveFlight_ArrivalAirportNotFound_ThrowsException() {
        when(flightMapper.flightDTOToFlight(any(FlightDTO.class))).thenReturn(flight);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.saveFlight(flightDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.AIRPORT_NOT_FOUND);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void saveFlight_AirplaneNotFound_ThrowsException() {
        when(flightMapper.flightDTOToFlight(any(FlightDTO.class))).thenReturn(flight);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(airplaneRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.saveFlight(flightDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.AIRPLANE_NOT_FOUND);
        verify(flightRepository, never()).save(any(Flight.class));
    }


    @Test
    void getAvailableSeats_Success() {
        long flightId = 1L;
        int totalSeats = airplane.getTotalSeats();
        Reservation res1 = new Reservation();
        res1.setSeatNumber(5);
        Reservation res2 = new Reservation();
        res2.setSeatNumber(10);
        List<Reservation> reserved = Arrays.asList(res1, res2);
        List<Integer> expectedAvailable = IntStream.rangeClosed(1, totalSeats)
                .filter(seat -> seat != 5 && seat != 10)
                .boxed()
                .collect(Collectors.toList());

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(reservationRepository.findByFlightId(flightId)).thenReturn(reserved);
        when(temporarySeatLockRepository.findLockedSeatsByFlightId(eq(flightId), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        List<Integer> availableSeats = flightService.getAvailableSeats(flightId);

        assertThat(availableSeats).hasSize(totalSeats - 2);
        assertThat(availableSeats).containsExactlyElementsOf(expectedAvailable);
        assertThat(availableSeats).doesNotContain(5, 10);
    }

    @Test
    void getAvailableSeats_WithLockedSeats_Success() {
        long flightId = 1L;
        int totalSeats = airplane.getTotalSeats();
        Reservation res1 = new Reservation();
        res1.setSeatNumber(5);
        List<Reservation> reserved = Collections.singletonList(res1);
        List<Integer> locked = Collections.singletonList(10);
        List<Integer> expectedAvailable = IntStream.rangeClosed(1, totalSeats)
                .filter(seat -> seat != 5 && seat != 10)
                .boxed()
                .collect(Collectors.toList());

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(reservationRepository.findByFlightId(flightId)).thenReturn(reserved);
        when(temporarySeatLockRepository.findLockedSeatsByFlightId(eq(flightId), any(LocalDateTime.class))).thenReturn(locked);

        List<Integer> availableSeats = flightService.getAvailableSeats(flightId);

        assertThat(availableSeats).hasSize(totalSeats - 2);
        assertThat(availableSeats).containsExactlyElementsOf(expectedAvailable);
        assertThat(availableSeats).doesNotContain(5, 10);
    }


    @Test
    void getAvailableSeats_FlightNotFound_ThrowsException() {
        long flightId = 99L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getAvailableSeats(flightId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);
        verify(reservationRepository, never()).findByFlightId(anyLong());
        verify(temporarySeatLockRepository, never()).findLockedSeatsByFlightId(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void getAvailableFlights_ReturnsCorrectFlights() {
        List<Flight> allFlights = Arrays.asList(flight, pastFlight, completedFlight);
        when(flightRepository.findAll()).thenReturn(allFlights);

        List<Flight> available = flightService.getAvailableFlights(LocalDateTime.now());

        assertThat(available).hasSize(1);
        assertThat(available).containsExactly(flight);
    }

    @Test
    void getFlightById_Success() {
        long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));

        Flight result = flightService.getFlightById(flightId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(flightId);
        verify(flightRepository).findById(flightId);
    }

    @Test
    void getFlightById_NotFound_ThrowsException() {
        long flightId = 99L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(flightId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);
        verify(flightRepository).findById(flightId);
    }

    @Test
    void searchFlights_Success() {
        Long depId = 1L;
        Long arrId = 2L;
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> expectedPage = new PageImpl<>(Collections.singletonList(flight));

        when(flightRepository.findFlightsByCriteria(depId, arrId, startOfDay, endOfDay, pageable)).thenReturn(expectedPage);

        Page<Flight> actualPage = flightService.searchFlights(depId, arrId, date, pageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().get(0)).isEqualTo(flight);
        verify(flightRepository).findFlightsByCriteria(depId, arrId, startOfDay, endOfDay, pageable);
    }

    @Test
    void searchFlights_ArrivalAnywhere_Success() {
        Long depId = 1L;
        Long arrId = null;
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> expectedPage = new PageImpl<>(Collections.singletonList(flight));

        when(flightRepository.findFlightsByCriteria(depId, arrId, startOfDay, endOfDay, pageable)).thenReturn(expectedPage);

        Page<Flight> actualPage = flightService.searchFlights(depId, arrId, date, pageable);

        assertThat(actualPage).isNotNull();
        verify(flightRepository).findFlightsByCriteria(depId, null, startOfDay, endOfDay, pageable);
    }

    @Test
    void getUpcomingFlights_Success() {
        int limit = 3;
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "departureDate"));
        Page<Flight> expectedPage = new PageImpl<>(Arrays.asList(completedFlight, flight));

        when(flightRepository.findUpcomingAvailable(any(LocalDateTime.class), eq(pageable))).thenReturn(expectedPage);

        List<Flight> upcoming = flightService.getUpcomingFlights(limit);

        assertThat(upcoming).hasSize(2);
        assertThat(upcoming).containsExactly(completedFlight, flight);
        verify(flightRepository).findUpcomingAvailable(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void findAllFlightsPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> expectedPage = new PageImpl<>(Arrays.asList(flight, pastFlight));
        when(flightRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Flight> actualPage = flightService.findAllFlightsPaginated(pageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(2);
        verify(flightRepository).findAll(pageable);
    }

    @Test
    void updateFlight_Success() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(airportRepository.findById(1L)).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(arrivalAirport));
        when(airplaneRepository.findById(10L)).thenReturn(Optional.of(airplane));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(flightMapper.flightToFlightDTO(any(Flight.class))).thenReturn(flightDTO);

        FlightDTO updatedDto = flightService.updateFlight(flightDTO);

        assertThat(updatedDto).isNotNull();
        verify(flightRepository).findById(1L);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void updateFlight_FlightNotFound_ThrowsException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.updateFlight(flightDTO))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);
        verify(flightRepository, never()).save(any(Flight.class));
    }


    @Test
    void deleteFlight_Success() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightId(1L)).thenReturn(false);
        doNothing().when(temporarySeatLockRepository).deleteByFlightId(1L);
        doNothing().when(flightRepository).deleteById(1L);

        assertThatCode(() -> flightService.deleteFlight(1L)).doesNotThrowAnyException();

        verify(reservationRepository).existsByFlightId(1L);
        verify(temporarySeatLockRepository).deleteByFlightId(1L);
        verify(flightRepository).deleteById(1L);
    }

    @Test
    void deleteFlight_FlightNotFound_ThrowsException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.deleteFlight(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_NOT_FOUND);

        verify(reservationRepository, never()).existsByFlightId(anyLong());
        verify(temporarySeatLockRepository, never()).deleteByFlightId(anyLong());
        verify(flightRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteFlight_HasReservations_ThrowsException() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(reservationRepository.existsByFlightId(1L)).thenReturn(true);

        assertThatThrownBy(() -> flightService.deleteFlight(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FLIGHT_HAS_RESERVATIONS);

        verify(temporarySeatLockRepository, never()).deleteByFlightId(anyLong());
        verify(flightRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAvailableSeatsIncludingCurrentLocks_Success() {
        long flightId = 1L;
        int currentSeat = 5;
        int totalSeats = airplane.getTotalSeats();
        Reservation resOther = new Reservation();
        resOther.setSeatNumber(10);
        List<Reservation> reserved = Collections.singletonList(resOther);
        List<Integer> locked = Collections.singletonList(15);
        List<Integer> expectedAvailable = IntStream.rangeClosed(1, totalSeats)
                .filter(seat -> seat != 10 && seat != 15) // 5 should be available now
                .boxed()
                .collect(Collectors.toList());


        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(reservationRepository.findByFlightId(flightId)).thenReturn(reserved);
        when(temporarySeatLockRepository.findLockedSeatsByFlightId(eq(flightId), any(LocalDateTime.class))).thenReturn(locked);

        List<Integer> availableSeats = flightService.getAvailableSeatsIncludingCurrentLocks(flightId, currentSeat);

        assertThat(availableSeats).hasSize(totalSeats - 2);
        assertThat(availableSeats).containsExactlyElementsOf(expectedAvailable);
        assertThat(availableSeats).contains(5);
        assertThat(availableSeats).doesNotContain(10, 15);
    }

}
