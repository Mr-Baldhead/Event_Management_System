package com.eventmanager.repository;

import com.eventmanager.entity.User;
import com.eventmanager.entity.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// Repository for User entity database operations
@ApplicationScoped
public class UserRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find user by email
    public Optional<User> findByEmail(String email) {
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findByEmail", User.class);
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find user by ID
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }

    // Find all users
    public List<User> findAll() {
        TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }

    // Find users by role
    public List<User> findByRole(UserRole role) {
        TypedQuery<User> query = em.createNamedQuery("User.findByRole", User.class);
        query.setParameter("role", role);
        return query.getResultList();
    }

    // Count users by role
    public long countByRole(UserRole role) {
        TypedQuery<Long> query = em.createNamedQuery("User.countByRole", Long.class);
        query.setParameter("role", role);
        return query.getSingleResult();
    }

    // Save new user
    @Transactional
    public User save(User user) {
        em.persist(user);
        em.flush();
        return user;
    }

    // Update existing user
    @Transactional
    public User update(User user) {
        return em.merge(user);
    }

    // Delete user by ID
    @Transactional
    public void deleteById(Long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    // Check if email exists
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
