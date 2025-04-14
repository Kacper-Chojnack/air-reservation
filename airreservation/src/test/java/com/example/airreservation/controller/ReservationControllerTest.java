package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.lock.TemporarySeatLock;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.service.FlightService;
import com.example.airreservation.service.ReservationService;
import com.example.airreservation.service.SeatLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private FlightService flightService;
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private SeatLockService seatLockService;

    @TestConfiguration
    static class ReservationControllerTestConfig {
        @Bean
        ReservationService reservationService() {
            return Mockito.mock(ReservationService.class);
        }

        @Bean
        FlightService flightService() {
            return Mockito.mock(FlightService.class);
        }

        @Bean
        PassengerRepository passengerRepository() {
            return Mockito.mock(PassengerRepository.class);
        }

        @Bean
        SeatLockService seatLockService() {
            return Mockito.mock(SeatLockService.class);
        }
    }

    private Flight flight;
    private Passenger passenger;
    private TemporarySeatLock lock;

    @BeforeEach
    void setUp() {
        Airplane airplane = new Airplane();
        airplane.setId(1L);
        airplane.setTotalSeats(100);
        flight = new Flight();
        flight.setId(1L);
        flight.setAirplane(airplane);
        flight.setDepartureDate(LocalDateTime.now().plusDays(1));
        flight.setDepartureAirport(new Airport());
        flight.getDepartureAirport().setName("WAW");
        flight.setArrivalAirport(new Airport());
        flight.getArrivalAirport().setName("JFK");
        flight.setFlightNumber("LO123");
        flight.setFlightDuration(Duration.ofHours(8));

        passenger = new Passenger();
        passenger.setId(5L);
        passenger.setEmail("user@test.com");

        lock = new TemporarySeatLock(flight, 25, passenger, LocalDateTime.now().plusMinutes(1));
        lock.setId(50L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showCreateForm_Success() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(flight);
        when(flightService.getAvailableSeats(1L)).thenReturn(List.of(10, 25, 30));

        mockMvc.perform(get("/reservations/create").param("flightId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/create"))
                .andExpect(model().attributeExists("reservationDTO", "flightId", "availableSeats", "departureAirport"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showCreateForm_FlightNotAvailable_RedirectsHome() throws Exception {
        flight.setDepartureDate(LocalDateTime.now().minusDays(1));
        when(flightService.getFlightById(1L)).thenReturn(flight);

        mockMvc.perform(get("/reservations/create").param("flightId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void showCreateForm_Anonymous_RedirectsLogin() throws Exception {
        mockMvc.perform(get("/reservations/create").param("flightId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void acquireLock_Success_RedirectsToConfirmPassword() throws Exception {
        when(passengerRepository.findByEmail("user@test.com")).thenReturn(Optional.of(passenger));
        when(seatLockService.acquireLock(1L, 25, 5L)).thenReturn(lock);

        mockMvc.perform(post("/reservations/acquire-lock")
                        .param("flightId", "1")
                        .param("seatNumber", "25")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/confirm-password"))
                .andExpect(request().sessionAttribute("seatLockId", is(50L)))
                .andExpect(request().sessionAttribute("seatLockExpiresAt", is(lock.getExpiresAt())));

        verify(seatLockService).acquireLock(1L, 25, 5L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void acquireLock_NoSeatSelected_RedirectsBackToCreate() throws Exception {
        mockMvc.perform(post("/reservations/acquire-lock")
                        .param("flightId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/create?flightId=1"))
                .andExpect(flash().attribute("error", "Musisz wybrać miejsce."));
        verify(seatLockService, never()).acquireLock(anyLong(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void acquireLock_SeatLocked_RedirectsBackToCreate() throws Exception {
        when(passengerRepository.findByEmail("user@test.com")).thenReturn(Optional.of(passenger));
        when(seatLockService.acquireLock(1L, 25, 5L)).thenThrow(ErrorType.SEAT_LOCKED_BY_ANOTHER_USER.create(25));

        mockMvc.perform(post("/reservations/acquire-lock")
                        .param("flightId", "1")
                        .param("seatNumber", "25")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/create?flightId=1"))
                .andExpect(flash().attributeExists("error"));
        verify(seatLockService).acquireLock(1L, 25, 5L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showConfirmPasswordPage_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("seatLockId", 50L);
        session.setAttribute("seatLockExpiresAt", LocalDateTime.now().plusSeconds(30));
        session.setAttribute("seatLockFlightId", 1L);
        session.setAttribute("seatLockSeatNumber", 25);

        when(flightService.getFlightById(1L)).thenReturn(flight);

        mockMvc.perform(get("/reservations/confirm-password").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/confirm-password"))
                .andExpect(model().attributeExists("lockId", "expiresAtMillis", "flightInfo", "seatNumber", "passwordConfirmDTO"))
                .andExpect(model().attribute("lockId", is(50L)));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showConfirmPasswordPage_NoLockInSession_RedirectsHome() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/reservations/confirm-password").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void showConfirmPasswordPage_LockExpiredInSession_RedirectsToCreate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("seatLockId", 50L);
        session.setAttribute("seatLockExpiresAt", LocalDateTime.now().minusSeconds(30)); // Expired
        session.setAttribute("seatLockFlightId", 1L);
        session.setAttribute("seatLockSeatNumber", 25);

        mockMvc.perform(get("/reservations/confirm-password").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/create?flightId=1"))
                .andExpect(flash().attributeExists("error"));
    }


    @Test
    @WithMockUser(username = "user@test.com")
    void finalizeReservation_Success_RedirectsToProfile() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("seatLockId", 50L);

        when(seatLockService.finalizeReservationWithPassword(50L, "correct_password")).thenReturn(new Reservation());

        mockMvc.perform(post("/reservations/finalize")
                        .param("lockId", "50")
                        .param("password", "correct_password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(request().sessionAttributeDoesNotExist("seatLockId"));

        verify(seatLockService).finalizeReservationWithPassword(50L, "correct_password");
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void finalizeReservation_InvalidPassword_RedirectsToConfirmPassword() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("seatLockId", 50L);
        session.setAttribute("seatLockFlightId", 1L);
        session.setAttribute("seatLockSeatNumber", 25);

        when(seatLockService.finalizeReservationWithPassword(50L, "wrong_password"))
                .thenThrow(ErrorType.INVALID_PASSWORD_FOR_RESERVATION.create());

        mockMvc.perform(post("/reservations/finalize")
                        .param("lockId", "50")
                        .param("password", "wrong_password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/confirm-password"))
                .andExpect(flash().attributeExists("error"));
        verify(seatLockService).finalizeReservationWithPassword(50L, "wrong_password");
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void finalizeReservation_LockExpired_RedirectsToCreate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("seatLockId", 50L);
        session.setAttribute("seatLockFlightId", 1L);
        session.setAttribute("seatLockSeatNumber", 25);

        when(seatLockService.finalizeReservationWithPassword(50L, "password"))
                .thenThrow(ErrorType.LOCK_EXPIRED.create());

        mockMvc.perform(post("/reservations/finalize")
                        .param("lockId", "50")
                        .param("password", "password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/create?flightId=1"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(request().sessionAttributeDoesNotExist("seatLockId"));
        verify(seatLockService).finalizeReservationWithPassword(50L, "password");
    }

    @Test
    void finalizeReservation_Anonymous_RedirectsLogin() throws Exception {
        mockMvc.perform(post("/reservations/finalize")
                        .param("lockId", "50")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        verify(seatLockService, never()).finalizeReservationWithPassword(anyLong(), anyString());
    }
}