package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// JPA Entity representing a registration linking a participant to an event
@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "participant_id"})
})
@NamedQueries({
    @NamedQuery(
        name = "Registration.findAll",
        query = "SELECT r FROM Registration r ORDER BY r.registrationDate DESC"
    ),
    @NamedQuery(
        name = "Registration.findByEvent",
        query = "SELECT r FROM Registration r WHERE r.event.id = :eventId ORDER BY r.registrationDate DESC"
    ),
    @NamedQuery(
        name = "Registration.findByParticipant",
        query = "SELECT r FROM Registration r WHERE r.participant.id = :participantId ORDER BY r.registrationDate DESC"
    ),
    @NamedQuery(
        name = "Registration.findByStatus",
        query = "SELECT r FROM Registration r WHERE r.status = :status ORDER BY r.registrationDate DESC"
    ),
    @NamedQuery(
        name = "Registration.findByEventAndStatus",
        query = "SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = :status ORDER BY r.registrationDate DESC"
    ),
    @NamedQuery(
        name = "Registration.countConfirmedByEvent",
        query = "SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'"
    ),
    @NamedQuery(
        name = "Registration.findByEventAndParticipant",
        query = "SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.participant.id = :participantId"
    )
})
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Event is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull(message = "Participant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "confirmation_date")
    private LocalDateTime confirmationDate;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
    }

    // Default constructor required by JPA
    public Registration() {
    }

    // Constructor with event and participant
    public Registration(Event event, Participant participant) {
        this.event = event;
        this.participant = participant;
    }

    // Helper method to confirm registration
    public void confirm() {
        if (this.status == RegistrationStatus.PENDING || this.status == RegistrationStatus.WAITLIST) {
            this.status = RegistrationStatus.CONFIRMED;
            this.confirmationDate = LocalDateTime.now();
        }
    }

    // Helper method to cancel registration
    public void cancel() {
        if (this.status != RegistrationStatus.CANCELLED) {
            this.status = RegistrationStatus.CANCELLED;
            this.cancellationDate = LocalDateTime.now();
        }
    }

    // Helper method to put on waitlist
    public void putOnWaitlist() {
        if (this.status == RegistrationStatus.PENDING) {
            this.status = RegistrationStatus.WAITLIST;
        }
    }

    // Helper method to check if registration is active
    public boolean isActive() {
        return status == RegistrationStatus.CONFIRMED || status == RegistrationStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public LocalDateTime getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(LocalDateTime confirmationDate) {
        this.confirmationDate = confirmationDate;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
