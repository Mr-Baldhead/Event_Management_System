package com.eventmanager.service;

import com.eventmanager.dto.EventDTO;
import com.eventmanager.entity.Event;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for EventService
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventDTO testEventDTO;

    @BeforeEach
    void setUp() {
        // Create test event
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Blåsarläger 2026");
        testEvent.setSlug("blasarlager-2026");
        testEvent.setDescription("Årligt scoutläger");
        testEvent.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        testEvent.setEndDate(LocalDateTime.of(2026, 7, 20, 14, 0));
        testEvent.setCity("Naturby");
        testEvent.setCapacity(100);
        testEvent.setActive(true);

        // Create test DTO
        testEventDTO = new EventDTO();
        testEventDTO.setName("Blåsarläger 2026");
        testEventDTO.setSlug("blasarlager-2026");
        testEventDTO.setDescription("Årligt scoutläger");
        testEventDTO.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        testEventDTO.setEndDate(LocalDateTime.of(2026, 7, 20, 14, 0));
        testEventDTO.setCity("Naturby");
        testEventDTO.setCapacity(100);
    }

    @Test
    @DisplayName("findAll should return list of EventDTOs")
    void findAll_ShouldReturnEventDTOList() {
        // Arrange
        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Höstläger");
        event2.setSlug("hostlager");
        event2.setStartDate(LocalDateTime.now());
        event2.setEndDate(LocalDateTime.now().plusDays(3));

        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent, event2));

        // Act
        List<EventDTO> result = eventService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Blåsarläger 2026");
        assertThat(result.get(1).getName()).isEqualTo("Höstläger");
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById should return EventDTO when event exists")
    void findById_WhenEventExists_ShouldReturnEventDTO() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        EventDTO result = eventService.findById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Blåsarläger 2026");
        assertThat(result.getSlug()).isEqualTo("blasarlager-2026");
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when event not found")
    void findById_WhenEventNotFound_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Event")
            .hasMessageContaining("99");
        verify(eventRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("findBySlug should return EventDTO when slug exists")
    void findBySlug_WhenSlugExists_ShouldReturnEventDTO() {
        // Arrange
        when(eventRepository.findBySlug("blasarlager-2026")).thenReturn(Optional.of(testEvent));

        // Act
        EventDTO result = eventService.findBySlug("blasarlager-2026");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("blasarlager-2026");
        verify(eventRepository, times(1)).findBySlug("blasarlager-2026");
    }

    @Test
    @DisplayName("create should save and return new EventDTO")
    void create_ShouldSaveAndReturnEventDTO() {
        // Arrange
        when(eventRepository.isSlugTaken("blasarlager-2026", null)).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        EventDTO result = eventService.create(testEventDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Blåsarläger 2026");
        verify(eventRepository, times(1)).isSlugTaken("blasarlager-2026", null);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("create should throw exception when slug is taken")
    void create_WhenSlugTaken_ShouldThrowException() {
        // Arrange
        when(eventRepository.isSlugTaken("blasarlager-2026", null)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> eventService.create(testEventDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Slug")
            .hasMessageContaining("already taken");
        verify(eventRepository, times(1)).isSlugTaken("blasarlager-2026", null);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("update should update and return EventDTO")
    void update_ShouldUpdateAndReturnEventDTO() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.isSlugTaken("blasarlager-2026", 1L)).thenReturn(false);
        when(eventRepository.update(any(Event.class))).thenReturn(testEvent);

        testEventDTO.setDescription("Uppdaterad beskrivning");

        // Act
        EventDTO result = eventService.update(1L, testEventDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).update(any(Event.class));
    }

    @Test
    @DisplayName("delete should call repository deleteById")
    void delete_ShouldCallRepositoryDelete() {
        // Arrange
        when(eventRepository.existsById(1L)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(1L);

        // Act
        eventService.delete(1L);

        // Assert
        verify(eventRepository, times(1)).existsById(1L);
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete should throw exception when event not found")
    void delete_WhenEventNotFound_ShouldThrowException() {
        // Arrange
        when(eventRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> eventService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, times(1)).existsById(99L);
        verify(eventRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("generateSlug should create valid slug from Swedish name")
    void generateSlug_ShouldCreateValidSlugFromSwedishName() {
        // Arrange
        when(eventRepository.isSlugTaken(anyString(), any())).thenReturn(false);

        // Act
        String result = eventService.generateSlug("Blåsarläger 2026!");

        // Assert
        assertThat(result).isEqualTo("blasarlager-2026");
    }

    @Test
    @DisplayName("generateSlug should append number when slug is taken")
    void generateSlug_WhenSlugTaken_ShouldAppendNumber() {
        // Arrange
        when(eventRepository.isSlugTaken("blasarlager", null)).thenReturn(true);
        when(eventRepository.isSlugTaken("blasarlager-1", null)).thenReturn(true);
        when(eventRepository.isSlugTaken("blasarlager-2", null)).thenReturn(false);

        // Act
        String result = eventService.generateSlug("Blåsarläger");

        // Assert
        assertThat(result).isEqualTo("blasarlager-2");
    }

    @Test
    @DisplayName("findUpcoming should return future events")
    void findUpcoming_ShouldReturnFutureEvents() {
        // Arrange
        when(eventRepository.findUpcoming()).thenReturn(List.of(testEvent));

        // Act
        List<EventDTO> result = eventService.findUpcoming();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Blåsarläger 2026");
        verify(eventRepository, times(1)).findUpcoming();
    }
}
