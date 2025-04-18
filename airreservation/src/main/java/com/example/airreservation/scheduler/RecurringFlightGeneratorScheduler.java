package com.example.airreservation.scheduler;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.schedule.RecurringFlightSchedule;
import com.example.airreservation.repository.FlightRepository;
import com.example.airreservation.repository.RecurringFlightScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecurringFlightGeneratorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringFlightGeneratorScheduler.class);

    private final RecurringFlightScheduleRepository scheduleRepository;
    private final FlightRepository flightRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void generateRecurringFlights() {
        logger.info("GENERATOR: Rozpoczynanie generowania cyklicznych lotów...");
        LocalDate today = LocalDate.now();
        List<RecurringFlightSchedule> activeSchedules = scheduleRepository.findByActiveTrue();

        if (activeSchedules.isEmpty()) {
            logger.info("GENERATOR: Brak aktywnych harmonogramów cyklicznych lotów do przetworzenia.");
            return;
        }

        int flightsGenerated = 0;
        int flightsSkipped = 0;

        for (RecurringFlightSchedule schedule : activeSchedules) {
            logger.debug("GENERATOR: Przetwarzanie harmonogramu ID: {}, Trasa: {} -> {}",
                    schedule.getId(), schedule.getDepartureAirport().getName(), schedule.getArrivalAirport().getName());

            LocalDate generationEndDate = today.plusMonths(schedule.getGenerateMonthsAhead());
            LocalDate currentDate = today;

            while (!currentDate.isAfter(generationEndDate)) {
                if (currentDate.getDayOfWeek() == schedule.getDayOfWeek()) {
                    LocalDateTime departureDateTime = currentDate.atTime(schedule.getDepartureTime());

                    boolean exists = flightRepository.existsByDepartureAirportAndArrivalAirportAndDepartureDate(
                            schedule.getDepartureAirport(),
                            schedule.getArrivalAirport(),
                            departureDateTime
                    );

                    if (!exists) {
                        Flight newFlight = new Flight();
                        newFlight.setDepartureAirport(schedule.getDepartureAirport());
                        newFlight.setArrivalAirport(schedule.getArrivalAirport());
                        newFlight.setAirplane(schedule.getAirplane());
                        newFlight.setDepartureDate(departureDateTime);
                        newFlight.setFlightDuration(schedule.getFlightDuration());

                        String datePart = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        String timePart = schedule.getDepartureTime().format(DateTimeFormatter.ofPattern("HHmm"));
                        newFlight.setFlightNumber(String.format("%s-%s-%s", schedule.getFlightNumberPrefix(), datePart, timePart));
                        newFlight.setCompleted(false);
                        newFlight.setRoundTrip(false);

                        flightRepository.save(newFlight);
                        flightsGenerated++;
                        logger.debug("GENERATOR: Utworzono lot: {} na {}", newFlight.getFlightNumber(), newFlight.getDepartureDate());
                    } else {
                        flightsSkipped++;
                        logger.trace("GENERATOR: Pominięto istniejący lot: {} -> {} na {}", schedule.getDepartureAirport().getName(), schedule.getArrivalAirport().getName(), departureDateTime);
                    }
                }

                currentDate = currentDate.plusDays(1);
            }
        }

        logger.info("GENERATOR: Zakończono generowanie cyklicznych lotów. Utworzono: {}, Pominięto istniejących: {}", flightsGenerated, flightsSkipped);
    }
}
