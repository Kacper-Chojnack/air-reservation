package com.example.airreservation.model.passenger;

import com.example.airreservation.model.reservation.Reservation;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"reservations"})
@EqualsAndHashCode(exclude = {"reservations"})
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phoneNumber")
})
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String password;
    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Column(nullable = false)
    private boolean enabled = false;

    private String confirmationToken;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime tokenExpiryDate;

    @Transient
    public String getFullName() {
        return (name != null ? name : "") + " " + (surname != null ? surname : "");
    }
}
