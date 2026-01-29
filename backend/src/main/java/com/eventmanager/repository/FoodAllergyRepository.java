package com.eventmanager.repository;

import com.eventmanager.entity.FoodAllergy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

// Repository for FoodAllergy entity operations
@ApplicationScoped
public class FoodAllergyRepository {

    @PersistenceContext
    private EntityManager em;

    // Find all allergies ordered by sortOrder
    public List<FoodAllergy> findAll() {
        TypedQuery<FoodAllergy> query = em.createNamedQuery("FoodAllergy.findAll", FoodAllergy.class);
        return query.getResultList();
    }

    // Find by ID
    public Optional<FoodAllergy> findById(Long id) {
        FoodAllergy allergy = em.find(FoodAllergy.class, id);
        return Optional.ofNullable(allergy);
    }

    // Find by name
    public Optional<FoodAllergy> findByName(String name) {
        TypedQuery<FoodAllergy> query = em.createNamedQuery("FoodAllergy.findByName", FoodAllergy.class);
        query.setParameter("name", name);
        List<FoodAllergy> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Check if allergy exists by name
    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }

    // Get max sort order
    public int getMaxSortOrder() {
        TypedQuery<Integer> query = em.createQuery(
                "SELECT COALESCE(MAX(f.sortOrder), 0) FROM FoodAllergy f",
                Integer.class
        );
        Integer result = query.getSingleResult();
        return result != null ? result : 0;
    }

    // Save a new allergy
    public FoodAllergy save(FoodAllergy allergy) {
        em.persist(allergy);
        em.flush();
        return allergy;
    }

    // Update an existing allergy
    public FoodAllergy update(FoodAllergy allergy) {
        return em.merge(allergy);
    }

    // Delete by ID
    public boolean deleteById(Long id) {
        FoodAllergy allergy = em.find(FoodAllergy.class, id);
        if (allergy != null) {
            em.remove(allergy);
            return true;
        }
        return false;
    }
}