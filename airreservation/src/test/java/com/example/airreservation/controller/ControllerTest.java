package com.example.airreservation.controller;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
@Import(ControllerTest.TestConfig.class)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @Test
    void testGetFlights() throws Exception {
        Flight flight = new Flight();
        flight.setFlightNumber("AB123");
        flight.setDepartureDate(LocalDateTime.now().plusDays(1));

        when(flightService.getAvailableFlights(any())).thenReturn(Collections.singletonList(flight));

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("flights"))
                .andExpect(view().name("flights"));

        verify(flightService, times(1)).getAvailableFlights(any());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FlightService flightService() {
            return Mockito.mock(FlightService.class);
        }
    }
}