package com.eventmanager.service;

import com.eventmanager.dto.EventDTO;
import com.eventmanager.dto.EventPatchDTO;
import com.eventmanager.entity.Event;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Service layer for Event business logic
@ApplicationScoped
public class EventService {

    @Inject
    private EventRepository eventRepository;

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Get all events as DTOs with registration counts
    @Transactional
    public List<EventDTO> findAll() {
        return eventRepository.findAll().stream()
                .map(event -> toEventDTOWithCount(event))
                .toList();
    }

    // Get event by ID as DTO with registration count
    @Transactional
    public EventDTO findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return toEventDTOWithCount(event);
    }

    // Get event by ID as Optional (for REST resource)
    @Transactional
    public Optional<EventDTO> findByIdOptional(Long id) {
        return eventRepository.findById(id)
                .map(this::toEventDTOWithCount);
    }

    // Get event entity by ID (for internal use)
    @Transactional
    public Event getEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    // Get upcoming events
    @Transactional
    public List<EventDTO> findUpcoming() {
        return eventRepository.findUpcoming().stream()
                .map(event -> toEventDTOWithCount(event))
                .toList();
    }

    // Get active events
    @Transactional
    public List<EventDTO> findActive() {
        return eventRepository.findActive().stream()
                .map(event -> toEventDTOWithCount(event))
                .toList();
    }

    // Create a new event
    @Transactional
    public EventDTO create(EventDTO eventDTO) {
        Event event = eventDTO.toEntity();
        Event savedEvent = eventRepository.save(event);
        return toEventDTOWithCount(savedEvent);
    }

    // Update an existing event (full update)
    @Transactional
    public EventDTO update(Long id, EventDTO eventDTO) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        eventDTO.updateEntity(existingEvent);
        Event updatedEvent = eventRepository.update(existingEvent);
        return toEventDTOWithCount(updatedEvent);
    }

    // Update as Optional (for REST resource)
    @Transactional
    public Optional<EventDTO> updateOptional(Long id, EventDTO eventDTO) {
        return eventRepository.findById(id)
                .map(existingEvent -> {
                    eventDTO.updateEntity(existingEvent);
                    Event updatedEvent = eventRepository.update(existingEvent);
                    return toEventDTOWithCount(updatedEvent);
                });
    }

    // Partial update (PATCH) - only updates non-null fields
    @Transactional
    public Optional<EventDTO> patch(Long id, EventPatchDTO patchDTO) {
        return eventRepository.findById(id)
                .map(event -> {
                    // Only update fields that are present in the patch
                    if (patchDTO.getActive() != null) {
                        event.setActive(patchDTO.getActive());
                    }
                    if (patchDTO.getName() != null) {
                        event.setName(patchDTO.getName());
                    }
                    if (patchDTO.getDescription() != null) {
                        event.setDescription(patchDTO.getDescription());
                    }

                    Event updatedEvent = eventRepository.update(event);
                    return toEventDTOWithCount(updatedEvent);
                });
    }

    // Delete an event
    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
    }

    // Delete as boolean (for REST resource)
    @Transactional
    public boolean deleteIfExists(Long id) {
        if (!eventRepository.existsById(id)) {
            return false;
        }
        eventRepository.deleteById(id);
        return true;
    }

    // Check if event has available capacity
    @Transactional
    public boolean hasAvailableCapacity(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        int count = getRegistrationCount(eventId);
        return event.getCapacity() == null || count < event.getCapacity();
    }

    // Get remaining spots for an event
    @Transactional
    public int getRemainingSpots(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        int count = getRegistrationCount(eventId);
        return event.getCapacity() != null ? event.getCapacity() - count : Integer.MAX_VALUE;
    }

    // Count total events
    public long count() {
        return eventRepository.count();
    }

    // Helper method to get registration count for an event
    private int getRegistrationCount(Long eventId) {
        Long count = em.createQuery(
                        "SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId", Long.class)
                .setParameter("eventId", eventId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    // Helper method to create EventDTO with proper registration count
    private EventDTO toEventDTOWithCount(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setStreetAddress(event.getStreetAddress());
        dto.setPostalCode(event.getPostalCode());
        dto.setCity(event.getCity());
        dto.setCapacity(event.getCapacity());
        dto.setActive(event.getActive());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        // Get registration count with separate query (avoids lazy loading issues)
        int count = getRegistrationCount(event.getId());
        dto.setRegistrationCount(count);
        dto.setRemainingSpots(event.getCapacity() != null ? event.getCapacity() - count : null);

        return dto;
    }
}