package com.eventmanager.service;

import com.eventmanager.dto.*;
import com.eventmanager.entity.Session;
import com.eventmanager.entity.User;
import com.eventmanager.entity.UserRole;
import com.eventmanager.exception.AuthenticationException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.SessionRepository;
import com.eventmanager.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

// Service for authentication and session management
@ApplicationScoped
public class AuthService {

    private static final int SESSION_TIMEOUT_MINUTES = 30;

    @Inject
    private UserRepository userRepository;

    @Inject
    private SessionRepository sessionRepository;

    @Inject
    private PasswordService passwordService;

    // Authenticate user and create session
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Felaktig e-post eller lösenord"));

        // Check if account is locked
        if (user.getLocked()) {
            throw new AuthenticationException("Kontot är låst. Kontakta administratör.");
        }

        // Verify password
        if (!passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.update(user);

            if (user.getLocked()) {
                throw new AuthenticationException("Kontot har låsts efter för många misslyckade försök.");
            }
            throw new AuthenticationException("Felaktig e-post eller lösenord");
        }

        // Create session
        String token = passwordService.generateSessionToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES);

        Session session = new Session(user, token, expiresAt);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        sessionRepository.save(session);

        // Update user login info
        user.recordLogin();
        userRepository.update(user);

        // Build response
        UserDTO userDTO = toDTO(user);
        return new LoginResponse(token, userDTO, user.getMustChangePassword());
    }

    // Validate session and return user ID (not the entity to avoid lazy loading issues)
    @Transactional
    public Optional<Long> validateSessionAndGetUserId(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = sessionRepository.findValidByToken(token);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        Session session = sessionOpt.get();
        if (!session.isValid()) {
            return Optional.empty();
        }

        // Update session activity (extend timeout)
        session.updateActivity(SESSION_TIMEOUT_MINUTES);
        sessionRepository.update(session);

        return Optional.of(session.getUser().getId());
    }

    // Validate session and return user (for internal use within transaction)
    @Transactional
    public Optional<User> validateSession(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> sessionOpt = sessionRepository.findValidByToken(token);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        Session session = sessionOpt.get();
        if (!session.isValid()) {
            return Optional.empty();
        }

        // Update session activity (extend timeout)
        session.updateActivity(SESSION_TIMEOUT_MINUTES);
        sessionRepository.update(session);

        // Re-fetch user to ensure it's fully loaded
        return userRepository.findById(session.getUser().getId());
    }

    // Logout - delete session
    @Transactional
    public void logout(String token) {
        sessionRepository.deleteByToken(token);
    }

    // Logout all sessions for a user
    @Transactional
    public void logoutAll(Long userId) {
        sessionRepository.deleteByUserId(userId);
    }

    // Change password (keeps current session active)
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String currentToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If not first login, verify current password
        if (!user.getMustChangePassword()) {
            if (request.getCurrentPassword() == null ||
                    !passwordService.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Nuvarande lösenord är felaktigt");
            }
        }

        // Update password
        user.setPasswordHash(passwordService.hashPassword(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.update(user);

        // Note: We don't invalidate sessions here to keep the user logged in
        // If you want to invalidate other sessions but keep current one:
        // sessionRepository.deleteByUserIdExceptToken(userId, currentToken);
    }

    // Change password (legacy method without token - invalidates all sessions)
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        changePassword(userId, request, null);
    }

    // Get current user from token
    @Transactional
    public UserDTO getCurrentUser(String token) {
        Optional<Long> userIdOpt = validateSessionAndGetUserId(token);
        if (userIdOpt.isEmpty()) {
            throw new AuthenticationException("Ogiltig session");
        }

        User user = userRepository.findById(userIdOpt.get())
                .orElseThrow(() -> new AuthenticationException("Ogiltig session"));
        return toDTO(user);
    }

    // Check if user has required role
    @Transactional
    public boolean hasRole(String token, UserRole requiredRole) {
        Optional<User> userOpt = validateSession(token);
        if (userOpt.isEmpty()) {
            return false;
        }
        return userOpt.get().getRole() == requiredRole;
    }

    // Clean up expired sessions (should be called periodically)
    @Transactional
    public int cleanupExpiredSessions() {
        return sessionRepository.deleteExpired();
    }

    // Convert User entity to DTO
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setMustChangePassword(user.getMustChangePassword());
        dto.setLocked(user.getLocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setInitials(user.getInitials());
        dto.setFullName(user.getFullName());
        return dto;
    }
}