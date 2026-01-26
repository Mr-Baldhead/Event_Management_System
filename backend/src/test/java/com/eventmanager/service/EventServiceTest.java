package com.eventmanager.service;

import com.eventmanager.dto.EventDTO;
import com.eventmanager.dto.EventPatchDTO;
import com.eventmanager.entity.Event;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService
 * Uses Mockito to mock repository and EntityManager dependencies
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Long> countQuery;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventDTO testEventDTO;

    @BeforeEach
    void setUp() {
        // Create test event entity
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Blåsarläger 2026");
        testEvent.setDescription("Scoutläger för blåsare");
        testEvent.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        testEvent.setEndDate(LocalDateTime.of(2026, 7, 20, 16, 0));
        testEvent.setStreetAddress("Lägervägen 1");
        testEvent.setPostalCode("12345");
        testEvent.setCity("Naturby");
        testEvent.setCapacity(150);
        testEvent.setActive(true);
        // Note: createdAt/updatedAt are set by JPA @PrePersist/@PreUpdate

        // Create test DTO
        testEventDTO = new EventDTO();
        testEventDTO.setId(1L);
        testEventDTO.setName("Blåsarläger 2026");
        testEventDTO.setDescription("Scoutläger för blåsare");
        testEventDTO.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        testEventDTO.setEndDate(LocalDateTime.of(2026, 7, 20, 16, 0));
        testEventDTO.setCity("Naturby");
        testEventDTO.setCapacity(150);
        testEventDTO.setActive(true);
    }

    // Helper method to mock registration count query
    private void mockRegistrationCount(Long eventId, int count) {
        lenient().when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        lenient().when(countQuery.setParameter(eq("eventId"), eq(eventId))).thenReturn(countQuery);
        lenient().when(countQuery.getSingleResult()).thenReturn((long) count);

        @Nested
        @DisplayName("findAll tests")
        class FindAllTests {

            @Test
            @DisplayName("Should return all events with registration counts")
            void findAll_ReturnsAllEvents() {
                // Arrange
                Event event2 = new Event();
                event2.setId(2L);
                event2.setName("Höstläger 2026");
                event2.setCapacity(100);
                event2.setActive(true);

                when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent, event2));
                mockRegistrationCount(1L, 50);
                mockRegistrationCount(2L, 25);

                // Act
                List<EventDTO> result = eventService.findAll();

                // Assert
                assertEquals(2, result.size());
                verify(eventRepository).findAll();
            }

            @Test
            @DisplayName("Should return empty list when no events exist")
            void findAll_ReturnsEmptyList() {
                // Arrange
                when(eventRepository.findAll()).thenReturn(Collections.emptyList());

                // Act
                List<EventDTO> result = eventService.findAll();

                // Assert
                assertTrue(result.isEmpty());
                verify(eventRepository).findAll();
            }
        }

        @Nested
        @DisplayName("findById tests")
        class FindByIdTests {

            @Test
            @DisplayName("Should return event when found")
            void findById_ReturnsEvent() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 6);

                // Act
                EventDTO result = eventService.findById(1L);

                // Assert
                assertNotNull(result);
                assertEquals("Blåsarläger 2026", result.getName());
                assertEquals(6, result.getRegistrationCount());
                assertEquals(144, result.getRemainingSpots()); // 150 - 6
            }

            @Test
            @DisplayName("Should throw ResourceNotFoundException when event not found")
            void findById_ThrowsException_WhenNotFound() {
                // Arrange
                when(eventRepository.findById(999L)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(ResourceNotFoundException.class, () ->
                        eventService.findById(999L)
                );
            }
        }

        @Nested
        @DisplayName("findByIdOptional tests")
        class FindByIdOptionalTests {

            @Test
            @DisplayName("Should return Optional with event when found")
            void findByIdOptional_ReturnsOptional() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 0);

                // Act
                Optional<EventDTO> result = eventService.findByIdOptional(1L);

                // Assert
                assertTrue(result.isPresent());
                assertEquals("Blåsarläger 2026", result.get().getName());
            }

            @Test
            @DisplayName("Should return empty Optional when not found")
            void findByIdOptional_ReturnsEmpty() {
                // Arrange
                when(eventRepository.findById(999L)).thenReturn(Optional.empty());

                // Act
                Optional<EventDTO> result = eventService.findByIdOptional(999L);

                // Assert
                assertTrue(result.isEmpty());
            }
        }

        @Nested
        @DisplayName("create tests")
        class CreateTests {

            @Test
            @DisplayName("Should create event successfully")
            void create_Success() {
                // Arrange
                when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
                mockRegistrationCount(1L, 0);

                // Act
                EventDTO result = eventService.create(testEventDTO);

                // Assert
                assertNotNull(result);
                assertEquals("Blåsarläger 2026", result.getName());
                verify(eventRepository).save(any(Event.class));
            }
        }

        @Nested
        @DisplayName("update tests")
        class UpdateTests {

            @Test
            @DisplayName("Should update event successfully")
            void update_Success() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                when(eventRepository.update(any(Event.class))).thenReturn(testEvent);
                mockRegistrationCount(1L, 6);

                testEventDTO.setName("Blåsarläger 2026 - Uppdaterad");

                // Act
                EventDTO result = eventService.update(1L, testEventDTO);

                // Assert
                assertNotNull(result);
                verify(eventRepository).update(any(Event.class));
            }

            @Test
            @DisplayName("Should throw exception when event not found")
            void update_ThrowsException_WhenNotFound() {
                // Arrange
                when(eventRepository.findById(999L)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(ResourceNotFoundException.class, () ->
                        eventService.update(999L, testEventDTO)
                );
            }
        }

        @Nested
        @DisplayName("patch tests")
        class PatchTests {

            @Test
            @DisplayName("Should patch active status only")
            void patch_ActiveStatusOnly() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                when(eventRepository.update(any(Event.class))).thenReturn(testEvent);
                mockRegistrationCount(1L, 6);

                EventPatchDTO patchDTO = new EventPatchDTO();
                patchDTO.setActive(false);

                // Act
                Optional<EventDTO> result = eventService.patch(1L, patchDTO);

                // Assert
                assertTrue(result.isPresent());
                verify(eventRepository).update(argThat(event ->
                        event.getActive() == false &&
                                event.getName().equals("Blåsarläger 2026") // Name unchanged
                ));
            }

            @Test
            @DisplayName("Should patch multiple fields")
            void patch_MultipleFields() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                when(eventRepository.update(any(Event.class))).thenReturn(testEvent);
                mockRegistrationCount(1L, 0);

                EventPatchDTO patchDTO = new EventPatchDTO();
                patchDTO.setActive(false);
                patchDTO.setName("Nytt namn");
                patchDTO.setDescription("Ny beskrivning");

                // Act
                Optional<EventDTO> result = eventService.patch(1L, patchDTO);

                // Assert
                assertTrue(result.isPresent());
                verify(eventRepository).update(any(Event.class));
            }

            @Test
            @DisplayName("Should return empty when event not found")
            void patch_ReturnsEmpty_WhenNotFound() {
                // Arrange
                when(eventRepository.findById(999L)).thenReturn(Optional.empty());

                EventPatchDTO patchDTO = new EventPatchDTO();
                patchDTO.setActive(true);

                // Act
                Optional<EventDTO> result = eventService.patch(999L, patchDTO);

                // Assert
                assertTrue(result.isEmpty());
            }
        }

        @Nested
        @DisplayName("delete tests")
        class DeleteTests {

            @Test
            @DisplayName("Should delete event successfully")
            void delete_Success() {
                // Arrange
                when(eventRepository.existsById(1L)).thenReturn(true);
                doNothing().when(eventRepository).deleteById(1L);

                // Act
                eventService.delete(1L);

                // Assert
                verify(eventRepository).deleteById(1L);
            }

            @Test
            @DisplayName("Should throw exception when event not found")
            void delete_ThrowsException_WhenNotFound() {
                // Arrange
                when(eventRepository.existsById(999L)).thenReturn(false);

                // Act & Assert
                assertThrows(ResourceNotFoundException.class, () ->
                        eventService.delete(999L)
                );
                verify(eventRepository, never()).deleteById(anyLong());
            }
        }

        @Nested
        @DisplayName("deleteIfExists tests")
        class DeleteIfExistsTests {

            @Test
            @DisplayName("Should return true when event deleted")
            void deleteIfExists_ReturnsTrue() {
                // Arrange
                when(eventRepository.existsById(1L)).thenReturn(true);
                doNothing().when(eventRepository).deleteById(1L);

                // Act
                boolean result = eventService.deleteIfExists(1L);

                // Assert
                assertTrue(result);
                verify(eventRepository).deleteById(1L);
            }

            @Test
            @DisplayName("Should return false when event not found")
            void deleteIfExists_ReturnsFalse() {
                // Arrange
                when(eventRepository.existsById(999L)).thenReturn(false);

                // Act
                boolean result = eventService.deleteIfExists(999L);

                // Assert
                assertFalse(result);
                verify(eventRepository, never()).deleteById(anyLong());
            }
        }

        @Nested
        @DisplayName("hasAvailableCapacity tests")
        class HasAvailableCapacityTests {

            @Test
            @DisplayName("Should return true when capacity available")
            void hasAvailableCapacity_ReturnsTrue() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 100);

                // Act
                boolean result = eventService.hasAvailableCapacity(1L);

                // Assert
                assertTrue(result); // 100 < 150
            }

            @Test
            @DisplayName("Should return false when full")
            void hasAvailableCapacity_ReturnsFalse_WhenFull() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 150);

                // Act
                boolean result = eventService.hasAvailableCapacity(1L);

                // Assert
                assertFalse(result); // 150 >= 150
            }

            @Test
            @DisplayName("Should return true when no capacity limit")
            void hasAvailableCapacity_ReturnsTrue_WhenNoLimit() {
                // Arrange
                testEvent.setCapacity(null);
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 1000);

                // Act
                boolean result = eventService.hasAvailableCapacity(1L);

                // Assert
                assertTrue(result); // No limit
            }
        }

        @Nested
        @DisplayName("getRemainingSpots tests")
        class GetRemainingSpotsTests {

            @Test
            @DisplayName("Should return correct remaining spots")
            void getRemainingSpots_ReturnsCorrect() {
                // Arrange
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 50);

                // Act
                int result = eventService.getRemainingSpots(1L);

                // Assert
                assertEquals(100, result); // 150 - 50
            }

            @Test
            @DisplayName("Should return MAX_VALUE when no limit")
            void getRemainingSpots_ReturnsMaxValue_WhenNoLimit() {
                // Arrange
                testEvent.setCapacity(null);
                when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
                mockRegistrationCount(1L, 100);

                // Act
                int result = eventService.getRemainingSpots(1L);

                // Assert
                assertEquals(Integer.MAX_VALUE, result);
            }
        }

        @Nested
        @DisplayName("count tests")
        class CountTests {

            @Test
            @DisplayName("Should return total count")
            void count_ReturnsTotal() {
                // Arrange
                when(eventRepository.count()).thenReturn(5L);

                // Act
                long result = eventService.count();

                // Assert
                assertEquals(5L, result);
            }
        }
    }
}