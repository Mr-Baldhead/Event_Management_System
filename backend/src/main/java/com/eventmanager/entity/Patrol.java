package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

// JPA Entity representing a patrol/scout troop (kår)
@Entity
@Table(name = "patrols")
@NamedQueries({
    @NamedQuery(
        name = "Patrol.findAll",
        query = "SELECT p FROM Patrol p ORDER BY p.name ASC"
    ),
    @NamedQuery(
        name = "Patrol.findByEvent",
        query = "SELECT p FROM Patrol p WHERE p.event.id = :eventId ORDER BY p.name ASC"
    ),
    @NamedQuery(
        name = "Patrol.findByName",
        query = "SELECT p FROM Patrol p WHERE p.name = :name AND p.event.id = :eventId"
    )
})
public class Patrol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patrol name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Size(max = 100, message = "Contact email cannot exceed 100 characters")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // Many-to-one relationship with Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Bidirectional relationship with Participant
    @OneToMany(mappedBy = "patrol", cascade = CascadeType.ALL)
    private List<Participant> participants = new ArrayList<>();

    // Default constructor required by JPA
    public Patrol() {
    }

    // Constructor with required fields
    public Patrol(String name) {
        this.name = name;
    }

    // Constructor with name and event
    public Patrol(String name, Event event) {
        this.name = name;
        this.event = event;
    }

    // Helper method to get participant count
    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    // Helper methods for managing relationships
    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setPatrol(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setPatrol(null);
    }
}
