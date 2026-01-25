package com.eventmanager.repository;

import com.eventmanager.entity.Participant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for Participant entity using Jakarta Persistence
@ApplicationScoped
public class ParticipantRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find all participants ordered by name
    public List<Participant> findAll() {
        return em.createNamedQuery("Participant.findAll", Participant.class)
            .getResultList();
    }

    // Find participant by ID
    public Optional<Participant> findById(Long id) {
        Participant participant = em.find(Participant.class, id);
        return Optional.ofNullable(participant);
    }

    // Find participant by ID with allergens eagerly loaded
    public Optional<Participant> findByIdWithAllergens(Long id) {
        try {
            Participant participant = em.createNamedQuery("Participant.findWithAllergens", Participant.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(participant);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find participants by patrol ID
    public List<Participant> findByPatrolId(Long patrolId) {
        return em.createNamedQuery("Participant.findByPatrol", Participant.class)
            .setParameter("patrolId", patrolId)
            .getResultList();
    }

    // Find participants by event ID
    public List<Participant> findByEventId(Long eventId) {
        return em.createNamedQuery("Participant.findByEvent", Participant.class)
            .setParameter("eventId", eventId)
            .getResultList();
    }

    // Find participant by email
    public Optional<Participant> findByEmail(String email) {
        try {
            Participant participant = em.createNamedQuery("Participant.findByEmail", Participant.class)
                .setParameter("email", email)
                .getSingleResult();
            return Optional.of(participant);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find participants with allergens
    public List<Participant> findWithAllergens() {
        return em.createQuery(
            "SELECT DISTINCT p FROM Participant p LEFT JOIN FETCH p.allergens WHERE SIZE(p.allergens) > 0 ORDER BY p.lastName, p.firstName",
            Participant.class)
            .getResultList();
    }

    // Find minors (under 18)
    public List<Participant> findMinors() {
        return em.createQuery(
            "SELECT p FROM Participant p WHERE p.birthDate IS NOT NULL ORDER BY p.lastName, p.firstName",
            Participant.class)
            .getResultList()
            .stream()
            .filter(Participant::isMinor)
            .toList();
    }

    // Save a new participant
    @Transactional
    public Participant save(Participant participant) {
        if (participant.getId() == null) {
            em.persist(participant);
            return participant;
        } else {
            return em.merge(participant);
        }
    }

    // Update an existing participant
    @Transactional
    public Participant update(Participant participant) {
        return em.merge(participant);
    }

    // Delete a participant by ID
    @Transactional
    public void deleteById(Long id) {
        Participant participant = em.find(Participant.class, id);
        if (participant != null) {
            em.remove(participant);
        }
    }

    // Check if a participant exists by ID
    public boolean existsById(Long id) {
        return em.find(Participant.class, id) != null;
    }

    // Count total participants
    public long count() {
        return em.createQuery("SELECT COUNT(p) FROM Participant p", Long.class)
            .getSingleResult();
    }

    // Search participants by name
    public List<Participant> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return em.createQuery(
            "SELECT p FROM Participant p WHERE LOWER(p.firstName) LIKE :pattern OR LOWER(p.lastName) LIKE :pattern ORDER BY p.lastName, p.firstName",
            Participant.class)
            .setParameter("pattern", pattern)
            .getResultList();
    }

    // Flush pending changes
    public void flush() {
        em.flush();
    }
}
