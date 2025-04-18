package com.example.airreservation.controller;

import com.example.airreservation.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PassengerService passengerService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/confirm")
    public String confirmRegistration(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean success = passengerService.confirmUser(token);

        if (success) {
            redirectAttributes.addFlashAttribute("confirmationSuccess", "Twoje konto zostało pomyślnie aktywowane! Możesz się teraz zalogować.");
        } else {
            redirectAttributes.addFlashAttribute("confirmationError", "Link aktywacyjny jest nieprawidłowy lub wygasł.");
        }

        return "redirect:/login";
    }
}
