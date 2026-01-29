package com.eventmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Global food allergy entity - shared across all events
@Entity
@Table(name = "food_allergies")
@NamedQueries({
        @NamedQuery(name = "FoodAllergy.findAll", query = "SELECT f FROM FoodAllergy f ORDER BY f.sortOrder, f.name"),
        @NamedQuery(name = "FoodAllergy.findByName", query = "SELECT f FROM FoodAllergy f WHERE f.name = :name")
})
public class FoodAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public FoodAllergy() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with name
    public FoodAllergy(String name) {
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