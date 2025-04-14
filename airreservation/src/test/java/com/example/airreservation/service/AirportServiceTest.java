package com.example.airreservation.service;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.repository.AirportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportService airportService;

    @Test
    void getAllAirports_shouldReturnListOfAirports() {
        Airport airport1 = new Airport();
        airport1.setName("WAW");
        Airport airport2 = new Airport();
        airport2.setName("JFK");
        List<Airport> expectedAirports = Arrays.asList(airport1, airport2);
        when(airportRepository.findAll()).thenReturn(expectedAirports);

        List<Airport> actualAirports = airportService.getAllAirports();

        assertThat(actualAirports).hasSize(2);
        assertThat(actualAirports).containsExactlyInAnyOrder(airport1, airport2);
        verify(airportRepository).findAll();
    }

    @Test
    void getAllAirports_shouldReturnEmptyListWhenNoAirports() {
        when(airportRepository.findAll()).thenReturn(Collections.emptyList());

        List<Airport> actualAirports = airportService.getAllAirports();

        assertThat(actualAirports).isEmpty();
        verify(airportRepository).findAll();
    }
}
