package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.model.flight.FlightMapper;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerAdminEditDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.model.reservation.ReservationAdminEditDTO;
import com.example.airreservation.model.reservation.ReservationMapper;
import com.example.airreservation.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;
    @Autowired
    private PassengerService passengerService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private AirportService airportService;
    @Autowired
    private AirplaneService airplaneService;
    @Autowired
    private FlightMapper flightMapper;
    @Autowired
    private PassengerMapper passengerMapper;
    @Autowired
    private ReservationMapper reservationMapper;


    @TestConfiguration
    static class AdminControllerTestConfiguration {
        @Bean
        FlightService flightService() {
            return Mockito.mock(FlightService.class);
        }

        @Bean
        PassengerService passengerService() {
            return Mockito.mock(PassengerService.class);
        }

        @Bean
        ReservationService reservationService() {
            return Mockito.mock(ReservationService.class);
        }

        @Bean
        AirportService airportService() {
            return Mockito.mock(AirportService.class);
        }

        @Bean
        AirplaneService airplaneService() {
            return Mockito.mock(AirplaneService.class);
        }

        @Bean
        FlightMapper flightMapper() {
            return Mockito.mock(FlightMapper.class);
        }

        @Bean
        PassengerMapper passengerMapper() {
            return Mockito.mock(PassengerMapper.class);
        }

        @Bean
        ReservationMapper reservationMapper() {
            return Mockito.mock(ReservationMapper.class);
        }
    }

    private Flight flight;
    private FlightDTO flightDTO;
    private Passenger passenger;
    private PassengerAdminEditDTO passengerAdminEditDTO;
    private Reservation reservation;
    private ReservationAdminEditDTO reservationAdminEditDTO;

    @BeforeEach
    void setUp() {
        Airport dep = new Airport();
        dep.setId(1L);
        dep.setName("WAW");
        Airport arr = new Airport();
        arr.setId(2L);
        arr.setName("JFK");
        Airplane plane = new Airplane("B737", 180);
        plane.setId(1L);

        flight = new Flight();
        flight.setId(1L);
        flight.setDepartureAirport(dep);
        flight.setArrivalAirport(arr);
        flight.setAirplane(plane);
        flight.setFlightNumber("LO123");
        flight.setDepartureDate(LocalDateTime.now().plusDays(5));
        flight.setFlightDuration(Duration.ofHours(8));

        flightDTO = new FlightDTO();
        flightDTO.setId(1L);
        flightDTO.setDepartureAirportId(1L);
        flightDTO.setArrivalAirportId(2L);
        flightDTO.setAirplane(1L);
        flightDTO.setFlightNumber("LO123");
        flightDTO.setDepartureDate(flight.getDepartureDate());
        flightDTO.setFlightDurationHours(8);
        flightDTO.setFlightDurationMinutes(0);
        flightDTO.setRoundTrip(false);

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("Admin");
        passenger.setSurname("Test");
        passenger.setEmail("admin@test.com");
        passenger.setPhoneNumber("111222333");
        passenger.setRole("ROLE_ADMIN");
        passenger.setEnabled(true);

        passengerAdminEditDTO = new PassengerAdminEditDTO();
        passengerAdminEditDTO.setId(1L);
        passengerAdminEditDTO.setName("Admin Edit");
        passengerAdminEditDTO.setSurname("Test Edit");
        passengerAdminEditDTO.setEmail("admin_edit@test.com");
        passengerAdminEditDTO.setPhoneNumber("333222111");
        passengerAdminEditDTO.setRole("ROLE_USER");
        passengerAdminEditDTO.setEnabled(false);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setPassenger(passenger);
        reservation.setFlight(flight);
        reservation.setSeatNumber(10);
        reservation.setReservationNumber("RESADMIN1");
        reservation.setEmail(passenger.getEmail());
        reservation.setFlightNumber(flight.getFlightNumber());

        reservationAdminEditDTO = new ReservationAdminEditDTO();
        reservationAdminEditDTO.setId(1L);
        reservationAdminEditDTO.setSeatNumber(15);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboard_shouldReturnAdminIndexView() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFlights_shouldReturnFlightsListView() throws Exception {
        Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(flight));
        when(flightService.findAllFlightsPaginated(any(Pageable.class))).thenReturn(flightPage);

        mockMvc.perform(get("/admin/flights"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flights/list"))
                .andExpect(model().attributeExists("flightPage"))
                .andExpect(model().attribute("flightPage", hasProperty("content", hasSize(1))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditFlightForm_shouldReturnEditView() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(flight);
        when(flightMapper.flightToFlightDTO(flight)).thenReturn(flightDTO);
        when(airportService.getAllAirports()).thenReturn(Collections.emptyList());
        when(airplaneService.getAllAirplanes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/flights/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flights/edit"))
                .andExpect(model().attributeExists("flightDTO", "airports", "airplanes"))
                .andExpect(model().attribute("flightDTO", hasProperty("id", is(1L))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditFlightForm_NotFound_shouldRedirectToList() throws Exception {
        when(flightService.getFlightById(99L)).thenThrow(ErrorType.FLIGHT_NOT_FOUND.create(99L));

        mockMvc.perform(get("/admin/flights/edit/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flights"))
                .andExpect(flash().attributeExists("errorMessage"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFlight_Success_shouldRedirectToList() throws Exception {
        when(flightService.updateFlight(any(FlightDTO.class))).thenReturn(flightDTO);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("departureAirportId", "1");
        params.add("arrivalAirportId", "2");
        params.add("airplane", "1");
        params.add("flightNumber", "LO123");
        params.add("departureDate", LocalDateTime.now().plusDays(2).toString());
        params.add("flightDurationHours", "2");
        params.add("flightDurationMinutes", "30");
        params.add("roundTrip", "false");


        mockMvc.perform(post("/admin/flights/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flights"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(flightService).updateFlight(any(FlightDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFlight_ValidationError_shouldReturnEditView() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("flightNumber", ""); // Błąd walidacji

        mockMvc.perform(post("/admin/flights/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flights/edit"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("flightDTO", "flightNumber"));
        verify(flightService, never()).updateFlight(any(FlightDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFlight_Success_shouldRedirectToList() throws Exception {
        doNothing().when(flightService).deleteFlight(1L);

        mockMvc.perform(post("/admin/flights/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flights"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(flightService).deleteFlight(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFlight_HasReservations_shouldRedirectToListWithError() throws Exception {
        doThrow(ErrorType.FLIGHT_HAS_RESERVATIONS.create(1L)).when(flightService).deleteFlight(1L);

        mockMvc.perform(post("/admin/flights/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/flights"))
                .andExpect(flash().attributeExists("errorMessage"));
        verify(flightService).deleteFlight(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPassengers_shouldReturnPassengersListView() throws Exception {
        Page<Passenger> passengerPage = new PageImpl<>(Collections.singletonList(passenger));
        when(passengerService.findAllPassengersPaginated(any(Pageable.class))).thenReturn(passengerPage);

        mockMvc.perform(get("/admin/passengers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/passengers/list"))
                .andExpect(model().attributeExists("passengerPage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditPassengerForm_Success() throws Exception {
        when(passengerService.findPassengerById(1L)).thenReturn(Optional.of(passenger));
        when(passengerMapper.passengerToAdminEditDTO(passenger)).thenReturn(passengerAdminEditDTO);

        mockMvc.perform(get("/admin/passengers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/passengers/edit"))
                .andExpect(model().attributeExists("passengerAdminEditDTO"))
                .andExpect(model().attribute("passengerAdminEditDTO", hasProperty("id", is(1L))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditPassengerForm_NotFound_Redirects() throws Exception {
        when(passengerService.findPassengerById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/passengers/edit/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/passengers"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassenger_Success_shouldRedirectToList() throws Exception {
        doNothing().when(passengerService).updatePassengerByAdmin(any(PassengerAdminEditDTO.class));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("name", "Janek");
        params.add("surname", "Nowy");
        params.add("email", "j.nowy@test.com");
        params.add("phoneNumber", "111000111");
        params.add("role", "ROLE_USER");
        params.add("enabled", "true");

        mockMvc.perform(post("/admin/passengers/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/passengers"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(passengerService).updatePassengerByAdmin(any(PassengerAdminEditDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePassenger_ValidationError_shouldRedirectToEdit() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("email", "invalid-email");

        mockMvc.perform(post("/admin/passengers/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/passengers/edit/1"))
                .andExpect(flash().attributeExists("errorMessage", "org.springframework.validation.BindingResult.passengerAdminEditDTO", "passengerAdminEditDTO"));
        verify(passengerService, never()).updatePassengerByAdmin(any(PassengerAdminEditDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePassenger_Success_shouldRedirectToList() throws Exception {
        doNothing().when(passengerService).deletePassenger(1L);

        mockMvc.perform(post("/admin/passengers/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/passengers"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(passengerService).deletePassenger(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listReservations_shouldReturnReservationsListView() throws Exception {
        Page<Reservation> reservationPage = new PageImpl<>(Collections.singletonList(reservation));
        when(reservationService.findAllReservationsPaginated(any(Pageable.class))).thenReturn(reservationPage);

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservations/list"))
                .andExpect(model().attributeExists("reservationPage"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditReservationForm_Success() throws Exception {
        when(reservationService.findReservationById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.reservationToAdminEditDTO(reservation)).thenReturn(reservationAdminEditDTO);
        when(flightService.getAvailableSeatsIncludingCurrentLocks(eq(10L), eq(25))).thenReturn(List.of(5, 10, 25, 30));

        mockMvc.perform(get("/admin/reservations/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservations/edit"))
                .andExpect(model().attributeExists("reservationAdminEditDTO", "availableSeats"))
                .andExpect(model().attribute("reservationAdminEditDTO", hasProperty("id", is(1L))));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReservation_Success_shouldRedirectToList() throws Exception {
        doNothing().when(reservationService).updateReservationByAdmin(any(ReservationAdminEditDTO.class));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("seatNumber", "30");

        mockMvc.perform(post("/admin/reservations/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(reservationService).updateReservationByAdmin(any(ReservationAdminEditDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReservation_SeatOccupied_shouldRedirectToEdit() throws Exception {
        doThrow(ErrorType.SEAT_OCCUPIED.create(30)).when(reservationService).updateReservationByAdmin(any(ReservationAdminEditDTO.class));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "1");
        params.add("seatNumber", "30");

        mockMvc.perform(post("/admin/reservations/update")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations/edit/1"))
                .andExpect(flash().attributeExists("errorMessage", "reservationAdminEditDTO"));
        verify(reservationService).updateReservationByAdmin(any(ReservationAdminEditDTO.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReservation_Success_shouldRedirectToList() throws Exception {
        doNothing().when(reservationService).deleteReservation(1L);

        mockMvc.perform(post("/admin/reservations/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(reservationService).deleteReservation(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReservation_NotFound_shouldRedirectToListWithError() throws Exception {
        doThrow(ErrorType.RESERVATION_NOT_FOUND.create(99L)).when(reservationService).deleteReservation(99L);

        mockMvc.perform(post("/admin/reservations/delete/99").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations"))
                .andExpect(flash().attributeExists("errorMessage"));
        verify(reservationService).deleteReservation(99L);
    }

}
