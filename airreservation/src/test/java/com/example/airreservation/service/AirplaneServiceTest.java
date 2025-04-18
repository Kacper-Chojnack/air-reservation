package com.example.airreservation.service;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.repository.AirplaneRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AirplaneServiceTest {

    private final AirplaneRepository airplaneRepository = mock(AirplaneRepository.class);
    private final AirplaneService airplaneService = new AirplaneService(airplaneRepository);

    @Test
    void getAllAirplanes_returnsAllAirplanes() {
        List<Airplane> airplanes = List.of(new Airplane(), new Airplane(), new Airplane());
        when(airplaneRepository.findAll()).thenReturn(airplanes);

        List<Airplane> result = airplaneService.getAllAirplanes();

        assertEquals(3, result.size());
        verify(airplaneRepository, times(1)).findAll();
    }
}
