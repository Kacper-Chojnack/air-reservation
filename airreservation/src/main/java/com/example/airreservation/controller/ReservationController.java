package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.flight.Flight;
import com.example.airreservation.model.lock.TemporarySeatLock;
import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.reservation.ReservationDTO;
import com.example.airreservation.repository.PassengerRepository;
import com.example.airreservation.service.FlightService;
import com.example.airreservation.service.ReservationService;
import com.example.airreservation.service.SeatLockService;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final FlightService flightService;
    private final PassengerRepository passengerRepository;
    private final SeatLockService seatLockService;
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private void populateCreateModel(Model model, Long flightId, ReservationDTO reservationDTO) {
        var flight = flightService.getFlightById(flightId);
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("flightId", flightId);
        model.addAttribute("departureAirport", flight.getDepartureAirport().getName());
        model.addAttribute("arrivalAirport", flight.getArrivalAirport().getName());
        model.addAttribute("departureDate", flight.getDepartureDate());
        model.addAttribute("flightDuration", flight.getFlightDuration());
        model.addAttribute("flightNumber", flight.getFlightNumber());
        model.addAttribute("availableSeats", flightService.getAvailableSeats(flightId));
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long flightId, Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        var flight = flightService.getFlightById(flightId);
        if (flight.isDeparted() || flight.isCompleted()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można zarezerwować – lot jest już niedostępny.");
            return "redirect:/";
        }

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setFlightId(flightId);
        populateCreateModel(model, flightId, reservationDTO);
        return "reservations/create";
    }

    @PostMapping("/acquire-lock")
    public String acquireSeatLock(
            @ModelAttribute("reservationDTO") ReservationDTO reservationDTO,
            BindingResult bindingResult,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (reservationDTO.getSeatNumber() == null) {
            redirectAttributes.addFlashAttribute("error", "Musisz wybrać miejsce.");

            return "redirect:/reservations/create?flightId=" + reservationDTO.getFlightId();
        }


        String userEmail = authentication.getName();
        Passenger loggedInPassenger = passengerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono zalogowanego pasażera"));

        Long flightId = reservationDTO.getFlightId();
        Integer seatNumber = reservationDTO.getSeatNumber();

        try {

            TemporarySeatLock lock = seatLockService.acquireLock(flightId, seatNumber, loggedInPassenger.getId());


            session.setAttribute("seatLockId", lock.getId());
            session.setAttribute("seatLockExpiresAt", lock.getExpiresAt());
            session.setAttribute("seatLockFlightId", flightId);
            session.setAttribute("seatLockSeatNumber", seatNumber);


            logger.info("Uzyskano blokadę ID {} dla miejsca {} na locie {}, wygasa: {}", lock.getId(), seatNumber, flightId, lock.getExpiresAt());


            return "redirect:/reservations/confirm-password";

        } catch (BusinessException e) {
            logger.warn("Nie udało się uzyskać blokady dla miejsca {} lotu {}: {}", seatNumber, flightId, e.getReason());

            redirectAttributes.addFlashAttribute("error", e.getReason());

            return "redirect:/reservations/create?flightId=" + flightId;
        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas próby blokady miejsca {} lotu {}", seatNumber, flightId, e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił nieoczekiwany błąd serwera.");
            return "redirect:/reservations/create?flightId=" + flightId;
        }
    }


    @GetMapping("/confirm-password")
    public String showConfirmPasswordPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long lockId = (Long) session.getAttribute("seatLockId");
        LocalDateTime expiresAt = (LocalDateTime) session.getAttribute("seatLockExpiresAt");
        Long flightId = (Long) session.getAttribute("seatLockFlightId");
        Integer seatNumber = (Integer) session.getAttribute("seatLockSeatNumber");

        if (lockId == null || expiresAt == null || flightId == null || seatNumber == null) {
            logger.warn("Próba dostępu do /confirm-password bez aktywnej blokady w sesji.");
            redirectAttributes.addFlashAttribute("error", "Brak aktywnej blokady miejsca lub sesja wygasła. Spróbuj ponownie wybrać miejsce.");

            return "redirect:/";
        }


        if (expiresAt.isBefore(LocalDateTime.now())) {
            logger.warn("Próba dostępu do /confirm-password z wygasłą blokadą (ID: {}) w sesji.", lockId);


            session.removeAttribute("seatLockId");
            session.removeAttribute("seatLockExpiresAt");
            session.removeAttribute("seatLockFlightId");
            session.removeAttribute("seatLockSeatNumber");
            redirectAttributes.addFlashAttribute("error", "Czas na potwierdzenie rezerwacji minął. Miejsce zostało zwolnione.");
            return "redirect:/reservations/create?flightId=" + flightId;
        }


        long expiresAtMillis = expiresAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        model.addAttribute("expiresAtMillis", expiresAtMillis);


        model.addAttribute("lockId", lockId);

        try {
            Flight flight = flightService.getFlightById(flightId);
            model.addAttribute("flightInfo", flight);
        } catch (Exception e) {
            logger.error("Nie udało się pobrać lotu {} dla strony potwierdzenia hasła", flightId, e);

            redirectAttributes.addFlashAttribute("error", "Nie można załadować informacji o locie.");
            return "redirect:/";
        }

        model.addAttribute("seatNumber", seatNumber);
        model.addAttribute("passwordConfirmDTO", new PasswordConfirmDTO());

        return "reservations/confirm-password";
    }


    @PostMapping("/finalize")
    public String finalizeReservation(
            @RequestParam("lockId") Long lockId,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {


        Long sessionLockId = (Long) session.getAttribute("seatLockId");
        if (sessionLockId == null || !sessionLockId.equals(lockId)) {
            logger.warn("Próba finalizacji z lockId ({}) niezgodnym z sesją ({})", lockId, sessionLockId);
            redirectAttributes.addFlashAttribute("error", "Błąd sesji lub nieprawidłowa blokada.");
            return "redirect:/";
        }


        try {

            seatLockService.finalizeReservationWithPassword(lockId, password);


            session.removeAttribute("seatLockId");
            session.removeAttribute("seatLockExpiresAt");
            session.removeAttribute("seatLockFlightId");
            session.removeAttribute("seatLockSeatNumber");


            redirectAttributes.addFlashAttribute("successMessage", "Rezerwacja została pomyślnie utworzona!");
            return "redirect:/profile";

        } catch (BusinessException e) {
            logger.warn("Błąd biznesowy podczas finalizacji rezerwacji dla blokady {}: {}", lockId, e.getReason());
            redirectAttributes.addFlashAttribute("error", e.getReason());

            if (e.getErrorType() != ErrorType.LOCK_EXPIRED && e.getErrorType() != ErrorType.LOCK_NOT_FOUND) {

                return "redirect:/reservations/confirm-password";
            } else {

                session.removeAttribute("seatLockId");
                session.removeAttribute("seatLockExpiresAt");
                session.removeAttribute("seatLockFlightId");
                session.removeAttribute("seatLockSeatNumber");

                return "redirect:/reservations/create?flightId=" + session.getAttribute("seatLockFlightId");
            }

        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas finalizacji rezerwacji dla blokady {}", lockId, e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił nieoczekiwany błąd serwera podczas finalizacji.");

            session.removeAttribute("seatLockId");
            session.removeAttribute("seatLockExpiresAt");
            session.removeAttribute("seatLockFlightId");
            session.removeAttribute("seatLockSeatNumber");
            return "redirect:/";
        }
    }
}


@Getter
@Setter
class PasswordConfirmDTO {
    private String password;
}