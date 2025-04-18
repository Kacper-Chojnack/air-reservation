//package com.example.airreservation.service;
//
//import com.example.airreservation.exceptionHandler.BusinessException;
//import com.example.airreservation.exceptionHandler.ErrorType;
//import com.example.airreservation.model.passenger.Passenger;
//import com.example.airreservation.model.passenger.PassengerAdminEditDTO;
//import com.example.airreservation.model.passenger.PassengerDTO;
//import com.example.airreservation.model.passenger.PassengerMapper;
//import com.example.airreservation.repository.PassengerRepository;
//import com.example.airreservation.repository.ReservationRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.mail.MailException;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.thymeleaf.spring6.SpringTemplateEngine;
//import jakarta.mail.internet.MimeMessage;
//
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PassengerServiceTest {
//
//    @Mock
//    private PassengerRepository passengerRepository;
//    @Mock
//    private PassengerMapper passengerMapper;
//    @Mock
//    private PasswordEncoder passwordEncoder;
//    @Mock
//    private JavaMailSender mailSender;
//    @Mock
//    private SpringTemplateEngine templateEngine;
//    @Mock
//    private ReservationRepository reservationRepository;
//
//    @InjectMocks
//    private PassengerService passengerService;
//
//    private Passenger passenger;
//    private PassengerDTO passengerDTO;
//    private PassengerAdminEditDTO passengerAdminEditDTO;
//
//    @BeforeEach
//    void setUp() {
//        passengerDTO = new PassengerDTO();
//        passengerDTO.setName("Jan");
//        passengerDTO.setSurname("Kowalski");
//        passengerDTO.setEmail("jan.kowalski@example.com");
//        passengerDTO.setPassword("password123");
//        passengerDTO.setPhoneNumber("123456789");
//
//        passenger = new Passenger();
//        passenger.setId(1L);
//        passenger.setName("Jan");
//        passenger.setSurname("Kowalski");
//        passenger.setEmail("jan.kowalski@example.com");
//        passenger.setPassword("hashedPassword");
//        passenger.setPhoneNumber("123456789");
//        passenger.setRole("ROLE_USER");
//        passenger.setEnabled(false);
//        passenger.setConfirmationToken("test-token");
//        passenger.setTokenExpiryDate(LocalDateTime.now().plusHours(1));
//
//        passengerAdminEditDTO = new PassengerAdminEditDTO();
//        passengerAdminEditDTO.setId(1L);
//        passengerAdminEditDTO.setName("Jan Updated");
//        passengerAdminEditDTO.setSurname("Kowalski Updated");
//        passengerAdminEditDTO.setEmail("jan.updated@example.com");
//        passengerAdminEditDTO.setPhoneNumber("987654321");
//        passengerAdminEditDTO.setRole("ROLE_ADMIN");
//        passengerAdminEditDTO.setEnabled(true);
//        passengerAdminEditDTO.setNewPassword(null);
//    }
//
//    @Test
//    void registerNewPassenger_Success() {
//        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
//        when(passengerMapper.PassengerDTOToPassenger(any(PassengerDTO.class))).thenReturn(passenger);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//        when(passengerMapper.PassengerToPassengerDTO(any(Passenger.class))).thenReturn(passengerDTO);
//        doNothing().when(mailSender).send(any(org.springframework.mail.javamail.MimeMessage.class));
//        when(mailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));
//
//        PassengerDTO savedDto = passengerService.registerNewPassenger(passengerDTO);
//
//        assertThat(savedDto).isNotNull();
//        ArgumentCaptor<Passenger> passengerCaptor = ArgumentCaptor.forClass(Passenger.class);
//        verify(passengerRepository).save(passengerCaptor.capture());
//        Passenger capturedPassenger = passengerCaptor.getValue();
//        assertThat(capturedPassenger.getPassword()).isEqualTo("hashedPassword");
//        assertThat(capturedPassenger.getRole()).isEqualTo("ROLE_USER");
//        assertThat(capturedPassenger.isEnabled()).isFalse();
//        assertThat(capturedPassenger.getConfirmationToken()).isNotNull();
//        assertThat(capturedPassenger.getTokenExpiryDate()).isNotNull();
//        verify(templateEngine).process(eq("emails/confirmation-email"), any());
//        verify(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
//    }
//
//    @Test
//    void registerNewPassenger_EmailExists_ThrowsException() {
//        when(passengerRepository.existsByEmail(passengerDTO.getEmail())).thenReturn(true);
//
//        assertThatThrownBy(() -> passengerService.registerNewPassenger(passengerDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.EMAIL_ALREADY_EXISTS);
//        verify(passengerRepository, never()).save(any(Passenger.class));
//        verify(mailSender, never()).send(any(org.springframework.mail.javamail.MimeMessage.class));
//    }
//
//    @Test
//    void registerNewPassenger_PhoneNumberExists_ThrowsException() {
//        when(passengerRepository.existsByEmail(passengerDTO.getEmail())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(passengerDTO.getPhoneNumber())).thenReturn(true);
//
//        assertThatThrownBy(() -> passengerService.registerNewPassenger(passengerDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.PHONE_NUMBER_ALREADY_EXISTS);
//        verify(passengerRepository, never()).save(any(Passenger.class));
//        verify(mailSender, never()).send(any(org.springframework.mail.javamail.MimeMessage.class));
//    }
//
//    @Test
//    void registerNewPassenger_MailExceptionOccurs_LogsErrorButSavesUser() {
//        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
//        when(passengerMapper.PassengerDTOToPassenger(any(PassengerDTO.class))).thenReturn(passenger);
//        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//        when(passengerMapper.PassengerToPassengerDTO(any(Passenger.class))).thenReturn(passengerDTO);
//        when(mailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));
//        doThrow(new MailException("Test Mail Error") {
//        }).when(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
//
//        PassengerDTO savedDto = passengerService.registerNewPassenger(passengerDTO);
//
//        assertThat(savedDto).isNotNull();
//        verify(passengerRepository).save(any(Passenger.class));
//    }
//
//    @Test
//    void confirmUser_Success() {
//        when(passengerRepository.findByConfirmationToken("test-token")).thenReturn(Optional.of(passenger));
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//
//        boolean confirmed = passengerService.confirmUser("test-token");
//
//        assertThat(confirmed).isTrue();
//        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
//        verify(passengerRepository).save(captor.capture());
//        assertThat(captor.getValue().isEnabled()).isTrue();
//        assertThat(captor.getValue().getConfirmationToken()).isNull();
//        assertThat(captor.getValue().getTokenExpiryDate()).isNull();
//    }
//
//    @Test
//    void confirmUser_InvalidToken_ReturnsFalse() {
//        when(passengerRepository.findByConfirmationToken("invalid-token")).thenReturn(Optional.empty());
//
//        boolean confirmed = passengerService.confirmUser("invalid-token");
//
//        assertThat(confirmed).isFalse();
//        verify(passengerRepository, never()).save(any(Passenger.class));
//    }
//
//    @Test
//    void confirmUser_ExpiredToken_ReturnsFalse() {
//        passenger.setTokenExpiryDate(LocalDateTime.now().minusDays(1));
//        when(passengerRepository.findByConfirmationToken("test-token")).thenReturn(Optional.of(passenger));
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//
//
//        boolean confirmed = passengerService.confirmUser("test-token");
//
//        assertThat(confirmed).isFalse();
//        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
//        verify(passengerRepository).save(captor.capture());
//        assertThat(captor.getValue().isEnabled()).isFalse();
//        assertThat(captor.getValue().getConfirmationToken()).isNull();
//        assertThat(captor.getValue().getTokenExpiryDate()).isNull();
//    }
//
//    @Test
//    void existsByEmail_ReturnsTrue() {
//        when(passengerRepository.existsByEmail("test@example.com")).thenReturn(true);
//        boolean exists = passengerService.existsByEmail("test@example.com");
//        assertThat(exists).isTrue();
//        verify(passengerRepository).existsByEmail("test@example.com");
//    }
//
//    @Test
//    void existsByEmail_ReturnsFalse() {
//        when(passengerRepository.existsByEmail("test@example.com")).thenReturn(false);
//        boolean exists = passengerService.existsByEmail("test@example.com");
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    void existsByPhoneNumber_ReturnsTrue() {
//        when(passengerRepository.existsByPhoneNumber("123")).thenReturn(true);
//        boolean exists = passengerService.existsByPhoneNumber("123");
//        assertThat(exists).isTrue();
//        verify(passengerRepository).existsByPhoneNumber("123");
//    }
//
//    @Test
//    void existsByPhoneNumber_ReturnsFalse() {
//        when(passengerRepository.existsByPhoneNumber("123")).thenReturn(false);
//        boolean exists = passengerService.existsByPhoneNumber("123");
//        assertThat(exists).isFalse();
//    }
//
//
//    @Test
//    void getAllPassengers_ReturnsList() {
//        Passenger passenger2 = new Passenger();
//        passenger2.setId(2L);
//        List<Passenger> passengers = Arrays.asList(passenger, passenger2);
//        when(passengerRepository.findAll()).thenReturn(passengers);
//
//        List<Passenger> result = passengerService.getAllPassengers();
//
//        assertThat(result).hasSize(2);
//        assertThat(result).containsExactlyInAnyOrder(passenger, passenger2);
//        verify(passengerRepository).findAll();
//    }
//
//    @Test
//    void findAllPassengersPaginated_Success() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Passenger> expectedPage = new PageImpl<>(Collections.singletonList(passenger));
//        when(passengerRepository.findAll(pageable)).thenReturn(expectedPage);
//
//        Page<Passenger> actualPage = passengerService.findAllPassengersPaginated(pageable);
//
//        assertThat(actualPage).isNotNull();
//        assertThat(actualPage.getContent()).hasSize(1);
//        verify(passengerRepository).findAll(pageable);
//    }
//
//    @Test
//    void findPassengerById_Success() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        Optional<Passenger> result = passengerService.findPassengerById(1L);
//        assertThat(result).isPresent().contains(passenger);
//        verify(passengerRepository).findById(1L);
//    }
//
//    @Test
//    void findPassengerById_NotFound() {
//        when(passengerRepository.findById(99L)).thenReturn(Optional.empty());
//        Optional<Passenger> result = passengerService.findPassengerById(99L);
//        assertThat(result).isNotPresent();
//        verify(passengerRepository).findById(99L);
//    }
//
//    @Test
//    void updatePassengerByAdmin_Success_NoPasswordChange() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(passengerRepository.existsByEmail(passengerAdminEditDTO.getEmail())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(passengerAdminEditDTO.getPhoneNumber())).thenReturn(false);
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//
//        passengerService.updatePassengerByAdmin(passengerAdminEditDTO);
//
//        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
//        verify(passengerRepository).save(captor.capture());
//        Passenger updated = captor.getValue();
//
//        assertThat(updated.getName()).isEqualTo("Jan Updated");
//        assertThat(updated.getSurname()).isEqualTo("Kowalski Updated");
//        assertThat(updated.getEmail()).isEqualTo("jan.updated@example.com");
//        assertThat(updated.getPhoneNumber()).isEqualTo("987654321");
//        assertThat(updated.getRole()).isEqualTo("ROLE_ADMIN");
//        assertThat(updated.isEnabled()).isTrue();
//        assertThat(updated.getPassword()).isEqualTo("hashedPassword");
//        verify(passwordEncoder, never()).encode(anyString());
//    }
//
//    @Test
//    void updatePassengerByAdmin_Success_WithPasswordChange() {
//        passengerAdminEditDTO.setNewPassword("newPassword123");
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
//        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
//        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);
//
//        passengerService.updatePassengerByAdmin(passengerAdminEditDTO);
//
//        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
//        verify(passengerRepository).save(captor.capture());
//        assertThat(captor.getValue().getPassword()).isEqualTo("newHashedPassword");
//        verify(passwordEncoder).encode("newPassword123");
//    }
//
//    @Test
//    void updatePassengerByAdmin_NewPasswordTooShort_ThrowsException() {
//        passengerAdminEditDTO.setNewPassword("short");
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
//
//        assertThatThrownBy(() -> passengerService.updatePassengerByAdmin(passengerAdminEditDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.TOO_SHORT_PASSWORD);
//        verify(passengerRepository, never()).save(any(Passenger.class));
//        verify(passwordEncoder, never()).encode(anyString());
//    }
//
//
//    @Test
//    void updatePassengerByAdmin_EmailConflict_ThrowsException() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(passengerRepository.existsByEmail(passengerAdminEditDTO.getEmail())).thenReturn(true);
//
//        assertThatThrownBy(() -> passengerService.updatePassengerByAdmin(passengerAdminEditDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.EMAIL_ALREADY_EXISTS);
//        verify(passengerRepository, never()).save(any(Passenger.class));
//    }
//
//    @Test
//    void updatePassengerByAdmin_PhoneConflict_ThrowsException() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passengerRepository.existsByPhoneNumber(passengerAdminEditDTO.getPhoneNumber())).thenReturn(true);
//
//        assertThatThrownBy(() -> passengerService.updatePassengerByAdmin(passengerAdminEditDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.PHONE_NUMBER_ALREADY_EXISTS);
//        verify(passengerRepository, never()).save(any(Passenger.class));
//    }
//
//    @Test
//    void updatePassengerByAdmin_PassengerNotFound_ThrowsException() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> passengerService.updatePassengerByAdmin(passengerAdminEditDTO))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSENGER_NOT_FOUND);
//    }
//
//
//    @Test
//    void deletePassenger_Success_NoReservations() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(reservationRepository.existsByPassengerId(1L)).thenReturn(false);
//        doNothing().when(passengerRepository).deleteById(1L);
//
//        assertThatCode(() -> passengerService.deletePassenger(1L)).doesNotThrowAnyException();
//
//        verify(reservationRepository).existsByPassengerId(1L);
//        verify(passengerRepository).deleteById(1L);
//    }
//
//    @Test
//    void deletePassenger_Success_WithReservations_LogsWarning() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
//        when(reservationRepository.existsByPassengerId(1L)).thenReturn(true);
//        doNothing().when(passengerRepository).deleteById(1L);
//
//        assertThatCode(() -> passengerService.deletePassenger(1L)).doesNotThrowAnyException();
//
//        verify(reservationRepository).existsByPassengerId(1L);
//        verify(passengerRepository).deleteById(1L);
//    }
//
//    @Test
//    void deletePassenger_PassengerNotFound_ThrowsException() {
//        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> passengerService.deletePassenger(1L))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorType", ErrorType.PASSENGER_NOT_FOUND);
//
//        verify(reservationRepository, never()).existsByPassengerId(anyLong());
//        verify(passengerRepository, never()).deleteById(anyLong());
//    }
//
//}
