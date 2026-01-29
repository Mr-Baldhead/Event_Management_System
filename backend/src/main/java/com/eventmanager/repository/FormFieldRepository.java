package com.eventmanager.repository;

import com.eventmanager.entity.FormField;
import com.eventmanager.entity.FieldType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for FormField entity operations
@ApplicationScoped
public class FormFieldRepository {

    @PersistenceContext
    private EntityManager em;

    // Find all fields for an event
    public List<FormField> findByEventId(Long eventId) {
        TypedQuery<FormField> query = em.createNamedQuery("FormField.findByEventId", FormField.class);
        query.setParameter("eventId", eventId);
        return query.getResultList();
    }

    // Find by ID
    public Optional<FormField> findById(Long id) {
        FormField field = em.find(FormField.class, id);
        return Optional.ofNullable(field);
    }

    // Find by event ID and type
    public List<FormField> findByEventIdAndType(Long eventId, FieldType fieldType) {
        TypedQuery<FormField> query = em.createNamedQuery("FormField.findByEventIdAndType", FormField.class);
        query.setParameter("eventId", eventId);
        query.setParameter("fieldType", fieldType);
        return query.getResultList();
    }

    // Get max sort order for an event
    public int getMaxSortOrder(Long eventId) {
        TypedQuery<Integer> query = em.createQuery(
                "SELECT COALESCE(MAX(f.sortOrder), 0) FROM FormField f WHERE f.event.id = :eventId",
                Integer.class
        );
        query.setParameter("eventId", eventId);
        return query.getSingleResult();
    }

    // Get max row number for an event
    public int getMaxRowIndex(Long eventId) {
        TypedQuery<Integer> query = em.createQuery(
                "SELECT COALESCE(MAX(f.rowIndex), -1) FROM FormField f WHERE f.event.id = :eventId",
                Integer.class
        );
        query.setParameter("eventId", eventId);
        return query.getSingleResult();
    }

    // Save a new field
    @Transactional
    public FormField save(FormField field) {
        em.persist(field);
        em.flush();
        return field;
    }

    // Update an existing field
    @Transactional
    public FormField update(FormField field) {
        return em.merge(field);
    }

    // Delete a field
    @Transactional
    public void delete(FormField field) {
        if (!em.contains(field)) {
            field = em.merge(field);
        }
        em.remove(field);
    }

    // Delete by ID
    @Transactional
    public boolean deleteById(Long id) {
        FormField field = em.find(FormField.class, id);
        if (field != null) {
            em.remove(field);
            return true;
        }
        return false;
    }

    // Delete all fields for an event
    @Transactional
    public int deleteByEventId(Long eventId) {
        return em.createQuery("DELETE FROM FormField f WHERE f.event.id = :eventId")
                .setParameter("eventId", eventId)
                .executeUpdate();
    }

    // Check if field exists
    public boolean existsById(Long id) {
        return em.find(FormField.class, id) != null;
    }

    // Count fields for an event
    public long countByEventId(Long eventId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(f) FROM FormField f WHERE f.event.id = :eventId",
                Long.class
        );
        query.setParameter("eventId", eventId);
        return query.getSingleResult();
    }
}