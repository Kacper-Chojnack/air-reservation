package com.example.airreservation.service;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.repository.AirplaneRepository;
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
class AirplaneServiceTest {

    @Mock
    private AirplaneRepository airplaneRepository;

    @InjectMocks
    private AirplaneService airplaneService;

    @Test
    void getAllAirplanes_shouldReturnListOfAirplanes() {
        Airplane plane1 = new Airplane("Boeing 737", 180);
        Airplane plane2 = new Airplane("Airbus A320", 150);
        List<Airplane> expectedAirplanes = Arrays.asList(plane1, plane2);
        when(airplaneRepository.findAll()).thenReturn(expectedAirplanes);

        List<Airplane> actualAirplanes = airplaneService.getAllAirplanes();

        assertThat(actualAirplanes).hasSize(2);
        assertThat(actualAirplanes).containsExactlyInAnyOrder(plane1, plane2);
        verify(airplaneRepository).findAll();
    }

    @Test
    void getAllAirplanes_shouldReturnEmptyListWhenNoAirplanes() {
        when(airplaneRepository.findAll()).thenReturn(Collections.emptyList());

        List<Airplane> actualAirplanes = airplaneService.getAllAirplanes();

        assertThat(actualAirplanes).isEmpty();
        verify(airplaneRepository).findAll();
    }
}