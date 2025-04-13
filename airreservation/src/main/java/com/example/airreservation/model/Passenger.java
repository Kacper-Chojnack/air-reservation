package com.example.airreservation.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String password;

    @OneToMany(mappedBy = "passenger")
    private List<Reservation> reservations = new ArrayList<>();
}
