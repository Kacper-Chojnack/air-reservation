package com.example.airreservation.init;

import com.example.airreservation.model.airplane.Airplane;
import com.example.airreservation.model.airport.Airport;
import com.example.airreservation.model.country.Country;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.schedule.RecurringFlightSchedule;
import com.example.airreservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CountryRepository countryRepository;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;
    private final PassengerRepository passengerRepository;
    private final RecurringFlightScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FlightRepository flightRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Uruchamianie inicjalizacji danych...");

        boolean countriesAdded = false;
        if (countryRepository.count() == 0) {
            logger.info("Brak krajów w bazie. Dodawanie danych początkowych...");
            List<Country> countries = Arrays.asList(
                    new Country("Polska"), new Country("Niemcy"), new Country("Francja"),
                    new Country("Hiszpania"), new Country("Włochy"), new Country("Wielka Brytania"),
                    new Country("Stany Zjednoczone"), new Country("Holandia"), new Country("Turcja"),
                    new Country("Grecja"), new Country("Egipt"), new Country("Zjednoczone Emiraty Arabskie"),
                    new Country("Tajlandia"), new Country("Chiny"), new Country("Japonia"),
                    new Country("Kanada"), new Country("Austria"), new Country("Czechy"),
                    new Country("Szwecja"), new Country("Norwegia")
            );
            countryRepository.saveAll(countries);
            countriesAdded = true;
            logger.info("Dodano {} krajów.", countries.size());
        } else {
            logger.info("Kraje już istnieją w bazie. Pomijanie dodawania krajów.");
        }

        boolean airportsAdded = false;
        if (airportRepository.count() == 0) {
            logger.info("Brak lotnisk w bazie. Dodawanie danych początkowych...");
            Map<String, Country> countryMap = countryRepository.findAll().stream()
                    .collect(Collectors.toMap(Country::getName, Function.identity()));

            List<Airport> airports = Arrays.asList(
                    createAirport("Lotnisko Chopina w Warszawie", "Polska", countryMap),
                    createAirport("Lotnisko Kraków-Balice", "Polska", countryMap),
                    createAirport("Lotnisko Gdańsk im. Lecha Wałęsy", "Polska", countryMap),
                    createAirport("Lotnisko Katowice-Pyrzowice", "Polska", countryMap),
                    createAirport("Lotnisko Wrocław-Strachowice", "Polska", countryMap),
                    createAirport("Lotnisko Poznań-Ławica", "Polska", countryMap),
                    createAirport("Lotnisko Rzeszów-Jasionka", "Polska", countryMap),
                    createAirport("Lotnisko Frankfurt", "Niemcy", countryMap),
                    createAirport("Lotnisko Berlin-Brandenburg", "Niemcy", countryMap),
                    createAirport("Lotnisko Paryż-Charles de Gaulle", "Francja", countryMap),
                    createAirport("Lotnisko Madryt-Barajas", "Hiszpania", countryMap),
                    createAirport("Lotnisko Rzym-Fiumicino", "Włochy", countryMap),
                    createAirport("Lotnisko Londyn-Heathrow", "Wielka Brytania", countryMap),
                    createAirport("Lotnisko Manchester", "Wielka Brytania", countryMap),
                    createAirport("Lotnisko Amsterdam-Schiphol", "Holandia", countryMap),
                    createAirport("Lotnisko Stambuł", "Turcja", countryMap),
                    createAirport("Lotnisko Ateny", "Grecja", countryMap),
                    createAirport("Lotnisko JFK Nowy Jork", "Stany Zjednoczone", countryMap),
                    createAirport("Lotnisko Toronto Pearson", "Kanada", countryMap),
                    createAirport("Lotnisko Wiedeń-Schwechat", "Austria", countryMap),
                    createAirport("Lotnisko Praga im. Václava Havla", "Czechy", countryMap),
                    createAirport("Lotnisko Sztokholm-Arlanda", "Szwecja", countryMap),
                    createAirport("Lotnisko Oslo-Gardermoen", "Norwegia", countryMap)
            ).stream().filter(java.util.Objects::nonNull).collect(Collectors.toList());

            if (!airports.isEmpty()) {
                airportRepository.saveAll(airports);
                airportsAdded = true;
                logger.info("Dodano {} lotnisk.", airports.size());
            }
        } else {
            logger.info("Lotniska już istnieją w bazie. Pomijanie dodawania lotnisk.");
        }

        boolean airplanesAdded = false;
        if (airplaneRepository.count() == 0) {
            logger.info("Brak samolotów w bazie. Dodawanie danych początkowych...");
            List<Airplane> airplanes = Arrays.asList(
                    new Airplane("Boeing 737", 189),
                    new Airplane("Airbus A320", 180),
                    new Airplane("Boeing 777", 396),
                    new Airplane("Airbus A380", 853),
                    new Airplane("Boeing 787 Dreamliner", 335),
                    new Airplane("Embraer E195", 124)
            );
            airplaneRepository.saveAll(airplanes);
            airplanesAdded = true;
            logger.info("Dodano {} samolotów.", airplanes.size());
        } else {
            logger.info("Samoloty już istnieją w bazie. Pomijanie dodawania samolotów.");
        }

        String adminEmail = "admin";
        if (passengerRepository.findByEmail(adminEmail).isEmpty()) {
            logger.info("Brak użytkownika admina. Tworzenie...");
            Passenger admin = new Passenger();
            admin.setName("Admin");
            admin.setSurname("Adminowski");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setPhoneNumber("000000000");
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setConfirmationToken(null);
            admin.setTokenExpiryDate(null);
            passengerRepository.save(admin);
            logger.info("Utworzono użytkownika admina: {}", adminEmail);
        } else {
            logger.info("Użytkownik admin ({}) już istnieje.", adminEmail);
        }

        List<RecurringFlightSchedule> savedSchedules = new ArrayList<>();
        if (scheduleRepository.count() == 0) {
            logger.info("Brak harmonogramów cyklicznych. Dodawanie danych początkowych...");
            Map<String, Airport> airportMap = airportRepository.findAll().stream()
                    .collect(Collectors.toMap(Airport::getName, Function.identity()));
            Map<String, Airplane> airplaneMap = airplaneRepository.findAll().stream()
                    .collect(Collectors.toMap(Airplane::getName, Function.identity()));

            List<RecurringFlightSchedule> schedulesToCreate = Arrays.asList(
                    createSchedule("Lotnisko Chopina w Warszawie", "Lotnisko Londyn-Heathrow", "Boeing 737", DayOfWeek.MONDAY, "08:00", Duration.ofHours(2).plusMinutes(30), "LO", airportMap, airplaneMap),
                    createSchedule("Lotnisko Londyn-Heathrow", "Lotnisko Chopina w Warszawie", "Boeing 737", DayOfWeek.TUESDAY, "11:00", Duration.ofHours(2).plusMinutes(30), "LO", airportMap, airplaneMap),
                    createSchedule("Lotnisko Manchester", "Lotnisko Chopina w Warszawie", "Airbus A320", DayOfWeek.FRIDAY, "15:00", Duration.ofHours(2).plusMinutes(20), "WZZ", airportMap, airplaneMap),
                    createSchedule("Lotnisko Manchester", "Lotnisko Chopina w Warszawie", "Airbus A320", DayOfWeek.THURSDAY, "17:00", Duration.ofHours(2).plusMinutes(20), "WZZ", airportMap, airplaneMap),
                    createSchedule("Lotnisko Kraków-Balice", "Lotnisko Rzym-Fiumicino", "Boeing 737", DayOfWeek.SATURDAY, "06:30", Duration.ofHours(1).plusMinutes(55), "RYR", airportMap, airplaneMap),
                    createSchedule("Lotnisko Rzym-Fiumicino", "Lotnisko Kraków-Balice", "Boeing 737", DayOfWeek.SUNDAY, "09:00", Duration.ofHours(1).plusMinutes(55), "RYR", airportMap, airplaneMap)
            ).stream().filter(java.util.Objects::nonNull).collect(Collectors.toList());

            if (!schedulesToCreate.isEmpty()) {
                savedSchedules = scheduleRepository.saveAll(schedulesToCreate);

                if (!savedSchedules.isEmpty() && flightRepository.count() == 0) {
                    logger.info("Brak lotów w bazie. Generowanie lotów początkowych na podstawie nowych harmonogramów...");
                    generateInitialFlights(savedSchedules, 7);
                }
            }
        } else {
            logger.info("Harmonogramy cykliczne już istnieją. Pomijanie dodawania.");
        }

        logger.info("Inicjalizacja danych zakończona.");
    }

    private Airport createAirport(String name, String countryName, Map<String, Country> countryMap) {
        Country country = countryMap.get(countryName);
        if (country == null) {
            logger.warn("Nie znaleziono kraju '{}' podczas tworzenia lotniska '{}'.", countryName, name);
            return null;
        }
        Airport airport = new Airport();
        airport.setName(name);
        airport.setCountry(country);
        return airport;
    }

    private RecurringFlightSchedule createSchedule(String depAirportName, String arrAirportName, String airplaneName,
                                                   DayOfWeek day, String time, Duration duration, String prefix,
                                                   Map<String, Airport> airportMap, Map<String, Airplane> airplaneMap) {
        Airport dep = airportMap.get(depAirportName);
        Airport arr = airportMap.get(arrAirportName);
        Airplane plane = airplaneMap.get(airplaneName);

        if (dep == null || arr == null || plane == null) {
            logger.warn("Nie można utworzyć harmonogramu: brakujące lotnisko ({}, {}) lub samolot ({}).", depAirportName, arrAirportName, airplaneName);
            return null;
        }

        RecurringFlightSchedule schedule = new RecurringFlightSchedule();
        schedule.setDepartureAirport(dep);
        schedule.setArrivalAirport(arr);
        schedule.setAirplane(plane);
        schedule.setDayOfWeek(day);
        schedule.setDepartureTime(LocalTime.parse(time));
        schedule.setFlightDuration(duration);
        schedule.setFlightNumberPrefix(prefix);
        schedule.setGenerateMonthsAhead(3);
        schedule.setActive(true);
        return schedule;
    }

    private void generateInitialFlights(List<RecurringFlightSchedule> schedules, int daysAhead) {
        List<Flight> initialFlights = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        int flightsGenerated = 0;

        for (RecurringFlightSchedule schedule : schedules) {
            LocalDate currentDate = today;
            while (!currentDate.isAfter(endDate)) {
                if (currentDate.getDayOfWeek() == schedule.getDayOfWeek()) {
                    LocalDateTime departureDateTime = currentDate.atTime(schedule.getDepartureTime());

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

                    initialFlights.add(newFlight);
                    flightsGenerated++;
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        if (!initialFlights.isEmpty()) {
            flightRepository.saveAll(initialFlights);
            logger.info("Wygenerowano i zapisano {} lotów początkowych.", flightsGenerated);
        }
    }
}