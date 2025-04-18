package com.example.airreservation.service;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.repository.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Passenger activePassenger;
    private Passenger disabledPassenger;

    @BeforeEach
    void setUp() {
        activePassenger = new Passenger();
        activePassenger.setId(1L);
        activePassenger.setEmail("active@user.com");
        activePassenger.setPassword("hashedPassword");
        activePassenger.setRole("ROLE_USER");
        activePassenger.setEnabled(true);

        disabledPassenger = new Passenger();
        disabledPassenger.setId(2L);
        disabledPassenger.setEmail("disabled@user.com");
        disabledPassenger.setPassword("hashedPassword2");
        disabledPassenger.setRole("ROLE_USER");
        disabledPassenger.setEnabled(false);
    }

    @Test
    void loadUserByUsername_shouldLoadActiveUserSuccessfully() {
        String email = "active@user.com";
        when(passengerRepository.findByEmail(email)).thenReturn(Optional.of(activePassenger));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).isEqualTo(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        verify(passengerRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_shouldLoadDisabledUserWithDisabledFlag() {
        String email = "disabled@user.com";
        when(passengerRepository.findByEmail(email)).thenReturn(Optional.of(disabledPassenger));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword2");
        assertThat(userDetails.isEnabled()).isFalse(); // Kluczowe sprawdzenie
        verify(passengerRepository).findByEmail(email);
    }


    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        String email = "nonexistent@example.com";
        when(passengerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Nie znaleziono użytkownika o emailu: " + email);
        verify(passengerRepository).findByEmail(email);
    }
}