package com.example.airreservation.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PassengerDTO {

    @NotBlank(message = "Imię jest wymagane")
    private String name;

    @NotBlank(message = "Nazwisko jest wymagane")
    private String surname;

    @NotBlank(message = "Numer jest wymagany")
    private String phoneNumber;

    @Email(message = "Nieprawidłowy format email")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    private String password;

}
