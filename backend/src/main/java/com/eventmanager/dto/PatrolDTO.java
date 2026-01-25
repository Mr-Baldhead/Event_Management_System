package com.eventmanager.dto;

import com.eventmanager.entity.Patrol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data Transfer Object for Patrol entity (Scout troop / kår)
public class PatrolDTO {

    private Long id;

    @NotBlank(message = "Patrol name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    private String contactPerson;

    @Size(max = 100, message = "Contact email cannot exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    private Long eventId;
    private String eventName;

    // Computed fields
    private Integer participantCount;

    // Default constructor
    public PatrolDTO() {
    }

    // Constructor from entity
    public PatrolDTO(Patrol patrol) {
        this.id = patrol.getId();
        this.name = patrol.getName();
        this.description = patrol.getDescription();
        this.contactPerson = patrol.getContactPerson();
        this.contactEmail = patrol.getContactEmail();
        this.contactPhone = patrol.getContactPhone();
        this.participantCount = patrol.getParticipantCount();

        if (patrol.getEvent() != null) {
            this.eventId = patrol.getEvent().getId();
            this.eventName = patrol.getEvent().getName();
        }
    }

    // Convert DTO to entity
    public Patrol toEntity() {
        Patrol patrol = new Patrol();
        patrol.setId(this.id);
        patrol.setName(this.name);
        patrol.setDescription(this.description);
        patrol.setContactPerson(this.contactPerson);
        patrol.setContactEmail(this.contactEmail);
        patrol.setContactPhone(this.contactPhone);
        return patrol;
    }

    // Update existing entity from DTO
    public void updateEntity(Patrol patrol) {
        patrol.setName(this.name);
        patrol.setDescription(this.description);
        patrol.setContactPerson(this.contactPerson);
        patrol.setContactEmail(this.contactEmail);
        patrol.setContactPhone(this.contactPhone);
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
}
