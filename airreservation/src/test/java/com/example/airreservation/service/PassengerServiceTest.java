package com.example.airreservation.service;

import com.example.airreservation.model.passenger.Passenger;
import com.example.airreservation.model.passenger.PassengerDTO;
import com.example.airreservation.model.passenger.PassengerMapper;
import com.example.airreservation.repository.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerService passengerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test poprawnego zapisu pasażera
    @Test
    void testSavePassenger_Success() {
        // Przygotowanie danych wejściowych
        PassengerDTO dto = new PassengerDTO();
        dto.setEmail("test@example.com");
        dto.setPhoneNumber("123456789");
        dto.setName("John");
        dto.setSurname("Doe");

        // Mapowanie DTO -> encja
        Passenger passenger = new Passenger();
        passenger.setEmail("test@example.com");
        passenger.setPhoneNumber("123456789");
        passenger.setName("John");
        passenger.setSurname("Doe");

        // Ustawienie zachowania mappera i repozytorium
        when(passengerMapper.PassengerDTOToPassenger(dto)).thenReturn(passenger);
        when(passengerRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passengerRepository.existsByPhoneNumber("123456789")).thenReturn(false);
        when(passengerRepository.save(passenger)).thenReturn(passenger);
        when(passengerMapper.PassengerToPassengerDTO(passenger)).thenReturn(dto);

        // Wywołanie metody
        PassengerDTO savedDto = passengerService.savePassenger(dto);

        // Weryfikacja
        assertNotNull(savedDto);
        assertEquals("test@example.com", savedDto.getEmail());
        verify(passengerRepository, times(1)).existsByEmail("test@example.com");
        verify(passengerRepository, times(1)).existsByPhoneNumber("123456789");
        verify(passengerRepository, times(1)).save(passenger);
    }

    // Test: email już istnieje
    @Test
    void testSavePassenger_EmailAlreadyExists() {
        PassengerDTO dto = new PassengerDTO();
        dto.setEmail("duplicate@example.com");
        dto.setPhoneNumber("123456789");
        dto.setName("Jane");
        dto.setSurname("Doe");

        Passenger passenger = new Passenger();
        passenger.setEmail("duplicate@example.com");
        passenger.setPhoneNumber("123456789");
        passenger.setName("Jane");
        passenger.setSurname("Doe");

        when(passengerMapper.PassengerDTOToPassenger(dto)).thenReturn(passenger);
        when(passengerRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        // Oczekujemy, że wywołanie metody rzuci wyjątkiem EmailAlreadyExistsException
        assertThrows(EmailAlreadyExistsException.class, () -> passengerService.savePassenger(dto));
        verify(passengerRepository, times(1)).existsByEmail("duplicate@example.com");
        verify(passengerRepository, never()).existsByPhoneNumber(any());
        verify(passengerRepository, never()).save(any());
    }

    // Test: numer telefonu już istnieje
    @Test
    void testSavePassenger_PhoneNumberAlreadyExists() {
        PassengerDTO dto = new PassengerDTO();
        dto.setEmail("unique@example.com");
        dto.setPhoneNumber("987654321");
        dto.setName("Alice");
        dto.setSurname("Smith");

        Passenger passenger = new Passenger();
        passenger.setEmail("unique@example.com");
        passenger.setPhoneNumber("987654321");
        passenger.setName("Alice");
        passenger.setSurname("Smith");

        when(passengerMapper.PassengerDTOToPassenger(dto)).thenReturn(passenger);
        when(passengerRepository.existsByEmail("unique@example.com")).thenReturn(false);
        when(passengerRepository.existsByPhoneNumber("987654321")).thenReturn(true);

        // Oczekujemy, że metoda rzuci PhoneNumberAlreadyExistsException
        assertThrows(PhoneNumberAlreadyExistsException.class, () -> passengerService.savePassenger(dto));
        verify(passengerRepository, times(1)).existsByEmail("unique@example.com");
        verify(passengerRepository, times(1)).existsByPhoneNumber("987654321");
        verify(passengerRepository, never()).save(any());
    }

    // Test pobierania wszystkich pasażerów
    @Test
    void testGetAllPassengers() {
        List<Passenger> passengers = List.of(new Passenger(), new Passenger());
        when(passengerRepository.findAll()).thenReturn(passengers);

        List<Passenger> result = passengerService.getAllPassengers();
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(passengerRepository, times(1)).findAll();
    }
}
