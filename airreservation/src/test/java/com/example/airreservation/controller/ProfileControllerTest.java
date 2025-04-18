package com.example.airreservation.controller;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @TestConfiguration
    static class ProfileControllerTestConfig {
        @Bean
        PassengerRepository passengerRepository() {
            return Mockito.mock(PassengerRepository.class);
        }

        @Bean
        ReservationRepository reservationRepository() {
            return Mockito.mock(ReservationRepository.class);
        }
    }

    private Passenger passenger;

    @BeforeEach
    void setUp() {
        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setEmail("user@example.com");
        passenger.setName("Test");
        passenger.setSurname("User");
        passenger.setEnabled(true);
        passenger.setRole("ROLE_USER");
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void showProfile_AuthenticatedUser_shouldReturnProfileView() throws Exception {
        when(passengerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(passenger));
        when(reservationRepository.findByPassengerIdOrderByFlightDepartureDateDesc(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("passenger", "futureReservations", "pastReservations"))
                .andExpect(model().attribute("passenger", hasProperty("email", is("user@example.com"))));

        verify(passengerRepository).findByEmail("user@example.com");
        verify(reservationRepository).findByPassengerIdOrderByFlightDepartureDateDesc(1L);
    }

    @Test
    void showProfile_UnauthenticatedUser_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        verify(passengerRepository, never()).findByEmail(anyString());
        verify(reservationRepository, never()).findByPassengerIdOrderByFlightDepartureDateDesc(anyLong());
    }

    @Test
    @WithMockUser(username = "unknown@user.com")
    void showProfile_AuthenticatedUserNotFound_shouldThrowException() throws Exception {
        when(passengerRepository.findByEmail("unknown@user.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockMvc.perform(get("/profile")))
                .hasCauseInstanceOf(UsernameNotFoundException.class);

        verify(passengerRepository).findByEmail("unknown@user.com");
        verify(reservationRepository, never()).findByPassengerIdOrderByFlightDepartureDateDesc(anyLong());
    }
}