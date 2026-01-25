package com.eventmanager.dto;

import com.eventmanager.entity.Participant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

// Data Transfer Object for Participant entity
public class ParticipantDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    private LocalDate birthDate;

    @Size(max = 13, message = "Personal number cannot exceed 13 characters")
    private String personalNumber;

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String streetAddress;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    // Guardian information
    @Size(max = 100, message = "Guardian name cannot exceed 100 characters")
    private String guardianName;

    @Size(max = 100, message = "Guardian email cannot exceed 100 characters")
    private String guardianEmail;

    @Size(max = 20, message = "Guardian phone cannot exceed 20 characters")
    private String guardianPhone;

    // Related entity IDs
    private Long patrolId;
    private String patrolName;

    // Computed fields
    private String fullName;
    private Integer age;
    private Boolean isMinor;
    private Boolean hasAllergens;

    // Allergen information
    private Set<AllergenDTO> allergens;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ParticipantDTO() {
    }

    // Constructor from entity
    public ParticipantDTO(Participant participant) {
        this.id = participant.getId();
        this.firstName = participant.getFirstName();
        this.lastName = participant.getLastName();
        this.email = participant.getEmail();
        this.phone = participant.getPhone();
        this.birthDate = participant.getBirthDate();
        this.personalNumber = participant.getPersonalNumber();
        this.streetAddress = participant.getStreetAddress();
        this.postalCode = participant.getPostalCode();
        this.city = participant.getCity();
        this.guardianName = participant.getGuardianName();
        this.guardianEmail = participant.getGuardianEmail();
        this.guardianPhone = participant.getGuardianPhone();
        this.createdAt = participant.getCreatedAt();
        this.updatedAt = participant.getUpdatedAt();

        // Computed fields
        this.fullName = participant.getFullName();
        this.age = participant.getAge();
        this.isMinor = participant.isMinor();
        this.hasAllergens = participant.hasAllergens();

        // Related entities
        if (participant.getPatrol() != null) {
            this.patrolId = participant.getPatrol().getId();
            this.patrolName = participant.getPatrol().getName();
        }

        // Allergens
        if (participant.getAllergens() != null && !participant.getAllergens().isEmpty()) {
            this.allergens = participant.getAllergens().stream()
                .map(AllergenDTO::new)
                .collect(Collectors.toSet());
        }
    }

    // Convert DTO to entity
    public Participant toEntity() {
        Participant participant = new Participant();
        participant.setId(this.id);
        participant.setFirstName(this.firstName);
        participant.setLastName(this.lastName);
        participant.setEmail(this.email);
        participant.setPhone(this.phone);
        participant.setBirthDate(this.birthDate);
        participant.setPersonalNumber(this.personalNumber);
        participant.setStreetAddress(this.streetAddress);
        participant.setPostalCode(this.postalCode);
        participant.setCity(this.city);
        participant.setGuardianName(this.guardianName);
        participant.setGuardianEmail(this.guardianEmail);
        participant.setGuardianPhone(this.guardianPhone);
        return participant;
    }

    // Update existing entity from DTO
    public void updateEntity(Participant participant) {
        participant.setFirstName(this.firstName);
        participant.setLastName(this.lastName);
        participant.setEmail(this.email);
        participant.setPhone(this.phone);
        participant.setBirthDate(this.birthDate);
        participant.setPersonalNumber(this.personalNumber);
        participant.setStreetAddress(this.streetAddress);
        participant.setPostalCode(this.postalCode);
        participant.setCity(this.city);
        participant.setGuardianName(this.guardianName);
        participant.setGuardianEmail(this.guardianEmail);
        participant.setGuardianPhone(this.guardianPhone);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public Long getPatrolId() {
        return patrolId;
    }

    public void setPatrolId(Long patrolId) {
        this.patrolId = patrolId;
    }

    public String getPatrolName() {
        return patrolName;
    }

    public void setPatrolName(String patrolName) {
        this.patrolName = patrolName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getIsMinor() {
        return isMinor;
    }

    public void setIsMinor(Boolean isMinor) {
        this.isMinor = isMinor;
    }

    public Boolean getHasAllergens() {
        return hasAllergens;
    }

    public void setHasAllergens(Boolean hasAllergens) {
        this.hasAllergens = hasAllergens;
    }

    public Set<AllergenDTO> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<AllergenDTO> allergens) {
        this.allergens = allergens;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
