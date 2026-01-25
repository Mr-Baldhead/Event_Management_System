package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

// JPA Entity representing an allergen that can be tracked for events
@Entity
@Table(name = "allergens")
@NamedQueries({
    @NamedQuery(
        name = "Allergen.findAll",
        query = "SELECT a FROM Allergen a ORDER BY a.name ASC"
    ),
    @NamedQuery(
        name = "Allergen.findByEvent",
        query = "SELECT a FROM Allergen a WHERE a.event.id = :eventId ORDER BY a.name ASC"
    ),
    @NamedQuery(
        name = "Allergen.findCritical",
        query = "SELECT a FROM Allergen a WHERE a.severity = :severity ORDER BY a.name ASC"
    ),
    @NamedQuery(
        name = "Allergen.findGlobal",
        query = "SELECT a FROM Allergen a WHERE a.event IS NULL ORDER BY a.name ASC"
    )
})
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Allergen name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllergenSeverity severity = AllergenSeverity.MEDIUM;

    // Many-to-one relationship with Event (null = global allergen)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Many-to-many relationship with Participant
    @ManyToMany(mappedBy = "allergens")
    private Set<Participant> participants = new HashSet<>();

    // Default constructor required by JPA
    public Allergen() {
    }

    // Constructor with required fields
    public Allergen(String name) {
        this.name = name;
    }

    // Constructor with name and severity
    public Allergen(String name, AllergenSeverity severity) {
        this.name = name;
        this.severity = severity;
    }

    // Helper method to check if this is a global allergen
    public boolean isGlobal() {
        return event == null;
    }

    // Helper method to check if this allergen is critical
    public boolean isCritical() {
        return severity == AllergenSeverity.CRITICAL || severity == AllergenSeverity.HIGH;
    }

    // Helper method to get affected participant count
    public int getAffectedParticipantCount() {
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

    public AllergenSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergenSeverity severity) {
        this.severity = severity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }
}
