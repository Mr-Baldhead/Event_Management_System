package com.eventmanager.test;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Participant;
import com.eventmanager.dto.EventDTO;
import com.eventmanager.dto.RegistrationDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Factory class for creating test data objects.
 * Provides consistent test data across all unit tests.
 */
public class TestDataFactory {

    // Default test values
    public static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(2026, 7, 15, 10, 0);
    public static final LocalDateTime DEFAULT_END_DATE = LocalDateTime.of(2026, 7, 20, 16, 0);

    /**
     * Create a test Event entity with specified values
     * Note: createdAt and updatedAt are managed by JPA @PrePersist/@PreUpdate
     */
    public static Event createEvent(Long id, String name) {
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setDescription("Test description for " + name);
        event.setStartDate(DEFAULT_START_DATE);
        event.setEndDate(DEFAULT_END_DATE);
        event.setStreetAddress("Testvägen 1");
        event.setPostalCode("12345");
        event.setCity("Teststad");
        event.setCapacity(100);
        event.setActive(true);
        // Note: createdAt/updatedAt set automatically by JPA
        return event;
    }

    /**
     * Create a test Event with default id=1 and given name
     */
    public static Event createEvent(String name) {
        return createEvent(1L, name);
    }

    /**
     * Create a scout camp event (typical use case)
     * Note: createdAt and updatedAt are managed by JPA @PrePersist/@PreUpdate
     */
    public static Event createScoutCampEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Blåsarläger 2026");
        event.setDescription("Scoutläger för blåsare");
        event.setStartDate(LocalDateTime.of(2026, 7, 15, 10, 0));
        event.setEndDate(LocalDateTime.of(2026, 7, 20, 16, 0));
        event.setStreetAddress("Lägervägen 1");
        event.setPostalCode("12345");
        event.setCity("Naturby");
        event.setCapacity(150);
        event.setActive(true);
        // Note: createdAt/updatedAt set automatically by JPA
        return event;
    }

    /**
     * Create a test EventDTO
     */
    public static EventDTO createEventDTO(Long id, String name) {
        EventDTO dto = new EventDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Test description for " + name);
        dto.setStartDate(DEFAULT_START_DATE);
        dto.setEndDate(DEFAULT_END_DATE);
        dto.setStreetAddress("Testvägen 1");
        dto.setPostalCode("12345");
        dto.setCity("Teststad");
        dto.setCapacity(100);
        dto.setActive(true);
        return dto;
    }

    /**
     * Create a test Participant entity
     */
    public static Participant createParticipant(Long id, String firstName, String lastName) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setFirstName(firstName);
        participant.setLastName(lastName);
        participant.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
        participant.setPhone("070-1234567");
        participant.setBirthDate(LocalDate.of(2010, 5, 15));
        participant.setPersonalNumber("20100515-1234");
        participant.setStreetAddress("Testgatan 1");
        participant.setPostalCode("12345");
        participant.setCity("Teststad");
        return participant;
    }

    /**
     * Create a minor participant (under 18)
     */
    public static Participant createMinorParticipant(Long id, String firstName, String lastName) {
        Participant participant = createParticipant(id, firstName, lastName);
        participant.setBirthDate(LocalDate.now().minusYears(12));
        return participant;
    }

    /**
     * Create a test RegistrationDTO
     */
    public static RegistrationDTO createRegistrationDTO(Long id, String firstName, String lastName) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
        dto.setPhone("070-1234567");
        dto.setBirthDate("2010-05-15");
        dto.setPersonalNumber("20100515-1234");
        dto.setCity("Teststad");
        dto.setAllergies(new ArrayList<>());
        return dto;
    }
}