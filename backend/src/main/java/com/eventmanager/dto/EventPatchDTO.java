package com.eventmanager.dto;

/**
 * DTO for partial event updates (PATCH operations)
 * All fields are optional - only non-null fields will be updated
 */
public class EventPatchDTO {

    private Boolean active;
    private String name;
    private String description;

    // Default constructor
    public EventPatchDTO() {
    }

    // Getters and setters
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}