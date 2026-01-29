package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// JPA Entity representing an option for SELECT, CHECKBOX, or RADIO fields
@Entity
@Table(name = "field_options")
public class FieldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private FormField formField;

    @NotBlank(message = "Value is required")
    @Size(max = 200, message = "Value cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String value;

    @NotBlank(message = "Label is required")
    @Size(max = 200, message = "Label cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // Default constructor
    public FieldOption() {
    }

    // Constructor with required fields
    public FieldOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FormField getFormField() {
        return formField;
    }

    public void setFormField(FormField formField) {
        this.formField = formField;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}