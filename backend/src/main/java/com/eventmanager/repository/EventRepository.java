package com.eventmanager.repository;

import com.eventmanager.entity.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repository for Event entity using Jakarta Persistence
@ApplicationScoped
public class EventRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find all events ordered by start date descending
    public List<Event> findAll() {
        return em.createNamedQuery("Event.findAll", Event.class)
                .getResultList();
    }

    // Find event by ID
    public Optional<Event> findById(Long id) {
        Event event = em.find(Event.class, id);
        return Optional.ofNullable(event);
    }

    // Find upcoming events (start date > now)
    public List<Event> findUpcoming() {
        return em.createNamedQuery("Event.findUpcoming", Event.class)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    // Find active events
    public List<Event> findActive() {
        return em.createQuery(
                        "SELECT e FROM Event e WHERE e.active = true ORDER BY e.startDate DESC", Event.class)
                .getResultList();
    }

    // Save a new event
    @Transactional
    public Event save(Event event) {
        if (event.getId() == null) {
            em.persist(event);
            return event;
        } else {
            return em.merge(event);
        }
    }

    // Update an existing event
    @Transactional
    public Event update(Event event) {
        return em.merge(event);
    }

    // Delete an event by ID
    @Transactional
    public void deleteById(Long id) {
        Event event = em.find(Event.class, id);
        if (event != null) {
            em.remove(event);
        }
    }

    // Check if an event exists by ID
    public boolean existsById(Long id) {
        return em.find(Event.class, id) != null;
    }

    // Count total events
    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM Event e", Long.class)
                .getSingleResult();
    }

    // Find events by date range
    public List<Event> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                        "SELECT e FROM Event e WHERE e.startDate >= :start AND e.startDate <= :end ORDER BY e.startDate ASC",
                        Event.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    // Flush pending changes
    public void flush() {
        em.flush();
    }
}