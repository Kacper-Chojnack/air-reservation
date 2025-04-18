package com.example.airreservation.controller;

import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FlightService flightService;

    @GetMapping("/")
    public String index(Model model) {
        List<Flight> flights = flightService.getAvailableFlights(LocalDateTime.now());
        model.addAttribute("flights", flights);
        return "index";
    }

}