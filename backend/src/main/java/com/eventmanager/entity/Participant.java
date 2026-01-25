package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

// JPA Entity representing a participant in an event
@Entity
@Table(name = "participants")
@NamedQueries({
    @NamedQuery(
        name = "Participant.findAll",
        query = "SELECT p FROM Participant p ORDER BY p.lastName, p.firstName ASC"
    ),
    @NamedQuery(
        name = "Participant.findByPatrol",
        query = "SELECT p FROM Participant p WHERE p.patrol.id = :patrolId ORDER BY p.lastName, p.firstName ASC"
    ),
    @NamedQuery(
        name = "Participant.findByEvent",
        query = "SELECT p FROM Participant p JOIN p.registrations r WHERE r.event.id = :eventId ORDER BY p.lastName, p.firstName ASC"
    ),
    @NamedQuery(
        name = "Participant.findByEmail",
        query = "SELECT p FROM Participant p WHERE p.email = :email"
    ),
    @NamedQuery(
        name = "Participant.findWithAllergens",
        query = "SELECT DISTINCT p FROM Participant p LEFT JOIN FETCH p.allergens WHERE p.id = :id"
    )
})
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 13, message = "Personal number cannot exceed 13 characters")
    @Column(name = "personal_number", length = 13)
    private String personalNumber;

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    @Column(name = "street_address", length = 200)
    private String streetAddress;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(length = 100)
    private String city;

    // Guardian information (for minors)
    @Size(max = 100, message = "Guardian name cannot exceed 100 characters")
    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Size(max = 100, message = "Guardian email cannot exceed 100 characters")
    @Column(name = "guardian_email", length = 100)
    private String guardianEmail;

    @Size(max = 20, message = "Guardian phone cannot exceed 20 characters")
    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Many-to-one relationship with Patrol
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_id")
    private Patrol patrol;

    // One-to-many relationship with Registration
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Registration> registrations = new HashSet<>();

    // Many-to-many relationship with Allergen
    @ManyToMany
    @JoinTable(
        name = "participant_allergens",
        joinColumns = @JoinColumn(name = "participant_id"),
        inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private Set<Allergen> allergens = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public Participant() {
    }

    // Constructor with required fields
    public Participant(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Helper method to calculate age
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Helper method to check if participant is a minor (under 18)
    public boolean isMinor() {
        Integer age = getAge();
        return age != null && age < 18;
    }

    // Helper method to check if participant needs guardian info
    public boolean needsGuardianInfo() {
        return isMinor() && (guardianName == null || guardianName.isBlank());
    }

    // Helper method to check if participant has allergens
    public boolean hasAllergens() {
        return allergens != null && !allergens.isEmpty();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Patrol getPatrol() {
        return patrol;
    }

    public void setPatrol(Patrol patrol) {
        this.patrol = patrol;
    }

    public Set<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Set<Registration> registrations) {
        this.registrations = registrations;
    }

    public Set<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<Allergen> allergens) {
        this.allergens = allergens;
    }

    // Helper methods for managing relationships
    public void addAllergen(Allergen allergen) {
        allergens.add(allergen);
        allergen.getParticipants().add(this);
    }

    public void removeAllergen(Allergen allergen) {
        allergens.remove(allergen);
        allergen.getParticipants().remove(this);
    }

    public void addRegistration(Registration registration) {
        registrations.add(registration);
        registration.setParticipant(this);
    }

    public void removeRegistration(Registration registration) {
        registrations.remove(registration);
        registration.setParticipant(null);
    }
}
