package com.example.airreservation.controller;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.service.AirportService;
import com.example.airreservation.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FlightService flightService;
    private final AirportService airportService;

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @PageableDefault(size = 10, sort = "departureDate") Pageable pageable,
            Model model) {

        Page<Flight> flightPage = null;


        if (departureAirportId != null && departureDate != null) {
            try {
                flightPage = flightService.searchFlights(departureAirportId, arrivalAirportId, departureDate, pageable);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Wystąpił błąd podczas wyszukiwania lotów.");
            }
        }


        List<Flight> upcomingFlights = Collections.emptyList();
        try {
            upcomingFlights = flightService.getUpcomingFlights(5);
        } catch (Exception e) {


            model.addAttribute("upcomingFlightsError", "Nie udało się załadować nadchodzących lotów.");
        }


        model.addAttribute("flightPage", flightPage);
        model.addAttribute("upcomingFlights", upcomingFlights);
        model.addAttribute("airports", airportService.getAllAirports());
        model.addAttribute("searchDepartureAirportId", departureAirportId);
        model.addAttribute("searchArrivalAirportId", arrivalAirportId);
        model.addAttribute("searchDepartureDate", departureDate);

        return "index";
    }
}