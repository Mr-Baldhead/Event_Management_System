package com.eventmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for registration with participant details and allergies
 */
public class RegistrationDTO {

    private Long id;
    private Long eventId;
    private String status;
    private LocalDateTime registrationDate;
    private LocalDateTime confirmationDate;
    private String notes;
    
    // Participant info
    private Long participantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String personalNumber;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String patrolName;
    
    // Guardian info (for minors)
    private String guardianName;
    private String guardianEmail;
    private String guardianPhone;
    
    // Allergies
    private List<AllergyInfo> allergies;

    // Default constructor
    public RegistrationDTO() {
    }

    // Inner class for allergy info
    public static class AllergyInfo {
        private Long id;
        private String name;
        private String severity;

        public AllergyInfo() {
        }

        public AllergyInfo(Long id, String name, String severity) {
            this.id = id;
            this.name = name;
            this.severity = severity;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(LocalDateTime confirmationDate) {
        this.confirmationDate = confirmationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
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

    public String getPatrolName() {
        return patrolName;
    }

    public void setPatrolName(String patrolName) {
        this.patrolName = patrolName;
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

    public List<AllergyInfo> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<AllergyInfo> allergies) {
        this.allergies = allergies;
    }

    // Computed properties
    public String getFullName() {
        return lastName + " " + firstName;
    }

    public boolean hasAllergies() {
        return allergies != null && !allergies.isEmpty();
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null && !streetAddress.isEmpty()) {
            sb.append(streetAddress);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(postalCode);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(city);
        }
        return sb.toString();
    }
}
