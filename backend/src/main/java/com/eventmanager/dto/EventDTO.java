package com.eventmanager.dto;

import com.eventmanager.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import org.hibernate.Hibernate;
import java.time.LocalDateTime;

// Data Transfer Object for Event entity - prevents JPA entity serialization issues
public class EventDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 200, message = "Slug cannot exceed 200 characters")
    private String slug;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String streetAddress;

    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Min(value = 0, message = "Capacity must be non-negative")
    private Integer capacity;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Computed fields
    private Integer registrationCount;
    private Integer remainingSpots;

    // Default constructor
    public EventDTO() {
    }

    // Constructor from entity - safely handles lazy collections
    public EventDTO(Event event) {
        this.id = event.getId();
        this.name = event.getName();
        this.slug = event.getSlug();
        this.description = event.getDescription();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.streetAddress = event.getStreetAddress();
        this.postalCode = event.getPostalCode();
        this.city = event.getCity();
        this.capacity = event.getCapacity();
        this.active = event.getActive();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
        
        // Safely handle lazy-loaded registrations collection
        if (event.getRegistrations() != null && Hibernate.isInitialized(event.getRegistrations())) {
            this.registrationCount = event.getRegistrations().size();
            this.remainingSpots = event.getCapacity() != null 
                ? event.getCapacity() - this.registrationCount 
                : null;
        } else {
            this.registrationCount = 0;
            this.remainingSpots = event.getCapacity();
        }
    }

    // Convert DTO to entity
    public Event toEntity() {
        Event event = new Event();
        event.setId(this.id);
        event.setName(this.name);
        event.setSlug(this.slug);
        event.setDescription(this.description);
        event.setStartDate(this.startDate);
        event.setEndDate(this.endDate);
        event.setStreetAddress(this.streetAddress);
        event.setPostalCode(this.postalCode);
        event.setCity(this.city);
        event.setCapacity(this.capacity);
        event.setActive(this.active != null ? this.active : true);
        return event;
    }

    // Update existing entity from DTO
    public void updateEntity(Event event) {
        event.setName(this.name);
        event.setSlug(this.slug);
        event.setDescription(this.description);
        event.setStartDate(this.startDate);
        event.setEndDate(this.endDate);
        event.setStreetAddress(this.streetAddress);
        event.setPostalCode(this.postalCode);
        event.setCity(this.city);
        event.setCapacity(this.capacity);
        if (this.active != null) {
            event.setActive(this.active);
        }
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRegistrationCount() {
        return registrationCount;
    }

    public void setRegistrationCount(Integer registrationCount) {
        this.registrationCount = registrationCount;
    }

    public Integer getRemainingSpots() {
        return remainingSpots;
    }

    public void setRemainingSpots(Integer remainingSpots) {
        this.remainingSpots = remainingSpots;
    }
}
