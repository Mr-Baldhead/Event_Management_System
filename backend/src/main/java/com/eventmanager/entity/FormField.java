package com.eventmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// JPA Entity representing a form field in an event's registration form
@Entity
@Table(name = "form_fields")
@NamedQueries({
        @NamedQuery(
                name = "FormField.findByEventId",
                query = "SELECT f FROM FormField f WHERE f.event.id = :eventId ORDER BY f.sortOrder ASC"
        ),
        @NamedQuery(
                name = "FormField.findByEventIdAndType",
                query = "SELECT f FROM FormField f WHERE f.event.id = :eventId AND f.fieldType = :fieldType ORDER BY f.sortOrder ASC"
        ),
        @NamedQuery(
                name = "FormField.getMaxSortOrder",
                query = "SELECT COALESCE(MAX(f.sortOrder), 0) FROM FormField f WHERE f.event.id = :eventId"
        )
})
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Event is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String label;

    @NotNull(message = "Field type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(nullable = false)
    private Boolean visible = true;

    @Size(max = 500, message = "Validation pattern cannot exceed 500 characters")
    @Column(name = "validation_pattern", length = 500)
    private String validationPattern;

    @Size(max = 200, message = "Placeholder cannot exceed 200 characters")
    @Column(length = 200)
    private String placeholder;

    @Column(name = "max_length")
    private Integer maxLength;

    // For predefined fields like "firstName", "email", etc.
    @Column(name = "predefined_type", length = 50)
    private String predefinedType;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    // For conditional fields - which field triggers this one
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_field_id")
    private FormField parentField;

    // The value that triggers this field to show
    @Size(max = 100, message = "Trigger value cannot exceed 100 characters")
    @Column(name = "trigger_value", length = 100)
    private String triggerValue;

    // Row number for layout (fields with same row are displayed side by side)
    @Column(name = "row_index", nullable = false)
    private Integer rowIndex = 0;

    // Column position within row (0 = left, 1 = right, etc.)
    @Column(name = "col_position", nullable = false)
    private Integer colPosition = 0;

    // Column width (1-12 grid system, 12 = full width)
    @Column(name = "col_width", nullable = false)
    private Integer colWidth = 12;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Options for SELECT, CHECKBOX, RADIO fields
    @OneToMany(mappedBy = "formField", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<FieldOption> options = new ArrayList<>();

    // Child fields (for conditional display)
    @OneToMany(mappedBy = "parentField", cascade = CascadeType.ALL)
    private List<FormField> childFields = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Default constructor
    public FormField() {
    }

    // Constructor with required fields
    public FormField(Event event, String label, FieldType fieldType) {
        this.event = event;
        this.label = label;
        this.fieldType = fieldType;
    }

    // Helper method to add option
    public void addOption(FieldOption option) {
        options.add(option);
        option.setFormField(this);
    }

    // Helper method to remove option
    public void removeOption(FieldOption option) {
        options.remove(option);
        option.setFormField(null);
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getValidationPattern() {
        return validationPattern;
    }

    public void setValidationPattern(String validationPattern) {
        this.validationPattern = validationPattern;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getPredefinedType() {
        return predefinedType;
    }

    public void setPredefinedType(String predefinedType) {
        this.predefinedType = predefinedType;
    }

    public Boolean getIsPredefined() {
        return isPredefined;
    }

    public void setIsPredefined(Boolean isPredefined) {
        this.isPredefined = isPredefined;
    }

    public FormField getParentField() {
        return parentField;
    }

    public void setParentField(FormField parentField) {
        this.parentField = parentField;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getRowIndex() {return rowIndex;}

    public void setRowIndex(Integer rowIndex) {this.rowIndex = rowIndex;}

    public Integer getColPosition() {
        return colPosition;
    }

    public void setColPosition(Integer colPosition) {
        this.colPosition = colPosition;
    }

    public Integer getColWidth() {
        return colWidth;
    }

    public void setColWidth(Integer colWidth) {
        this.colWidth = colWidth;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<FieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOption> options) {
        this.options = options;
    }

    public List<FormField> getChildFields() {
        return childFields;
    }

    public void setChildFields(List<FormField> childFields) {
        this.childFields = childFields;
    }
}