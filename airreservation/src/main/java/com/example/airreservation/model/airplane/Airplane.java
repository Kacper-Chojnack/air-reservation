package com.example.airreservation.model.airplane;

import com.example.airreservation.model.flight.Flight;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Entity
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int totalSeats;

    @ToString.Exclude
    @OneToMany(mappedBy = "airplane")
    private List<Flight> flights; // Exclude to prevent circular reference

}
