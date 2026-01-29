package com.eventmanager.dto;

// DTO for login responses
public class LoginResponse {

    private String token;
    private UserDTO user;
    private boolean mustChangePassword;
    private String message;

    // Default constructor
    public LoginResponse() {
    }

    // Constructor for successful login
    public LoginResponse(String token, UserDTO user, boolean mustChangePassword) {
        this.token = token;
        this.user = user;
        this.mustChangePassword = mustChangePassword;
        this.message = "Login successful";
    }

    // Constructor for error response
    public LoginResponse(String message) {
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
