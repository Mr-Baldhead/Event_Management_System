package com.eventmanager.service;

import com.eventmanager.dto.AllergyReportDTO;
import com.eventmanager.dto.AllergyReportDTO.AllergyGroupDTO;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AllergyReportService
 * Uses Mockito to mock EntityManager and native queries
 */
@ExtendWith(MockitoExtension.class)
class AllergyReportServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query eventQuery;

    @Mock
    private Query allergyQuery;

    @InjectMocks
    private AllergyReportService allergyReportService;

    private List<Object[]> mockAllergyResults;

    @BeforeEach
    void setUp() {
        // Create mock allergy data
        mockAllergyResults = new ArrayList<>();

        // Participant with Gluten allergy
        Object[] glutenAllergy1 = new Object[]{
                "Gluten",           // allergen_name
                101L,               // participant_id
                "Anna",             // first_name
                "Andersson",        // last_name
                "Vargpatrullen",    // patrol_name
                "Glutenintolerans"  // allergen_description
        };

        Object[] glutenAllergy2 = new Object[]{
                "Gluten", 102L, "Bengt", "Björk", "Örnpatrullen", "Glutenintolerans"
        };

        // Participant with Peanut allergy
        Object[] peanutAllergy1 = new Object[]{
                "Jordnötter", 101L, "Anna", "Andersson", "Vargpatrullen", "Svår allergi"
        };

        Object[] peanutAllergy2 = new Object[]{
                "Jordnötter", 103L, "Carl", "Carlsson", "Bäverpatrullen", "Svår allergi"
        };

        // Participant with Lactose intolerance
        Object[] lactoseAllergy = new Object[]{
                "Laktos", 104L, "Diana", "Davidsson", null, "Laktosintolerans"
        };

        mockAllergyResults.add(glutenAllergy1);
        mockAllergyResults.add(glutenAllergy2);
        mockAllergyResults.add(peanutAllergy1);
        mockAllergyResults.add(peanutAllergy2);
        mockAllergyResults.add(lactoseAllergy);
    }

    @Nested
    @DisplayName("generateReport tests")
    class GenerateReportTests {

        @Test
        @DisplayName("Should generate report with allergy groups")
        void generateReport_Success() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Blåsarläger 2026"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert
            assertNotNull(result);
            assertEquals(eventId, result.getEventId());
            assertEquals("Blåsarläger 2026", result.getEventName());
            assertNotNull(result.getGeneratedAt());

            // Check allergy groups
            assertEquals(3, result.getAllergies().size()); // Gluten, Jordnötter, Laktos

            // Check total unique participants with allergies
            assertEquals(4, result.getTotalParticipantsWithAllergies()); // Anna (2 allergies counts once), Bengt, Carl, Diana
        }

        @Test
        @DisplayName("Should group participants by allergy type")
        void generateReport_GroupsCorrectly() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert
            // Find Gluten group
            AllergyGroupDTO glutenGroup = result.getAllergies().stream()
                    .filter(g -> g.getAllergyName().equals("Gluten"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(2, glutenGroup.getCount());
            assertEquals(2, glutenGroup.getParticipants().size());

            // Find Jordnötter group
            AllergyGroupDTO peanutGroup = result.getAllergies().stream()
                    .filter(g -> g.getAllergyName().equals("Jordnötter"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(2, peanutGroup.getCount());

            // Find Laktos group
            AllergyGroupDTO lactoseGroup = result.getAllergies().stream()
                    .filter(g -> g.getAllergyName().equals("Laktos"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1, lactoseGroup.getCount());
        }

        @Test
        @DisplayName("Should format name as 'Efternamn Förnamn'")
        void generateReport_FormatsNameCorrectly() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert
            AllergyGroupDTO glutenGroup = result.getAllergies().stream()
                    .filter(g -> g.getAllergyName().equals("Gluten"))
                    .findFirst()
                    .orElseThrow();

            // Names should be "Efternamn Förnamn"
            assertTrue(glutenGroup.getParticipants().stream()
                    .anyMatch(p -> p.getFullName().equals("Andersson Anna")));
            assertTrue(glutenGroup.getParticipants().stream()
                    .anyMatch(p -> p.getFullName().equals("Björk Bengt")));
        }

        @Test
        @DisplayName("Should return empty allergies when no participants have allergies")
        void generateReport_NoAllergies() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Event Without Allergies"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert
            assertNotNull(result);
            assertEquals("Event Without Allergies", result.getEventName());
            assertTrue(result.getAllergies().isEmpty());
            assertEquals(0, result.getTotalParticipantsWithAllergies());
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void generateReport_ThrowsException_WhenEventNotFound() {
            // Arrange
            Long eventId = 999L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> allergyReportService.generateReport(eventId)
            );
            assertTrue(exception.getMessage().contains("Event not found"));
        }

        @Test
        @DisplayName("Should handle participants without patrol")
        void generateReport_HandlesNullPatrol() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert - Diana has no patrol (null)
            AllergyGroupDTO lactoseGroup = result.getAllergies().stream()
                    .filter(g -> g.getAllergyName().equals("Laktos"))
                    .findFirst()
                    .orElseThrow();

            assertNull(lactoseGroup.getParticipants().get(0).getPatrol());
        }
    }

    @Nested
    @DisplayName("generateCSV tests")
    class GenerateCSVTests {

        @Test
        @DisplayName("Should generate CSV with correct format")
        void generateCSV_Success() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            String csv = allergyReportService.generateCSV(eventId);

            // Assert
            assertNotNull(csv);
            assertTrue(csv.startsWith("\uFEFF")); // BOM
            assertTrue(csv.contains("Allergi;Namn;Kår/Patrull"));
            assertTrue(csv.contains("Gluten;Andersson Anna;Vargpatrullen"));
            assertTrue(csv.contains("Jordnötter;Carlsson Carl;Bäverpatrullen"));
        }

        @Test
        @DisplayName("Should return only header when no allergies")
        void generateCSV_NoAllergies() {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Empty Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            String csv = allergyReportService.generateCSV(eventId);

            // Assert
            assertNotNull(csv);
            assertTrue(csv.contains("Allergi;Namn;Kår/Patrull"));
            // Only header line (plus BOM)
            String[] lines = csv.split("\n");
            assertEquals(1, lines.length);
        }

        @Test
        @DisplayName("Should escape special characters in CSV")
        void generateCSV_EscapesSpecialChars() {
            // Arrange
            Long eventId = 1L;

            // Create data with special characters
            Object[] specialChars = new Object[]{
                    "Allergi; med; semikolon",  // allergen_name with semicolons
                    105L,
                    "Erik",
                    "Eriksson",
                    "Patrull \"Test\"",  // patrol with quotes
                    "Description"
            };

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Arrays.asList(new Object[][]{specialChars}));

            // Act
            String csv = allergyReportService.generateCSV(eventId);

            // Assert
            // Values with semicolons or quotes should be quoted and escaped
            assertTrue(csv.contains("\"Allergi; med; semikolon\""));
            assertTrue(csv.contains("\"Patrull \"\"Test\"\"\""));
        }
    }

    @Nested
    @DisplayName("generateExcel tests")
    class GenerateExcelTests {

        @Test
        @DisplayName("Should generate valid Excel file")
        void generateExcel_Success() throws IOException {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            byte[] excelBytes = allergyReportService.generateExcel(eventId);

            // Assert
            assertNotNull(excelBytes);
            assertTrue(excelBytes.length > 0);

            // Verify it starts with XLSX magic bytes (PK - ZIP format)
            assertEquals(0x50, excelBytes[0] & 0xFF); // 'P'
            assertEquals(0x4B, excelBytes[1] & 0xFF); // 'K'
        }

        @Test
        @DisplayName("Should generate Excel file even with no allergies")
        void generateExcel_NoAllergies() throws IOException {
            // Arrange
            Long eventId = 1L;

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Empty Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(Collections.emptyList());

            // Act
            byte[] excelBytes = allergyReportService.generateExcel(eventId);

            // Assert
            assertNotNull(excelBytes);
            assertTrue(excelBytes.length > 0);
        }
    }

    @Nested
    @DisplayName("Participant counting tests")
    class ParticipantCountingTests {

        @Test
        @DisplayName("Should count unique participants correctly")
        void shouldCountUniqueParticipants() {
            // Arrange
            Long eventId = 1L;

            // Anna (101) has both Gluten and Jordnötter allergies
            // Should only be counted once

            when(em.createNativeQuery(contains("SELECT name FROM events"))).thenReturn(eventQuery);
            when(eventQuery.setParameter(eq(1), eq(eventId))).thenReturn(eventQuery);
            when(eventQuery.getResultList()).thenReturn(List.of("Test Event"));

            when(em.createNativeQuery(contains("participant_allergens"))).thenReturn(allergyQuery);
            when(allergyQuery.setParameter(eq(1), eq(eventId))).thenReturn(allergyQuery);
            when(allergyQuery.getResultList()).thenReturn(mockAllergyResults);

            // Act
            AllergyReportDTO result = allergyReportService.generateReport(eventId);

            // Assert
            // Total unique participants: Anna (101), Bengt (102), Carl (103), Diana (104)
            assertEquals(4, result.getTotalParticipantsWithAllergies());
        }
    }
}
