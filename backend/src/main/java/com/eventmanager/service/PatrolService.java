package com.eventmanager.service;

import com.eventmanager.dto.PatrolDTO;
import com.eventmanager.entity.Event;
import com.eventmanager.entity.Patrol;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.PatrolRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

// Service layer for Patrol (scout troop) business logic
@ApplicationScoped
public class PatrolService {

    @Inject
    private PatrolRepository patrolRepository;

    @Inject
    private EventRepository eventRepository;

    // Get all patrols as DTOs
    public List<PatrolDTO> findAll() {
        return patrolRepository.findAll().stream()
            .map(PatrolDTO::new)
            .toList();
    }

    // Get patrol by ID as DTO
    public PatrolDTO findById(Long id) {
        Patrol patrol = patrolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patrol", id));
        return new PatrolDTO(patrol);
    }

    // Get patrol entity by ID (for internal use)
    public Patrol getEntityById(Long id) {
        return patrolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patrol", id));
    }

    // Get patrols by event ID
    public List<PatrolDTO> findByEventId(Long eventId) {
        return patrolRepository.findByEventId(eventId).stream()
            .map(PatrolDTO::new)
            .toList();
    }

    // Get patrol with participants
    public PatrolDTO findByIdWithParticipants(Long id) {
        Patrol patrol = patrolRepository.findByIdWithParticipants(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patrol", id));
        return new PatrolDTO(patrol);
    }

    // Create a new patrol
    @Transactional
    public PatrolDTO create(PatrolDTO patrolDTO) {
        Patrol patrol = patrolDTO.toEntity();

        // Set event if provided
        if (patrolDTO.getEventId() != null) {
            Event event = eventRepository.findById(patrolDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", patrolDTO.getEventId()));
            patrol.setEvent(event);
        }

        Patrol savedPatrol = patrolRepository.save(patrol);
        return new PatrolDTO(savedPatrol);
    }

    // Update an existing patrol
    @Transactional
    public PatrolDTO update(Long id, PatrolDTO patrolDTO) {
        Patrol existingPatrol = patrolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patrol", id));

        patrolDTO.updateEntity(existingPatrol);

        // Update event if provided
        if (patrolDTO.getEventId() != null) {
            Event event = eventRepository.findById(patrolDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", patrolDTO.getEventId()));
            existingPatrol.setEvent(event);
        }

        Patrol updatedPatrol = patrolRepository.update(existingPatrol);
        return new PatrolDTO(updatedPatrol);
    }

    // Delete a patrol
    @Transactional
    public void delete(Long id) {
        if (!patrolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patrol", id);
        }
        patrolRepository.deleteById(id);
    }

    // Search patrols by name
    public List<PatrolDTO> searchByName(String searchTerm) {
        return patrolRepository.searchByName(searchTerm).stream()
            .map(PatrolDTO::new)
            .toList();
    }

    // Count total patrols
    public long count() {
        return patrolRepository.count();
    }

    // Count patrols by event
    public long countByEventId(Long eventId) {
        return patrolRepository.countByEventId(eventId);
    }
}
