package com.eventmanager.repository;

import com.eventmanager.entity.Registration;
import com.eventmanager.entity.RegistrationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for Registration entity using Jakarta Persistence
@ApplicationScoped
public class RegistrationRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find all registrations ordered by date descending
    public List<Registration> findAll() {
        return em.createNamedQuery("Registration.findAll", Registration.class)
            .getResultList();
    }

    // Find registration by ID
    public Optional<Registration> findById(Long id) {
        Registration registration = em.find(Registration.class, id);
        return Optional.ofNullable(registration);
    }

    // Find registrations by event ID
    public List<Registration> findByEventId(Long eventId) {
        return em.createNamedQuery("Registration.findByEvent", Registration.class)
            .setParameter("eventId", eventId)
            .getResultList();
    }

    // Find registrations by participant ID
    public List<Registration> findByParticipantId(Long participantId) {
        return em.createNamedQuery("Registration.findByParticipant", Registration.class)
            .setParameter("participantId", participantId)
            .getResultList();
    }

    // Find registrations by status
    public List<Registration> findByStatus(RegistrationStatus status) {
        return em.createNamedQuery("Registration.findByStatus", Registration.class)
            .setParameter("status", status)
            .getResultList();
    }

    // Find registrations by event ID and status
    public List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status) {
        return em.createNamedQuery("Registration.findByEventAndStatus", Registration.class)
            .setParameter("eventId", eventId)
            .setParameter("status", status)
            .getResultList();
    }

    // Find registration by event ID and participant ID
    public Optional<Registration> findByEventIdAndParticipantId(Long eventId, Long participantId) {
        try {
            Registration registration = em.createNamedQuery("Registration.findByEventAndParticipant", Registration.class)
                .setParameter("eventId", eventId)
                .setParameter("participantId", participantId)
                .getSingleResult();
            return Optional.of(registration);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Count confirmed registrations by event ID
    public long countConfirmedByEventId(Long eventId) {
        return em.createQuery(
            "SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = :status",
            Long.class)
            .setParameter("eventId", eventId)
            .setParameter("status", RegistrationStatus.CONFIRMED)
            .getSingleResult();
    }

    // Find registration by ID with event and participant eagerly loaded
    public Optional<Registration> findByIdWithDetails(Long id) {
        try {
            Registration registration = em.createQuery(
                "SELECT r FROM Registration r LEFT JOIN FETCH r.event LEFT JOIN FETCH r.participant WHERE r.id = :id",
                Registration.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(registration);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Check if registration exists for event and participant
    public boolean existsByEventIdAndParticipantId(Long eventId, Long participantId) {
        Long count = em.createQuery(
            "SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.participant.id = :participantId",
            Long.class)
            .setParameter("eventId", eventId)
            .setParameter("participantId", participantId)
            .getSingleResult();
        return count > 0;
    }

    // Save a new registration
    @Transactional
    public Registration save(Registration registration) {
        if (registration.getId() == null) {
            em.persist(registration);
            return registration;
        } else {
            return em.merge(registration);
        }
    }

    // Update an existing registration
    @Transactional
    public Registration update(Registration registration) {
        return em.merge(registration);
    }

    // Delete a registration by ID
    @Transactional
    public void deleteById(Long id) {
        Registration registration = em.find(Registration.class, id);
        if (registration != null) {
            em.remove(registration);
        }
    }

    // Check if a registration exists by ID
    public boolean existsById(Long id) {
        return em.find(Registration.class, id) != null;
    }

    // Count total registrations
    public long count() {
        return em.createQuery("SELECT COUNT(r) FROM Registration r", Long.class)
            .getSingleResult();
    }

    // Count registrations by event ID
    public long countByEventId(Long eventId) {
        return em.createQuery(
            "SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId",
            Long.class)
            .setParameter("eventId", eventId)
            .getSingleResult();
    }

    // Find pending registrations (waitlist candidates)
    public List<Registration> findPendingByEventId(Long eventId) {
        return em.createQuery(
            "SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = :status ORDER BY r.registrationDate ASC",
            Registration.class)
            .setParameter("eventId", eventId)
            .setParameter("status", RegistrationStatus.PENDING)
            .getResultList();
    }

    // Flush pending changes
    public void flush() {
        em.flush();
    }
}
