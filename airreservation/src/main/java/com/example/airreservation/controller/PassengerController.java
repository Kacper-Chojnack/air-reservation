package com.example.airreservation.controller;

import com.example.airreservation.exceptionHandler.BusinessException;
import com.example.airreservation.exceptionHandler.ErrorType;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;
    private static final Logger logger = LoggerFactory.getLogger(PassengerController.class);


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("passengerDTO", new PassengerDTO());
        return "passengers/create";
    }

    @PostMapping
    public String createPassenger(
            @Valid @ModelAttribute("passengerDTO") PassengerDTO passengerDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {


        if (passengerDTO.getPassword() != null && !passengerDTO.getPassword().equals(passengerDTO.getMatchingPassword())) {
            bindingResult.rejectValue("matchingPassword", "error.passengerDTO", "Hasła muszą być identyczne");
        }


        if (!bindingResult.hasFieldErrors("email") && passengerService.existsByEmail(passengerDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.passengerDTO", "Podany adres email jest już zajęty.");
        }
        if (!bindingResult.hasFieldErrors("phoneNumber") && passengerService.existsByPhoneNumber(passengerDTO.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "error.passengerDTO", "Podany numer telefonu jest już zajęty.");
        }

        if (bindingResult.hasErrors()) {
            return "passengers/create";
        }


        try {

            passengerService.registerNewPassenger(passengerDTO);

            redirectAttributes.addFlashAttribute("registrationSuccessMessage",
                    "Rejestracja prawie zakończona! Sprawdź swój email (" + passengerDTO.getEmail() + ") i kliknij link aktywacyjny.");
            return "redirect:/login";
        } catch (BusinessException e) {

            if (e.getErrorType() == ErrorType.EMAIL_ALREADY_EXISTS) {
                bindingResult.rejectValue("email", "error.passengerDTO", e.getReason());
            } else if (e.getErrorType() == ErrorType.PHONE_NUMBER_ALREADY_EXISTS) {
                bindingResult.rejectValue("phoneNumber", "error.passengerDTO", e.getReason());
            } else {
                model.addAttribute("errorMessage", "Wystąpił błąd biznesowy: " + e.getReason());
            }
            return "passengers/create";
        } catch (Exception e) {
            logger.error("Error during passenger registration", e);
            model.addAttribute("errorMessage", "Wystąpił nieoczekiwany błąd podczas rejestracji.");
            return "passengers/create";
        }
    }
}