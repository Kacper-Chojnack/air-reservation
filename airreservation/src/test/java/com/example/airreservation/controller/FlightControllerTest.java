package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.FlightDTO;
import com.example.airreservation.service.AirplaneService;
import com.example.airreservation.service.AirportService;
import com.example.airreservation.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;
    @Autowired
    private AirportService airportService;
    @Autowired
    private AirplaneService airplaneService;

    @TestConfiguration
    static class FlightControllerTestConfiguration {
        @Bean
        FlightService flightService() {
            return Mockito.mock(FlightService.class);
        }

        @Bean
        AirportService airportService() {
            return Mockito.mock(AirportService.class);
        }

        @Bean
        AirplaneService airplaneService() {
            return Mockito.mock(AirplaneService.class);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showCreateForm_Admin_shouldReturnCreateView() throws Exception {
        when(airportService.getAllAirports()).thenReturn(Collections.emptyList());
        when(airplaneService.getAllAirplanes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/flights/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/create"))
                .andExpect(model().attributeExists("flightDTO", "airports", "airplanes"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showCreateForm_User_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/flights/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    void showCreateForm_Anonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/flights/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFlight_Admin_Success_shouldRedirectToHome() throws Exception {
        when(flightService.saveFlight(any(FlightDTO.class))).thenReturn(new FlightDTO());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("departureAirportId", "1");
        params.add("arrivalAirportId", "2");
        params.add("airplane", "1");
        params.add("flightNumber", "LO123");
        params.add("departureDate", LocalDateTime.now().plusDays(2).toString());
        params.add("flightDurationHours", "2");
        params.add("flightDurationMinutes", "30");
        params.add("roundTrip", "false");

        mockMvc.perform(post("/flights")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(flightService).saveFlight(any(FlightDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFlight_Admin_ValidationError_shouldReturnCreateView() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("flightNumber", "");

        mockMvc.perform(post("/flights")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("flightDTO", "flightNumber"));

        verify(flightService, never()).saveFlight(any(FlightDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFlight_Admin_SameAirportError_shouldReturnCreateView() throws Exception {
        when(flightService.saveFlight(any(FlightDTO.class))).thenThrow(ErrorType.AIRPORT_SAME_ERROR.create());
        when(airportService.getAllAirports()).thenReturn(Collections.emptyList());
        when(airplaneService.getAllAirplanes()).thenReturn(Collections.emptyList());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("departureAirportId", "1");
        params.add("arrivalAirportId", "1");
        params.add("airplane", "1");
        params.add("flightNumber", "LO123");
        params.add("departureDate", LocalDateTime.now().plusDays(2).toString());
        params.add("flightDurationHours", "2");
        params.add("flightDurationMinutes", "30");
        params.add("roundTrip", "false");

        mockMvc.perform(post("/flights")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("flights/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("flightDTO", "arrivalAirportId"));

        verify(flightService).saveFlight(any(FlightDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFlight_Admin_OtherBusinessError_shouldRedirectWithError() throws Exception {
        when(flightService.saveFlight(any(FlightDTO.class))).thenThrow(ErrorType.AIRPLANE_NOT_FOUND.create());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("departureAirportId", "1");
        params.add("arrivalAirportId", "2");
        params.add("airplane", "99");
        params.add("flightNumber", "LO123");
        params.add("departureDate", LocalDateTime.now().plusDays(2).toString());
        params.add("flightDurationHours", "2");
        params.add("flightDurationMinutes", "30");
        params.add("roundTrip", "false");

        mockMvc.perform(post("/flights")
                        .params(params)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/flights/create"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(flightService).saveFlight(any(FlightDTO.class));
    }


    @Test
    @WithMockUser(roles = "USER")
    void createFlight_User_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/flights")
                        .with(csrf()))
                .andExpect(status().isForbidden());
        verify(flightService, never()).saveFlight(any(FlightDTO.class));
    }

    @Test
    void createFlight_Anonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/flights")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        verify(flightService, never()).saveFlight(any(FlightDTO.class));
    }
}