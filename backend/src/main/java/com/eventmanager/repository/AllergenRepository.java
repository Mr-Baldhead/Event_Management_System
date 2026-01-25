package com.eventmanager.repository;

import com.eventmanager.entity.Allergen;
import com.eventmanager.entity.AllergenSeverity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for Allergen entity using Jakarta Persistence
@ApplicationScoped
public class AllergenRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find all allergens ordered by name
    public List<Allergen> findAll() {
        return em.createNamedQuery("Allergen.findAll", Allergen.class)
            .getResultList();
    }

    // Find allergen by ID
    public Optional<Allergen> findById(Long id) {
        Allergen allergen = em.find(Allergen.class, id);
        return Optional.ofNullable(allergen);
    }

    // Find allergens by event ID
    public List<Allergen> findByEventId(Long eventId) {
        return em.createNamedQuery("Allergen.findByEvent", Allergen.class)
            .setParameter("eventId", eventId)
            .getResultList();
    }

    // Find global allergens (not tied to any event)
    public List<Allergen> findGlobal() {
        return em.createNamedQuery("Allergen.findGlobal", Allergen.class)
            .getResultList();
    }

    // Find critical allergens (HIGH or CRITICAL severity)
    public List<Allergen> findCritical() {
        return em.createQuery(
            "SELECT a FROM Allergen a WHERE a.severity IN (:high, :critical) ORDER BY a.name ASC",
            Allergen.class)
            .setParameter("high", AllergenSeverity.HIGH)
            .setParameter("critical", AllergenSeverity.CRITICAL)
            .getResultList();
    }

    // Find allergens by severity
    public List<Allergen> findBySeverity(AllergenSeverity severity) {
        return em.createNamedQuery("Allergen.findCritical", Allergen.class)
            .setParameter("severity", severity)
            .getResultList();
    }

    // Find allergen by ID with participants eagerly loaded
    public Optional<Allergen> findByIdWithParticipants(Long id) {
        try {
            Allergen allergen = em.createQuery(
                "SELECT a FROM Allergen a LEFT JOIN FETCH a.participants WHERE a.id = :id",
                Allergen.class)
                .setParameter("id", id)
                .getSingleResult();
            return Optional.of(allergen);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Save a new allergen
    @Transactional
    public Allergen save(Allergen allergen) {
        if (allergen.getId() == null) {
            em.persist(allergen);
            return allergen;
        } else {
            return em.merge(allergen);
        }
    }

    // Update an existing allergen
    @Transactional
    public Allergen update(Allergen allergen) {
        return em.merge(allergen);
    }

    // Delete an allergen by ID
    @Transactional
    public void deleteById(Long id) {
        Allergen allergen = em.find(Allergen.class, id);
        if (allergen != null) {
            em.remove(allergen);
        }
    }

    // Check if an allergen exists by ID
    public boolean existsById(Long id) {
        return em.find(Allergen.class, id) != null;
    }

    // Count total allergens
    public long count() {
        return em.createQuery("SELECT COUNT(a) FROM Allergen a", Long.class)
            .getSingleResult();
    }

    // Search allergens by name
    public List<Allergen> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return em.createQuery(
            "SELECT a FROM Allergen a WHERE LOWER(a.name) LIKE :pattern ORDER BY a.name",
            Allergen.class)
            .setParameter("pattern", pattern)
            .getResultList();
    }

    // Find allergens by name (exact match)
    public Optional<Allergen> findByName(String name) {
        try {
            Allergen allergen = em.createQuery(
                "SELECT a FROM Allergen a WHERE a.name = :name",
                Allergen.class)
                .setParameter("name", name)
                .getSingleResult();
            return Optional.of(allergen);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Flush pending changes
    public void flush() {
        em.flush();
    }
}
