package com.example.airreservation.model.passenger;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

@Data
public class PassengerDTO {

    @NotBlank(message = "Imię jest wymagane")
    private String name;

    @NotBlank(message = "Nazwisko jest wymagane")
    private String surname;

    @NotBlank(message = "Numer jest wymagany")
    @Pattern(regexp = "\\+?[0-9]{9,15}", message = "Nieprawidłowy format numeru telefonu")
    private String phoneNumber;

    @Email(message = "Nieprawidłowy format email")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków")
    private String password;

    // Added matchingPassword field
    @NotBlank(message = "Potwierdzenie hasła jest wymagane")
    private String matchingPassword;

}
