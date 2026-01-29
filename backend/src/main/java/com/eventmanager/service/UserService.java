package com.eventmanager.service;

import com.eventmanager.dto.CreateUserRequest;
import com.eventmanager.dto.UserDTO;
import com.eventmanager.entity.User;
import com.eventmanager.entity.UserRole;
import com.eventmanager.exception.DuplicateResourceException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

// Service for user management (CRUD operations)
@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordService passwordService;

    @Inject
    private EmailService emailService;

    @Inject
    private AuthService authService;

    // Get all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::toDTO)
                .collect(Collectors.toList());
    }

    // Get all admins (excluding superadmin)
    public List<UserDTO> getAllAdmins() {
        return userRepository.findByRole(UserRole.ADMIN).stream()
                .map(authService::toDTO)
                .collect(Collectors.toList());
    }

    // Get user by ID
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return authService.toDTO(user);
    }

    // Get user counts
    public UserCounts getUserCounts() {
        long total = userRepository.findAll().size();
        long admins = userRepository.countByRole(UserRole.ADMIN);
        long superadmins = userRepository.countByRole(UserRole.SUPERADMIN);
        return new UserCounts(total, admins, superadmins);
    }

    // Create new admin user
    @Transactional
    public UserDTO createAdmin(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(UserRole.ADMIN);
        user.setMustChangePassword(true);

        User saved = userRepository.save(user);

        // Send notification email if requested
        if (request.isSendNotification()) {
            try {
                emailService.sendWelcomeEmail(
                    saved.getEmail(),
                    saved.getFirstName(),
                    request.getPassword()
                );
            } catch (Exception e) {
                // Log error but don't fail the user creation
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }
        }

        return authService.toDTO(saved);
    }

    // Update user
    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if email is being changed to one that already exists
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        User updated = userRepository.update(user);
        return authService.toDTO(updated);
    }

    // Delete user
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent deleting superadmin
        if (user.getRole() == UserRole.SUPERADMIN) {
            throw new IllegalStateException("Cannot delete superadmin user");
        }

        userRepository.deleteById(id);
    }

    // Lock/unlock user
    @Transactional
    public UserDTO setUserLocked(Long id, boolean locked) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setLocked(locked);
        if (!locked) {
            user.resetFailedAttempts();
        }

        User updated = userRepository.update(user);
        return authService.toDTO(updated);
    }

    // Reset user password
    @Transactional
    public String resetUserPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String newPassword = passwordService.generateRandomPassword(16);
        user.setPasswordHash(passwordService.hashPassword(newPassword));
        user.setMustChangePassword(true);

        userRepository.update(user);

        return newPassword;
    }

    // Inner class for user counts
    public static class UserCounts {
        public final long total;
        public final long admins;
        public final long superadmins;

        public UserCounts(long total, long admins, long superadmins) {
            this.total = total;
            this.admins = admins;
            this.superadmins = superadmins;
        }
    }
}
