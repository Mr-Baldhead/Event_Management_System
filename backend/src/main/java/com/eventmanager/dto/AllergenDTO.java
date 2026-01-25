package com.eventmanager.dto;

import com.eventmanager.entity.Allergen;
import com.eventmanager.entity.AllergenSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data Transfer Object for Allergen entity
public class AllergenDTO {

    private Long id;

    @NotBlank(message = "Allergen name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private AllergenSeverity severity;

    private Long eventId;

    // Computed fields
    private Boolean isGlobal;
    private Boolean isCritical;
    private Integer affectedParticipantCount;

    // Default constructor
    public AllergenDTO() {
    }

    // Constructor from entity
    public AllergenDTO(Allergen allergen) {
        this.id = allergen.getId();
        this.name = allergen.getName();
        this.description = allergen.getDescription();
        this.severity = allergen.getSeverity();
        this.isGlobal = allergen.isGlobal();
        this.isCritical = allergen.isCritical();
        this.affectedParticipantCount = allergen.getAffectedParticipantCount();

        if (allergen.getEvent() != null) {
            this.eventId = allergen.getEvent().getId();
        }
    }

    // Convert DTO to entity
    public Allergen toEntity() {
        Allergen allergen = new Allergen();
        allergen.setId(this.id);
        allergen.setName(this.name);
        allergen.setDescription(this.description);
        allergen.setSeverity(this.severity != null ? this.severity : AllergenSeverity.MEDIUM);
        return allergen;
    }

    // Update existing entity from DTO
    public void updateEntity(Allergen allergen) {
        allergen.setName(this.name);
        allergen.setDescription(this.description);
        if (this.severity != null) {
            allergen.setSeverity(this.severity);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AllergenSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergenSeverity severity) {
        this.severity = severity;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Boolean getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }

    public Integer getAffectedParticipantCount() {
        return affectedParticipantCount;
    }

    public void setAffectedParticipantCount(Integer affectedParticipantCount) {
        this.affectedParticipantCount = affectedParticipantCount;
    }
}
