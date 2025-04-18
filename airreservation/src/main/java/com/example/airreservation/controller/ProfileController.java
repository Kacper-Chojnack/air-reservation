package com.example.airreservation.controller;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.Reservation;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller("userProfileController")
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Passenger passenger = passengerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono pasażera: " + email));

        List<Reservation> allReservations = reservationRepository.findByPassengerIdOrderByFlightDepartureDateDesc(passenger.getId());

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> futureReservations = allReservations.stream()
                .filter(r -> r.getFlight() != null && r.getFlight().getDepartureDate().isAfter(now))
                .collect(Collectors.toList());

        List<Reservation> pastReservations = allReservations.stream()
                .filter(r -> r.getFlight() != null && r.getFlight().getDepartureDate().isBefore(now))
                .collect(Collectors.toList());

        model.addAttribute("passenger", passenger);
        model.addAttribute("futureReservations", futureReservations);
        model.addAttribute("pastReservations", pastReservations);

        return "profile";
    }
}