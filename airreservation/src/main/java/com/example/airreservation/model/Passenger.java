package com.example.airreservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Passenger {
    @Id
    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String password;

    @OneToMany(mappedBy = "passenger")
    private List<Reservation> reservations;
}
