package com.eventmanager.repository;

import com.eventmanager.entity.Troop;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

// Repository for Troop entity operations
@ApplicationScoped
public class TroopRepository {

    @PersistenceContext
    private EntityManager em;

    // Find all troops ordered by sortOrder
    public List<Troop> findAll() {
        TypedQuery<Troop> query = em.createNamedQuery("Troop.findAll", Troop.class);
        return query.getResultList();
    }

    // Find by ID
    public Optional<Troop> findById(Long id) {
        Troop troop = em.find(Troop.class, id);
        return Optional.ofNullable(troop);
    }

    // Find by name
    public Optional<Troop> findByName(String name) {
        TypedQuery<Troop> query = em.createNamedQuery("Troop.findByName", Troop.class);
        query.setParameter("name", name);
        List<Troop> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Check if troop exists by name
    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }

    // Get max sort order
    public int getMaxSortOrder() {
        TypedQuery<Integer> query = em.createQuery(
                "SELECT COALESCE(MAX(t.sortOrder), 0) FROM Troop t",
                Integer.class
        );
        Integer result = query.getSingleResult();
        return result != null ? result : 0;
    }

    // Save a new troop
    public Troop save(Troop troop) {
        em.persist(troop);
        em.flush();
        return troop;
    }

    // Update an existing troop
    public Troop update(Troop troop) {
        return em.merge(troop);
    }

    // Delete by ID
    public boolean deleteById(Long id) {
        Troop troop = em.find(Troop.class, id);
        if (troop != null) {
            em.remove(troop);
            return true;
        }
        return false;
    }
}