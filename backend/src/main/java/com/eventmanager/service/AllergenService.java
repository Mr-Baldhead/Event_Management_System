package com.eventmanager.service;

import com.eventmanager.dto.AllergenDTO;
import com.eventmanager.entity.Allergen;
import com.eventmanager.entity.AllergenSeverity;
import com.eventmanager.entity.Event;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.AllergenRepository;
import com.eventmanager.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

// Service layer for Allergen business logic
@ApplicationScoped
public class AllergenService {

    @Inject
    private AllergenRepository allergenRepository;

    @Inject
    private EventRepository eventRepository;

    // Get all allergens as DTOs
    public List<AllergenDTO> findAll() {
        return allergenRepository.findAll().stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Get allergen by ID as DTO
    public AllergenDTO findById(Long id) {
        Allergen allergen = allergenRepository.findByIdWithParticipants(id)
            .orElseThrow(() -> new ResourceNotFoundException("Allergen", id));
        return new AllergenDTO(allergen);
    }

    // Get allergen entity by ID (for internal use)
    public Allergen getEntityById(Long id) {
        return allergenRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Allergen", id));
    }

    // Get allergens by event ID
    public List<AllergenDTO> findByEventId(Long eventId) {
        return allergenRepository.findByEventId(eventId).stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Get global allergens (not tied to any event)
    public List<AllergenDTO> findGlobal() {
        return allergenRepository.findGlobal().stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Get critical allergens (HIGH or CRITICAL severity)
    public List<AllergenDTO> findCritical() {
        return allergenRepository.findCritical().stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Get allergens by severity
    public List<AllergenDTO> findBySeverity(AllergenSeverity severity) {
        return allergenRepository.findBySeverity(severity).stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Create a new allergen
    @Transactional
    public AllergenDTO create(AllergenDTO allergenDTO) {
        Allergen allergen = allergenDTO.toEntity();

        // Set event if provided
        if (allergenDTO.getEventId() != null) {
            Event event = eventRepository.findById(allergenDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", allergenDTO.getEventId()));
            allergen.setEvent(event);
        }

        Allergen savedAllergen = allergenRepository.save(allergen);
        return new AllergenDTO(savedAllergen);
    }

    // Update an existing allergen
    @Transactional
    public AllergenDTO update(Long id, AllergenDTO allergenDTO) {
        Allergen existingAllergen = allergenRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Allergen", id));

        allergenDTO.updateEntity(existingAllergen);

        // Update event if provided
        if (allergenDTO.getEventId() != null) {
            Event event = eventRepository.findById(allergenDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", allergenDTO.getEventId()));
            existingAllergen.setEvent(event);
        } else {
            existingAllergen.setEvent(null);
        }

        Allergen updatedAllergen = allergenRepository.update(existingAllergen);
        return new AllergenDTO(updatedAllergen);
    }

    // Delete an allergen
    @Transactional
    public void delete(Long id) {
        if (!allergenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Allergen", id);
        }
        allergenRepository.deleteById(id);
    }

    // Search allergens by name
    public List<AllergenDTO> searchByName(String searchTerm) {
        return allergenRepository.searchByName(searchTerm).stream()
            .map(AllergenDTO::new)
            .toList();
    }

    // Count total allergens
    public long count() {
        return allergenRepository.count();
    }
}
