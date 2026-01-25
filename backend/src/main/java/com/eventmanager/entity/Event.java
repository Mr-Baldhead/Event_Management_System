package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// JPA Entity representing an event in the system
@Entity
@Table(name = "events")
@NamedQueries({
    @NamedQuery(
        name = "Event.findAll",
        query = "SELECT e FROM Event e ORDER BY e.startDate DESC"
    ),
    @NamedQuery(
        name = "Event.findUpcoming",
        query = "SELECT e FROM Event e WHERE e.startDate > :now ORDER BY e.startDate ASC"
    ),
    @NamedQuery(
        name = "Event.findBySlug",
        query = "SELECT e FROM Event e WHERE e.slug = :slug"
    )
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 200, message = "Slug cannot exceed 200 characters")
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    @Column(name = "street_address", length = 200)
    private String streetAddress;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(length = 100)
    private String city;

    @Min(value = 0, message = "Capacity must be non-negative")
    @Column(nullable = false)
    private Integer capacity = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional relationship with Registration
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    // Bidirectional relationship with Patrol
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Patrol> patrols = new ArrayList<>();

    // Bidirectional relationship with Allergen (event-specific allergens)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allergen> allergens = new ArrayList<>();

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
    public Event() {
    }

    // Constructor with required fields
    public Event(String name, String slug, LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.slug = slug;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Helper method to check if event has available capacity
    public boolean hasAvailableCapacity() {
        if (capacity == null || capacity == 0) {
            return true; // Unlimited capacity
        }
        long confirmedCount = registrations.stream()
            .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
            .count();
        return confirmedCount < capacity;
    }

    // Helper method to get remaining spots
    public int getRemainingSpots() {
        if (capacity == null || capacity == 0) {
            return Integer.MAX_VALUE;
        }
        long confirmedCount = registrations.stream()
            .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
            .count();
        return (int) (capacity - confirmedCount);
    }

    // Getters and Setters
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }

    public List<Patrol> getPatrols() {
        return patrols;
    }

    public void setPatrols(List<Patrol> patrols) {
        this.patrols = patrols;
    }

    public List<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<Allergen> allergens) {
        this.allergens = allergens;
    }

    // Helper methods for managing relationships
    public void addRegistration(Registration registration) {
        registrations.add(registration);
        registration.setEvent(this);
    }

    public void removeRegistration(Registration registration) {
        registrations.remove(registration);
        registration.setEvent(null);
    }

    public void addPatrol(Patrol patrol) {
        patrols.add(patrol);
        patrol.setEvent(this);
    }

    public void removePatrol(Patrol patrol) {
        patrols.remove(patrol);
        patrol.setEvent(null);
    }

    public void addAllergen(Allergen allergen) {
        allergens.add(allergen);
        allergen.setEvent(this);
    }

    public void removeAllergen(Allergen allergen) {
        allergens.remove(allergen);
        allergen.setEvent(null);
    }
}
