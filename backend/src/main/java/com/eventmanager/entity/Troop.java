package com.eventmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Global troop entity - shared across all events
@Entity
@Table(name = "troops")
@NamedQueries({
        @NamedQuery(name = "Troop.findAll", query = "SELECT t FROM Troop t ORDER BY t.sortOrder, t.name"),
        @NamedQuery(name = "Troop.findByName", query = "SELECT t FROM Troop t WHERE t.name = :name")
})
public class Troop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public Troop() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with name
    public Troop(String name) {
        this();
        this.name = name;
    }

    // Getters and setters
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}