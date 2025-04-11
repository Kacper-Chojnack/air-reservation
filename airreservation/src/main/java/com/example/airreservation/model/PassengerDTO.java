package com.example.airreservation.model;

import jakarta.persistence.Id;

import java.util.List;

public class PassengerDTO {

    @Id
    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String password;
    private List<Reservation> reservations;

}
