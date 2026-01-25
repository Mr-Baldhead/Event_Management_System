package com.eventmanager.service;

import com.eventmanager.dto.ParticipantDTO;
import com.eventmanager.entity.Allergen;
import com.eventmanager.entity.Participant;
import com.eventmanager.entity.Patrol;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.AllergenRepository;
import com.eventmanager.repository.ParticipantRepository;
import com.eventmanager.repository.PatrolRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

// Service layer for Participant business logic
@ApplicationScoped
public class ParticipantService {

    @Inject
    private ParticipantRepository participantRepository;

    @Inject
    private PatrolRepository patrolRepository;

    @Inject
    private AllergenRepository allergenRepository;

    // Get all participants as DTOs
    public List<ParticipantDTO> findAll() {
        return participantRepository.findAll().stream()
            .map(ParticipantDTO::new)
            .toList();
    }

    // Get participant by ID as DTO
    public ParticipantDTO findById(Long id) {
        Participant participant = participantRepository.findByIdWithAllergens(id)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", id));
        return new ParticipantDTO(participant);
    }

    // Get participant entity by ID (for internal use)
    public Participant getEntityById(Long id) {
        return participantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", id));
    }

    // Get participants by patrol ID
    public List<ParticipantDTO> findByPatrolId(Long patrolId) {
        return participantRepository.findByPatrolId(patrolId).stream()
            .map(ParticipantDTO::new)
            .toList();
    }

    // Get participants by event ID
    public List<ParticipantDTO> findByEventId(Long eventId) {
        return participantRepository.findByEventId(eventId).stream()
            .map(ParticipantDTO::new)
            .toList();
    }

    // Get participants with allergens
    public List<ParticipantDTO> findWithAllergens() {
        return participantRepository.findWithAllergens().stream()
            .map(ParticipantDTO::new)
            .toList();
    }

    // Create a new participant
    @Transactional
    public ParticipantDTO create(ParticipantDTO participantDTO) {
        Participant participant = participantDTO.toEntity();

        // Set patrol if provided
        if (participantDTO.getPatrolId() != null) {
            Patrol patrol = patrolRepository.findById(participantDTO.getPatrolId())
                .orElseThrow(() -> new ResourceNotFoundException("Patrol", participantDTO.getPatrolId()));
            participant.setPatrol(patrol);
        }

        Participant savedParticipant = participantRepository.save(participant);
        return new ParticipantDTO(savedParticipant);
    }

    // Update an existing participant
    @Transactional
    public ParticipantDTO update(Long id, ParticipantDTO participantDTO) {
        Participant existingParticipant = participantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", id));

        participantDTO.updateEntity(existingParticipant);

        // Update patrol if provided
        if (participantDTO.getPatrolId() != null) {
            Patrol patrol = patrolRepository.findById(participantDTO.getPatrolId())
                .orElseThrow(() -> new ResourceNotFoundException("Patrol", participantDTO.getPatrolId()));
            existingParticipant.setPatrol(patrol);
        } else {
            existingParticipant.setPatrol(null);
        }

        Participant updatedParticipant = participantRepository.update(existingParticipant);
        return new ParticipantDTO(updatedParticipant);
    }

    // Delete a participant
    @Transactional
    public void delete(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Participant", id);
        }
        participantRepository.deleteById(id);
    }

    // Add allergen to participant
    @Transactional
    public ParticipantDTO addAllergen(Long participantId, Long allergenId) {
        Participant participant = participantRepository.findByIdWithAllergens(participantId)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", participantId));
        Allergen allergen = allergenRepository.findById(allergenId)
            .orElseThrow(() -> new ResourceNotFoundException("Allergen", allergenId));

        participant.addAllergen(allergen);
        Participant updatedParticipant = participantRepository.update(participant);
        return new ParticipantDTO(updatedParticipant);
    }

    // Remove allergen from participant
    @Transactional
    public ParticipantDTO removeAllergen(Long participantId, Long allergenId) {
        Participant participant = participantRepository.findByIdWithAllergens(participantId)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", participantId));
        Allergen allergen = allergenRepository.findById(allergenId)
            .orElseThrow(() -> new ResourceNotFoundException("Allergen", allergenId));

        participant.removeAllergen(allergen);
        Participant updatedParticipant = participantRepository.update(participant);
        return new ParticipantDTO(updatedParticipant);
    }

    // Set allergens for participant
    @Transactional
    public ParticipantDTO setAllergens(Long participantId, Set<Long> allergenIds) {
        Participant participant = participantRepository.findByIdWithAllergens(participantId)
            .orElseThrow(() -> new ResourceNotFoundException("Participant", participantId));

        // Clear existing allergens
        participant.getAllergens().clear();

        // Add new allergens
        for (Long allergenId : allergenIds) {
            Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergen", allergenId));
            participant.addAllergen(allergen);
        }

        Participant updatedParticipant = participantRepository.update(participant);
        return new ParticipantDTO(updatedParticipant);
    }

    // Search participants by name
    public List<ParticipantDTO> searchByName(String searchTerm) {
        return participantRepository.searchByName(searchTerm).stream()
            .map(ParticipantDTO::new)
            .toList();
    }

    // Count total participants
    public long count() {
        return participantRepository.count();
    }
}
