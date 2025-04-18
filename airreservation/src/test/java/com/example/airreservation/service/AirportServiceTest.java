package com.example.airreservation.service;

import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.repository.AirportRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AirportServiceTest {

    private final AirportRepository airportRepository = mock(AirportRepository.class);
    private final AirportService airportService = new AirportService(airportRepository);

    @Test
    void getAllAirports_returnsAllAirports() {
        List<Airport> airports = List.of(new Airport(), new Airport());
        when(airportRepository.findAll()).thenReturn(airports);

        List<Airport> result = airportService.getAllAirports();

        assertEquals(2, result.size());
        verify(airportRepository, times(1)).findAll();
    }
}
