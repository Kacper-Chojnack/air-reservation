package com.example.airreservation.scheduler;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.schedule.RecurringFlightSchedule;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.RecurringFlightScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringFlightGeneratorSchedulerTest {

    @Mock
    private RecurringFlightScheduleRepository scheduleRepository;
    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private RecurringFlightGeneratorScheduler recurringFlightGeneratorScheduler;

    private RecurringFlightSchedule schedule;

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

        schedule = new RecurringFlightSchedule();
        schedule.setId(1L);
        schedule.setDepartureAirport(dep);
        schedule.setArrivalAirport(arr);
        schedule.setAirplane(plane);
        schedule.setDayOfWeek(LocalDateTime.now().getDayOfWeek());
        schedule.setDepartureTime(LocalTime.of(10, 0));
        schedule.setFlightDuration(Duration.ofHours(8));
        schedule.setFlightNumberPrefix("AA");
        schedule.setGenerateMonthsAhead(1);
        schedule.setActive(true);
    }

    @Test
    void generateRecurringFlights_shouldGenerateFlightWhenDoesNotExist() {
        when(scheduleRepository.findByActiveTrue()).thenReturn(Collections.singletonList(schedule));
        when(flightRepository.existsByDepartureAirportAndArrivalAirportAndDepartureDate(
                eq(schedule.getDepartureAirport()),
                eq(schedule.getArrivalAirport()),
                any(LocalDateTime.class))
        ).thenReturn(false); // Flight does not exist

        recurringFlightGeneratorScheduler.generateRecurringFlights();

        ArgumentCaptor<Flight> flightCaptor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository, atLeastOnce()).save(flightCaptor.capture()); // Verify save was called at least once

        Flight generatedFlight = flightCaptor.getValue(); // Check one of the generated flights
        assertThat(generatedFlight.getDepartureAirport()).isEqualTo(schedule.getDepartureAirport());
        assertThat(generatedFlight.getArrivalAirport()).isEqualTo(schedule.getArrivalAirport());
        assertThat(generatedFlight.getAirplane()).isEqualTo(schedule.getAirplane());
        assertThat(generatedFlight.getDepartureDate().toLocalTime()).isEqualTo(schedule.getDepartureTime());
        assertThat(generatedFlight.getDepartureDate().getDayOfWeek()).isEqualTo(schedule.getDayOfWeek());
        assertThat(generatedFlight.getFlightNumber()).startsWith("AA-");
    }

    @Test
    void generateRecurringFlights_shouldSkipFlightWhenExists() {
        when(scheduleRepository.findByActiveTrue()).thenReturn(Collections.singletonList(schedule));
        when(flightRepository.existsByDepartureAirportAndArrivalAirportAndDepartureDate(
                eq(schedule.getDepartureAirport()),
                eq(schedule.getArrivalAirport()),
                any(LocalDateTime.class))
        ).thenReturn(true); // Flight exists

        recurringFlightGeneratorScheduler.generateRecurringFlights();

        verify(flightRepository, never()).save(any(Flight.class)); // Save should not be called
    }

    @Test
    void generateRecurringFlights_shouldHandleEmptyScheduleList() {
        when(scheduleRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        recurringFlightGeneratorScheduler.generateRecurringFlights();

        verify(flightRepository, never()).existsByDepartureAirportAndArrivalAirportAndDepartureDate(any(), any(), any());
        verify(flightRepository, never()).save(any(Flight.class));
    }
}
