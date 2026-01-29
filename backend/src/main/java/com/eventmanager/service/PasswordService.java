package com.eventmanager.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

// Service for password hashing and verification using BCrypt
@ApplicationScoped
public class PasswordService {

    private static final Logger LOGGER = Logger.getLogger(PasswordService.class.getName());
    private static final int BCRYPT_ROUNDS = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Hash a password using BCrypt
    public String hashPassword(String plainPassword) {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        LOGGER.info("Generated hash for password, hash length: " + hash.length());
        return hash;
    }

    // Verify a password against a hash
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        LOGGER.info("Verifying password...");
        LOGGER.info("Plain password length: " + (plainPassword != null ? plainPassword.length() : "null"));
        LOGGER.info("Hashed password: " + hashedPassword);

        if (plainPassword == null || hashedPassword == null) {
            LOGGER.warning("Password or hash is null!");
            return false;
        }

        try {
            boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
            LOGGER.info("BCrypt verification result: " + result);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "BCrypt verification failed with exception", e);
            return false;
        }
    }

    // Generate a secure random token for sessions
    public String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    // Generate a random password
    public String generateRandomPassword(int length) {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=";
        String allChars = upperCase + lowerCase + digits + special;

        StringBuilder password = new StringBuilder(length);

        // Ensure at least one of each type
        password.append(upperCase.charAt(SECURE_RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(SECURE_RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        password.append(special.charAt(SECURE_RANDOM.nextInt(special.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(SECURE_RANDOM.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    // Calculate password strength (returns 0-100)
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int strength = 0;

        // Length scoring
        if (password.length() >= 8) strength += 20;
        if (password.length() >= 12) strength += 20;
        if (password.length() >= 16) strength += 10;

        // Character type scoring
        if (password.matches(".*[a-z].*")) strength += 10;
        if (password.matches(".*[A-Z].*")) strength += 15;
        if (password.matches(".*[0-9].*")) strength += 15;
        if (password.matches(".*[^a-zA-Z0-9].*")) strength += 20;

        return Math.min(strength, 100);
    }

    // Get password strength level
    public String getPasswordStrengthLevel(String password) {
        int strength = calculatePasswordStrength(password);
        if (strength < 40) return "svagt";
        if (strength < 70) return "mellan";
        return "starkt";
    }
}