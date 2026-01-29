package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// JPA Entity representing a user session
@Entity
@Table(name = "sessions")
@NamedQueries({
    @NamedQuery(
        name = "Session.findByToken",
        query = "SELECT s FROM Session s WHERE s.token = :token"
    ),
    @NamedQuery(
        name = "Session.findByUserId",
        query = "SELECT s FROM Session s WHERE s.user.id = :userId ORDER BY s.createdAt DESC"
    ),
    @NamedQuery(
        name = "Session.deleteExpired",
        query = "DELETE FROM Session s WHERE s.expiresAt < :now"
    ),
    @NamedQuery(
        name = "Session.deleteByUserId",
        query = "DELETE FROM Session s WHERE s.user.id = :userId"
    ),
    @NamedQuery(
        name = "Session.findValidByToken",
        query = "SELECT s FROM Session s WHERE s.token = :token AND s.expiresAt > :now"
    )
})
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Token is required")
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Expiration time is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivity = createdAt;
    }

    // Default constructor
    public Session() {
    }

    // Constructor with required fields
    public Session(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Helper method to check if session is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Helper method to check if session is valid
    public boolean isValid() {
        return !isExpired() && user != null && !user.getLocked();
    }

    // Helper method to update last activity and extend session
    public void updateActivity(int sessionTimeoutMinutes) {
        this.lastActivity = LocalDateTime.now();
        this.expiresAt = this.lastActivity.plusMinutes(sessionTimeoutMinutes);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
