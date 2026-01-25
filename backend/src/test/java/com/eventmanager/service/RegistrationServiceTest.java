package com.eventmanager.service;

import com.eventmanager.dto.RegistrationDTO;
import com.eventmanager.entity.*;
import com.eventmanager.exception.RegistrationFullException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ParticipantRepository;
import com.eventmanager.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for RegistrationService
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Event testEvent;
    private Participant testParticipant;
    private Registration testRegistration;
    private RegistrationDTO testRegistrationDTO;

    @BeforeEach
    void setUp() {
        // Create test event with capacity
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Blåsarläger 2026");
        testEvent.setSlug("blasarlager-2026");
        testEvent.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        testEvent.setEndDate(LocalDateTime.of(2026, 7, 20, 14, 0));
        testEvent.setCapacity(100);
        testEvent.setActive(true);
        testEvent.setRegistrations(new ArrayList<>());

        // Create test participant
        testParticipant = new Participant();
        testParticipant.setId(1L);
        testParticipant.setFirstName("Emma");
        testParticipant.setLastName("Andersson");
        testParticipant.setEmail("emma@test.se");

        // Create test registration
        testRegistration = new Registration(testEvent, testParticipant);
        testRegistration.setId(1L);
        testRegistration.setStatus(RegistrationStatus.PENDING);

        // Create test DTO
        testRegistrationDTO = new RegistrationDTO();
        testRegistrationDTO.setEventId(1L);
        testRegistrationDTO.setParticipantId(1L);
    }

    @Test
    @DisplayName("findAll should return list of RegistrationDTOs")
    void findAll_ShouldReturnRegistrationDTOList() {
        // Arrange
        when(registrationRepository.findAll()).thenReturn(List.of(testRegistration));

        // Act
        List<RegistrationDTO> result = registrationService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        verify(registrationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById should return RegistrationDTO when registration exists")
    void findById_WhenRegistrationExists_ShouldReturnRegistrationDTO() {
        // Arrange
        when(registrationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testRegistration));

        // Act
        RegistrationDTO result = registrationService.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.PENDING);
        verify(registrationRepository, times(1)).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when not found")
    void findById_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(registrationRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registrationService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Registration");
        verify(registrationRepository, times(1)).findByIdWithDetails(99L);
    }

    @Test
    @DisplayName("create should save and return new RegistrationDTO")
    void create_ShouldSaveAndReturnRegistrationDTO() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(testParticipant));
        when(registrationRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(testRegistration);

        // Act
        RegistrationDTO result = registrationService.create(testRegistrationDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(eventRepository, times(1)).findById(1L);
        verify(participantRepository, times(1)).findById(1L);
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    @DisplayName("create should throw exception when event is full")
    void create_WhenEventFull_ShouldThrowException() {
        // Arrange
        testEvent.setCapacity(1);
        Registration existingRegistration = new Registration(testEvent, testParticipant);
        existingRegistration.setStatus(RegistrationStatus.CONFIRMED);
        testEvent.getRegistrations().add(existingRegistration);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(testParticipant));
        when(registrationRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.create(testRegistrationDTO))
            .isInstanceOf(RegistrationFullException.class)
            .hasMessageContaining("full");
    }

    @Test
    @DisplayName("create should throw exception when registration already exists")
    void create_WhenRegistrationExists_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(testParticipant));
        when(registrationRepository.existsByEventIdAndParticipantId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.create(testRegistrationDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("confirm should change status to CONFIRMED")
    void confirm_ShouldChangeStatusToConfirmed() {
        // Arrange
        when(registrationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testRegistration));
        when(registrationRepository.update(any(Registration.class))).thenReturn(testRegistration);

        // Act
        RegistrationDTO result = registrationService.confirm(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(registrationRepository, times(1)).findByIdWithDetails(1L);
        verify(registrationRepository, times(1)).update(any(Registration.class));
    }

    @Test
    @DisplayName("confirm should throw exception when event is full")
    void confirm_WhenEventFull_ShouldThrowException() {
        // Arrange
        testEvent.setCapacity(0);
        testRegistration.setStatus(RegistrationStatus.PENDING);
        
        when(registrationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testRegistration));

        // Act & Assert
        assertThatThrownBy(() -> registrationService.confirm(1L))
            .isInstanceOf(RegistrationFullException.class);
    }

    @Test
    @DisplayName("cancel should change status to CANCELLED")
    void cancel_ShouldChangeStatusToCancelled() {
        // Arrange
        testRegistration.setStatus(RegistrationStatus.CONFIRMED);
        when(registrationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testRegistration));
        when(registrationRepository.update(any(Registration.class))).thenReturn(testRegistration);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.findByEventIdAndStatus(1L, RegistrationStatus.WAITLIST)).thenReturn(List.of());

        // Act
        RegistrationDTO result = registrationService.cancel(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(registrationRepository, times(1)).findByIdWithDetails(1L);
        verify(registrationRepository, times(1)).update(any(Registration.class));
    }

    @Test
    @DisplayName("cancel should promote from waitlist when spot becomes available")
    void cancel_ShouldPromoteFromWaitlist() {
        // Arrange
        testRegistration.setStatus(RegistrationStatus.CONFIRMED);
        testEvent.setCapacity(1);
        
        Registration waitlistedRegistration = new Registration(testEvent, testParticipant);
        waitlistedRegistration.setId(2L);
        waitlistedRegistration.setStatus(RegistrationStatus.WAITLIST);

        when(registrationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testRegistration));
        when(registrationRepository.update(any(Registration.class))).thenReturn(testRegistration);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.findByEventIdAndStatus(1L, RegistrationStatus.WAITLIST))
            .thenReturn(List.of(waitlistedRegistration));

        // Act
        registrationService.cancel(1L);

        // Assert
        verify(registrationRepository, times(2)).update(any(Registration.class));
    }

    @Test
    @DisplayName("delete should call repository deleteById")
    void delete_ShouldCallRepositoryDelete() {
        // Arrange
        when(registrationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(registrationRepository).deleteById(1L);

        // Act
        registrationService.delete(1L);

        // Assert
        verify(registrationRepository, times(1)).existsById(1L);
        verify(registrationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("findByEventId should return registrations for event")
    void findByEventId_ShouldReturnRegistrationsForEvent() {
        // Arrange
        when(registrationRepository.findByEventId(1L)).thenReturn(List.of(testRegistration));

        // Act
        List<RegistrationDTO> result = registrationService.findByEventId(1L);

        // Assert
        assertThat(result).hasSize(1);
        verify(registrationRepository, times(1)).findByEventId(1L);
    }

    @Test
    @DisplayName("countByEventId should return correct count")
    void countByEventId_ShouldReturnCorrectCount() {
        // Arrange
        when(registrationRepository.countByEventId(1L)).thenReturn(5L);

        // Act
        long result = registrationService.countByEventId(1L);

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(registrationRepository, times(1)).countByEventId(1L);
    }
}
