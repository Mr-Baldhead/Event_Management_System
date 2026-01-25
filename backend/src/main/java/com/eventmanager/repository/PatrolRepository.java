package com.eventmanager.repository;

import com.eventmanager.entity.Patrol;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for Patrol entity using Jakarta Persistence
@ApplicationScoped
public class PatrolRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find all patrols ordered by name
    public List<Patrol> findAll() {
        return em.createNamedQuery("Patrol.findAll", Patrol.class)
            .getResultList();
    }

    // Find patrol by ID
    public Optional<Patrol> findById(Long id) {
        Patrol patrol = em.find(Patrol.class, id);
        return Optional.ofNullable(patrol);
    }

    // Find patrols by event ID
    public List<Patrol> findByEventId(Long eventId) {
        return em.createNamedQuery("Patrol.findByEvent", Patrol.class)
            .setParameter("eventId", eventId)
            .getResultList();
    }

    // Find patrol by name and event ID
    public Optional<Patrol> findByNameAndEventId(String name, Long eventId) {
        try {
            Patrol patrol = em.createNamedQuery("Patrol.findByName", Patrol.class)
                .setParameter("name", name)
                .setParameter("eventId", eventId)
                .getSingleResult();
            return Optional.of(patrol);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find patrol by ID with participants eagerly loaded
    public Optional<Patrol> findByIdWithParticipants(Long id) {
        try {
            Patrol patrol = em.createQuery(
                "SELECT p FROM Patrol p LEFT JOIN FETCH p.participants WHERE p.id = :id",
                Patrol.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(patrol);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Save a new patrol
    @Transactional
    public Patrol save(Patrol patrol) {
        if (patrol.getId() == null) {
            em.persist(patrol);
            return patrol;
        } else {
            return em.merge(patrol);
        }
    }

    // Update an existing patrol
    @Transactional
    public Patrol update(Patrol patrol) {
        return em.merge(patrol);
    }

    // Delete a patrol by ID
    @Transactional
    public void deleteById(Long id) {
        Patrol patrol = em.find(Patrol.class, id);
        if (patrol != null) {
            em.remove(patrol);
        }
    }

    // Check if a patrol exists by ID
    public boolean existsById(Long id) {
        return em.find(Patrol.class, id) != null;
    }

    // Count total patrols
    public long count() {
        return em.createQuery("SELECT COUNT(p) FROM Patrol p", Long.class)
            .getSingleResult();
    }

    // Count patrols by event ID
    public long countByEventId(Long eventId) {
        return em.createQuery(
            "SELECT COUNT(p) FROM Patrol p WHERE p.event.id = :eventId",
            Long.class)
            .setParameter("eventId", eventId)
            .getSingleResult();
    }

    // Search patrols by name
    public List<Patrol> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return em.createQuery(
            "SELECT p FROM Patrol p WHERE LOWER(p.name) LIKE :pattern ORDER BY p.name",
            Patrol.class)
            .setParameter("pattern", pattern)
            .getResultList();
    }

    // Flush pending changes
    public void flush() {
        em.flush();
    }
}
