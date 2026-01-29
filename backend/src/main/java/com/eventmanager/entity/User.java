package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// JPA Entity representing a user (SuperAdmin or Admin)
@Entity
@Table(name = "users")
@NamedQueries({
    @NamedQuery(
        name = "User.findByEmail",
        query = "SELECT u FROM User u WHERE u.email = :email"
    ),
    @NamedQuery(
        name = "User.findByRole",
        query = "SELECT u FROM User u WHERE u.role = :role ORDER BY u.lastName, u.firstName"
    ),
    @NamedQuery(
        name = "User.findAll",
        query = "SELECT u FROM User u ORDER BY u.lastName, u.firstName"
    ),
    @NamedQuery(
        name = "User.countByRole",
        query = "SELECT COUNT(u) FROM User u WHERE u.role = :role"
    )
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.ADMIN;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Column(nullable = false)
    private Boolean locked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Default constructor
    public User() {
    }

    // Constructor with required fields
    public User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Helper method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }

    // Helper method to get initials
    public String getInitials() {
        String first = firstName != null && !firstName.isEmpty() ? firstName.substring(0, 1).toUpperCase() : "";
        String last = lastName != null && !lastName.isEmpty() ? lastName.substring(0, 1).toUpperCase() : "";
        if (first.isEmpty() && last.isEmpty()) {
            return email.substring(0, 1).toUpperCase();
        }
        return first + last;
    }

    // Helper method to increment failed login attempts
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.locked = true;
        }
    }

    // Helper method to reset failed login attempts
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.locked = false;
    }

    // Helper method to record login
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        resetFailedAttempts();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
