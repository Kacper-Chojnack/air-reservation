package com.example.airreservation.controller;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.service.AirportService;
import com.example.airreservation.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;
    @Autowired
    private AirportService airportService;

    @TestConfiguration
    static class HomeControllerTestConfiguration {
        @Bean
        FlightService flightService() {
            return Mockito.mock(FlightService.class);
        }

        @Bean
        AirportService airportService() {
            return Mockito.mock(AirportService.class);
        }
    }

    @Test
    void index_NoSearchParams_shouldReturnIndexViewWithUpcomingAndAirports() throws Exception {
        List<Flight> upcoming = Collections.singletonList(new Flight());
        List<Airport> airports = Collections.singletonList(new Airport());
        when(flightService.getUpcomingFlights(5)).thenReturn(upcoming);
        when(airportService.getAllAirports()).thenReturn(airports);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("upcomingFlights", "airports"))
                .andExpect(model().attribute("upcomingFlights", hasSize(1)))
                .andExpect(model().attribute("airports", hasSize(1)))
                .andExpect(model().attributeDoesNotExist("flightPage", "errorMessage"));
        verify(flightService).getUpcomingFlights(5);
        verify(airportService).getAllAirports();
        verify(flightService, never()).searchFlights(any(), any(), any(), any());
    }

    @Test
    void index_WithSearchParams_shouldReturnIndexViewWithResults() throws Exception {
        Long depId = 1L;
        Long arrId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        Page<Flight> flightPage = new PageImpl<>(Collections.singletonList(new Flight()));
        List<Flight> upcoming = Collections.emptyList();
        List<Airport> airports = Collections.emptyList();

        when(flightService.searchFlights(eq(depId), eq(arrId), eq(date), any(Pageable.class))).thenReturn(flightPage);
        when(flightService.getUpcomingFlights(5)).thenReturn(upcoming);
        when(airportService.getAllAirports()).thenReturn(airports);

        mockMvc.perform(get("/")
                        .param("departureAirportId", depId.toString())
                        .param("arrivalAirportId", arrId.toString())
                        .param("departureDate", date.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("flightPage", "upcomingFlights", "airports",
                        "searchDepartureAirportId", "searchArrivalAirportId", "searchDepartureDate"))
                .andExpect(model().attribute("flightPage", flightPage))
                .andExpect(model().attribute("searchDepartureAirportId", depId))
                .andExpect(model().attribute("searchArrivalAirportId", arrId))
                .andExpect(model().attribute("searchDepartureDate", date));

        verify(flightService).searchFlights(eq(depId), eq(arrId), eq(date), any(Pageable.class));
        verify(flightService).getUpcomingFlights(5);
        verify(airportService).getAllAirports();
    }

    @Test
    void index_WithSearchParams_ServiceError_shouldReturnIndexWithError() throws Exception {
        Long depId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        when(flightService.searchFlights(eq(depId), isNull(), eq(date), any(Pageable.class)))
                .thenThrow(new RuntimeException("Search failed"));
        when(flightService.getUpcomingFlights(5)).thenReturn(Collections.emptyList());
        when(airportService.getAllAirports()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/")
                        .param("departureAirportId", depId.toString())
                        .param("departureDate", date.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("errorMessage", "upcomingFlights", "airports"))
                .andExpect(model().attributeDoesNotExist("flightPage"));

        verify(flightService).searchFlights(eq(depId), isNull(), eq(date), any(Pageable.class));
    }
}