package com.eventmanager.dto;

import com.eventmanager.entity.FieldType;
import java.util.ArrayList;
import java.util.List;

// DTO for transferring form field data
public class FormFieldDTO {

    private Long id;
    private Long eventId;
    private String label;
    private FieldType fieldType;
    private Integer sortOrder;
    private Boolean required;
    private Boolean visible;
    private String validationPattern;
    private String placeholder;
    private Integer maxLength;
    private String predefinedType;
    private Boolean isPredefined;
    private Long parentFieldId;
    private String triggerValue;
    private Integer rowIndex;
    private Integer colPosition;
    private Integer colWidth;
    private List<FieldOptionDTO> options = new ArrayList<>();

    // Default constructor
    public FormFieldDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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

    public Long getParentFieldId() {
        return parentFieldId;
    }

    public void setParentFieldId(Long parentFieldId) {
        this.parentFieldId = parentFieldId;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getRowIndex() { return rowIndex; }

    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex;}

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

    public List<FieldOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<FieldOptionDTO> options) {
        this.options = options;
    }

    // Nested DTO for field options
    public static class FieldOptionDTO {
        private Long id;
        private String value;
        private String label;
        private Integer sortOrder;

        public FieldOptionDTO() {
        }

        public FieldOptionDTO(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
}