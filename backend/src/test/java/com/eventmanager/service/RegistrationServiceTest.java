package com.eventmanager.service;

import com.eventmanager.dto.RegistrationDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationService
 * Uses Mockito to mock EntityManager and native queries
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query registrationQuery;

    @Mock
    private Query allergyQuery;

    @Mock
    private Query countQuery;

    @Mock
    private Query deleteQuery;

    @Mock
    private Query participantQuery;

    @Mock
    private Query eventNameQuery;

    @InjectMocks
    private RegistrationService registrationService;

    private List<Object[]> mockRegistrationResults;
    private List<Object[]> mockAllergyResults;

    @BeforeEach
    void setUp() {
        // Create mock registration result data
        mockRegistrationResults = new ArrayList<>();

        // Registration 1: Andersson, Anna
        Object[] reg1 = new Object[]{
                1L,                                              // reg_id
                "CONFIRMED",                                     // status
                Timestamp.valueOf(LocalDateTime.now()),          // registration_date
                Timestamp.valueOf(LocalDateTime.now()),          // confirmation_date
                "Inga noteringar",                               // notes
                101L,                                            // participant_id
                "Anna",                                          // first_name
                "Andersson",                                     // last_name
                "anna@example.com",                              // email
                "070-1234567",                                   // phone
                "2010-05-15",                                    // birth_date
                "20100515-1234",                                 // personal_number
                "Storgatan 1",                                   // street_address
                "12345",                                         // postal_code
                "Stockholm",                                     // city
                "Erik Andersson",                                // guardian_name
                "erik@example.com",                              // guardian_email
                "070-9876543",                                   // guardian_phone
                "Vargpatrullen"                                  // patrol_name
        };

        // Registration 2: Björk, Bengt
        Object[] reg2 = new Object[]{
                2L, "PENDING", Timestamp.valueOf(LocalDateTime.now()), null, null,
                102L, "Bengt", "Björk", "bengt@example.com", "070-2345678",
                "2012-08-20", "20120820-5678", "Lillvägen 5", "54321", "Göteborg",
                null, null, null, "Örnpatrullen"
        };

        mockRegistrationResults.add(reg1);
        mockRegistrationResults.add(reg2);

        // Create mock allergy result data
        mockAllergyResults = new ArrayList<>();

        // Participant 101 has peanut allergy
        Object[] allergy1 = new Object[]{101L, 1L, "Jordnötter", "SEVERE"};
        // Participant 101 also has gluten intolerance
        Object[] allergy2 = new Object[]{101L, 2L, "Gluten", "MODERATE"};

        mockAllergyResults.add(allergy1);
        mockAllergyResults.add(allergy2);
    }

    @Nested
    @DisplayName("getRegistrationsForEvent tests")
    class GetRegistrationsForEventTests {

        @Test
        @DisplayName("Should return registrations with participant details")
        void getRegistrationsForEvent_ReturnsRegistrations() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(mockRegistrationResults);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            assertEquals(2, result.size());

            // First registration (sorted by lastName, firstName)
            RegistrationDTO first = result.get(0);
            assertEquals("Anna", first.getFirstName());
            assertEquals("Andersson", first.getLastName());
            assertEquals("anna@example.com", first.getEmail());
            assertEquals("CONFIRMED", first.getStatus());
            assertEquals("Vargpatrullen", first.getPatrolName());
            assertEquals("Erik Andersson", first.getGuardianName());

            // Check allergies for first participant
            assertEquals(2, first.getAllergies().size());
            assertEquals("Jordnötter", first.getAllergies().get(0).getName());

            // Second registration
            RegistrationDTO second = result.get(1);
            assertEquals("Bengt", second.getFirstName());
            assertEquals("Björk", second.getLastName());
            assertEquals("PENDING", second.getStatus());
            assertTrue(second.getAllergies().isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when no registrations")
        void getRegistrationsForEvent_ReturnsEmptyList() {
            // Arrange
            Long eventId = 99L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            assertTrue(result.isEmpty());
            // Verify allergy query is not called when no registrations
            verify(em, never()).createNativeQuery(contains("participant_allergens"));
        }

        @Test
        @DisplayName("Should handle registrations without allergies")
        void getRegistrationsForEvent_HandlesNoAllergies() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(mockRegistrationResults);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.get(0).getAllergies().isEmpty());
            assertTrue(result.get(1).getAllergies().isEmpty());
        }
    }

    @Nested
    @DisplayName("countRegistrationsForEvent tests")
    class CountRegistrationsForEventTests {

        @Test
        @DisplayName("Should return correct count")
        void countRegistrationsForEvent_ReturnsCount() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("COUNT(*) FROM registrations"))).thenReturn(countQuery);
            when(countQuery.setParameter(eq(1), eq(eventId))).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(10L);

            // Act
            int result = registrationService.countRegistrationsForEvent(eventId);

            // Assert
            assertEquals(10, result);
        }

        @Test
        @DisplayName("Should return zero when no registrations")
        void countRegistrationsForEvent_ReturnsZero() {
            // Arrange
            Long eventId = 99L;

            when(em.createNativeQuery(contains("COUNT(*)"))).thenReturn(countQuery);
            when(countQuery.setParameter(eq(1), eq(eventId))).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(0L);

            // Act
            int result = registrationService.countRegistrationsForEvent(eventId);

            // Assert
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("countConfirmedRegistrations tests")
    class CountConfirmedRegistrationsTests {

        @Test
        @DisplayName("Should return only confirmed count")
        void countConfirmedRegistrations_ReturnsConfirmedOnly() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("status = 'CONFIRMED'"))).thenReturn(countQuery);
            when(countQuery.setParameter(eq(1), eq(eventId))).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(5L);

            // Act
            int result = registrationService.countConfirmedRegistrations(eventId);

            // Assert
            assertEquals(5, result);
        }
    }

    @Nested
    @DisplayName("deleteRegistration tests")
    class DeleteRegistrationTests {

        @Test
        @DisplayName("Should delete registration successfully")
        void deleteRegistration_Success() {
            // Arrange
            Long eventId = 1L;
            Long registrationId = 10L;
            Long participantId = 101L;

            // Mock check query - registration exists
            Query checkQuery = mock(Query.class);
            when(em.createNativeQuery(contains("COUNT(*) FROM registrations WHERE id"))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(1), eq(registrationId))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(2), eq(eventId))).thenReturn(checkQuery);
            when(checkQuery.getSingleResult()).thenReturn(1L);

            // Mock get participant ID query
            when(em.createNativeQuery(contains("SELECT participant_id"))).thenReturn(participantQuery);
            when(participantQuery.setParameter(eq(1), eq(registrationId))).thenReturn(participantQuery);
            when(participantQuery.getSingleResult()).thenReturn(participantId);

            // Mock delete registration query
            Query deleteRegQuery = mock(Query.class);
            when(em.createNativeQuery(contains("DELETE FROM registrations WHERE id"))).thenReturn(deleteRegQuery);
            when(deleteRegQuery.setParameter(eq(1), eq(registrationId))).thenReturn(deleteRegQuery);
            when(deleteRegQuery.executeUpdate()).thenReturn(1);

            // Mock check other registrations query
            Query otherRegQuery = mock(Query.class);
            when(em.createNativeQuery(contains("COUNT(*) FROM registrations WHERE participant_id"))).thenReturn(otherRegQuery);
            when(otherRegQuery.setParameter(eq(1), eq(participantId))).thenReturn(otherRegQuery);
            when(otherRegQuery.getSingleResult()).thenReturn(0L);

            // Act
            boolean result = registrationService.deleteRegistration(eventId, registrationId);

            // Assert
            assertTrue(result);
            verify(deleteRegQuery).executeUpdate();
        }

        @Test
        @DisplayName("Should return false when registration not found")
        void deleteRegistration_ReturnsFalse_WhenNotFound() {
            // Arrange
            Long eventId = 1L;
            Long registrationId = 999L;

            Query checkQuery = mock(Query.class);
            when(em.createNativeQuery(contains("COUNT(*) FROM registrations WHERE id"))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(1), eq(registrationId))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(2), eq(eventId))).thenReturn(checkQuery);
            when(checkQuery.getSingleResult()).thenReturn(0L);

            // Act
            boolean result = registrationService.deleteRegistration(eventId, registrationId);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when registration belongs to different event")
        void deleteRegistration_ReturnsFalse_WhenWrongEvent() {
            // Arrange
            Long eventId = 1L;
            Long registrationId = 10L; // Belongs to event 2

            Query checkQuery = mock(Query.class);
            when(em.createNativeQuery(contains("COUNT(*) FROM registrations WHERE id"))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(1), eq(registrationId))).thenReturn(checkQuery);
            when(checkQuery.setParameter(eq(2), eq(eventId))).thenReturn(checkQuery);
            when(checkQuery.getSingleResult()).thenReturn(0L); // Not found for this event

            // Act
            boolean result = registrationService.deleteRegistration(eventId, registrationId);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Data mapping tests")
    class DataMappingTests {

        @Test
        @DisplayName("Should correctly map all DTO fields")
        void shouldMapAllDTOFields() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(mockRegistrationResults);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            RegistrationDTO dto = result.get(0);

            // Verify all fields are mapped
            assertEquals(1L, dto.getId());
            assertEquals(eventId, dto.getEventId());
            assertEquals("CONFIRMED", dto.getStatus());
            assertNotNull(dto.getRegistrationDate());
            assertNotNull(dto.getConfirmationDate());
            assertEquals("Inga noteringar", dto.getNotes());
            assertEquals(101L, dto.getParticipantId());
            assertEquals("Anna", dto.getFirstName());
            assertEquals("Andersson", dto.getLastName());
            assertEquals("anna@example.com", dto.getEmail());
            assertEquals("070-1234567", dto.getPhone());
            assertEquals("Storgatan 1", dto.getStreetAddress());
            assertEquals("12345", dto.getPostalCode());
            assertEquals("Stockholm", dto.getCity());
            assertEquals("Erik Andersson", dto.getGuardianName());
            assertEquals("erik@example.com", dto.getGuardianEmail());
            assertEquals("070-9876543", dto.getGuardianPhone());
            assertEquals("Vargpatrullen", dto.getPatrolName());
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullFields() {
            // Arrange
            Long eventId = 1L;

            // Create registration with null optional fields
            Object[] regWithNulls = new Object[]{
                    3L, "PENDING", Timestamp.valueOf(LocalDateTime.now()), null, null,
                    103L, "Carl", "Carlsson", null, null, // null email and phone
                    null, null, null, null, null, // null address fields
                    null, null, null, null // null guardian and patrol
            };

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            List<Object[]> resultList = new ArrayList<>();
            resultList.add(regWithNulls);
            when(registrationQuery.getResultList()).thenReturn(resultList);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            RegistrationDTO dto = result.get(0);
            assertEquals("Carl", dto.getFirstName());
            assertEquals("Carlsson", dto.getLastName());
            assertNull(dto.getEmail());
            assertNull(dto.getPhone());
            assertNull(dto.getStreetAddress());
            assertNull(dto.getGuardianName());
            assertNull(dto.getPatrolName());
        }
    }

    @Nested
    @DisplayName("Allergy mapping tests")
    class AllergyMappingTests {

        @Test
        @DisplayName("Should correctly map allergies to participants")
        void shouldMapAllergiesToCorrectParticipants() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(mockRegistrationResults);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            // Participant 101 (Anna) should have 2 allergies
            RegistrationDTO anna = result.stream()
                    .filter(r -> r.getParticipantId() == 101L)
                    .findFirst()
                    .orElseThrow();
            assertEquals(2, anna.getAllergies().size());

            // Participant 102 (Bengt) should have no allergies
            RegistrationDTO bengt = result.stream()
                    .filter(r -> r.getParticipantId() == 102L)
                    .findFirst()
                    .orElseThrow();
            assertTrue(bengt.getAllergies().isEmpty());
        }

        @Test
        @DisplayName("Should map allergy info correctly")
        void shouldMapAllergyInfoCorrectly() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("FROM registrations r"))).thenReturn(registrationQuery);
            when(registrationQuery.setParameter(eq(1), eq(eventId))).thenReturn(registrationQuery);
            when(registrationQuery.getResultList()).thenReturn(mockRegistrationResults);

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            List<RegistrationDTO> result = registrationService.getRegistrationsForEvent(eventId);

            // Assert
            RegistrationDTO anna = result.get(0);
            RegistrationDTO.AllergyInfo peanutAllergy = anna.getAllergies().get(0);

            assertEquals(1L, peanutAllergy.getId());
            assertEquals("Jordnötter", peanutAllergy.getName());
            assertEquals("SEVERE", peanutAllergy.getSeverity());
        }
    }
}