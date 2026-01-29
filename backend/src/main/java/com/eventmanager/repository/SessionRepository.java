package com.eventmanager.repository;

import com.eventmanager.entity.Session;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repository for Session entity database operations
@ApplicationScoped
public class SessionRepository {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    // Find session by token
    public Optional<Session> findByToken(String token) {
        try {
            TypedQuery<Session> query = em.createNamedQuery("Session.findByToken", Session.class);
            query.setParameter("token", token);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find valid session by token (not expired)
    public Optional<Session> findValidByToken(String token) {
        try {
            TypedQuery<Session> query = em.createNamedQuery("Session.findValidByToken", Session.class);
            query.setParameter("token", token);
            query.setParameter("now", LocalDateTime.now());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    // Find sessions by user ID
    public List<Session> findByUserId(Long userId) {
        TypedQuery<Session> query = em.createNamedQuery("Session.findByUserId", Session.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    // Save new session
    @Transactional
    public Session save(Session session) {
        em.persist(session);
        em.flush();
        return session;
    }

    // Update session
    @Transactional
    public Session update(Session session) {
        return em.merge(session);
    }

    // Delete session by token
    @Transactional
    public void deleteByToken(String token) {
        findByToken(token).ifPresent(session -> em.remove(session));
    }

    // Delete all sessions for a user
    @Transactional
    public int deleteByUserId(Long userId) {
        return em.createNamedQuery("Session.deleteByUserId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // Delete expired sessions
    @Transactional
    public int deleteExpired() {
        return em.createNamedQuery("Session.deleteExpired")
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }
}
