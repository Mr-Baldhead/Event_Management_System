package com.eventmanager.service;

import com.eventmanager.dto.FormFieldDTO;
import com.eventmanager.dto.FormFieldDTO.FieldOptionDTO;
import com.eventmanager.entity.Event;
import com.eventmanager.entity.FieldOption;
import com.eventmanager.entity.FieldType;
import com.eventmanager.entity.FormField;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.FormFieldRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Service for form field business logic
@ApplicationScoped
public class FormFieldService {

    @Inject
    private FormFieldRepository formFieldRepository;

    @Inject
    private EventRepository eventRepository;

    // Get all fields for an event
    public List<FormFieldDTO> getFieldsByEventId(Long eventId) {
        List<FormField> fields = formFieldRepository.findByEventId(eventId);
        return fields.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get a single field by ID
    public FormFieldDTO getFieldById(Long id) {
        FormField field = formFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form field not found with id: " + id));
        return toDTO(field);
    }

    // Create a new field
    @Transactional
    public FormFieldDTO createField(Long eventId, FormFieldDTO dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        FormField field = new FormField();
        field.setEvent(event);
        updateFieldFromDTO(field, dto);

        // Set sort order if not provided
        if (dto.getSortOrder() == null) {
            int maxSortOrder = formFieldRepository.getMaxSortOrder(eventId);
            field.setSortOrder(maxSortOrder + 1);
        }

        // Set row index if not provided
        if (dto.getRowIndex() == null) {
            int maxRowIndex = formFieldRepository.getMaxRowIndex(eventId);
            field.setRowIndex(maxRowIndex + 1);
        }

        FormField saved = formFieldRepository.save(field);
        return toDTO(saved);
    }

    // Update an existing field
    @Transactional
    public FormFieldDTO updateField(Long id, FormFieldDTO dto) {
        FormField field = formFieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form field not found with id: " + id));

        updateFieldFromDTO(field, dto);

        FormField updated = formFieldRepository.update(field);
        return toDTO(updated);
    }

    // Delete a field
    @Transactional
    public void deleteField(Long id) {
        if (!formFieldRepository.existsById(id)) {
            throw new ResourceNotFoundException("Form field not found with id: " + id);
        }
        formFieldRepository.deleteById(id);
    }

    // Bulk save fields (replace all fields for an event)
    @Transactional
    public List<FormFieldDTO> saveAllFields(Long eventId, List<FormFieldDTO> dtos) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Delete existing fields
        formFieldRepository.deleteByEventId(eventId);

        // Create new fields
        List<FormFieldDTO> savedFields = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            FormFieldDTO dto = dtos.get(i);
            FormField field = new FormField();
            field.setEvent(event);
            updateFieldFromDTO(field, dto);
            field.setSortOrder(i);

            FormField saved = formFieldRepository.save(field);
            savedFields.add(toDTO(saved));
        }

        return savedFields;
    }

    // Reorder fields
    @Transactional
    public void reorderFields(Long eventId, List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            Long fieldId = fieldIds.get(i);
            FormField field = formFieldRepository.findById(fieldId)
                    .orElseThrow(() -> new ResourceNotFoundException("Form field not found with id: " + fieldId));

            if (!field.getEvent().getId().equals(eventId)) {
                throw new IllegalArgumentException("Field does not belong to event: " + eventId);
            }

            field.setSortOrder(i);
            formFieldRepository.update(field);
        }
    }

    // Helper: Update field entity from DTO
    private void updateFieldFromDTO(FormField field, FormFieldDTO dto) {
        field.setLabel(dto.getLabel());
        field.setFieldType(dto.getFieldType());
        field.setRequired(dto.getRequired() != null ? dto.getRequired() : false);
        field.setVisible(dto.getVisible() != null ? dto.getVisible() : true);
        field.setValidationPattern(dto.getValidationPattern());
        field.setPlaceholder(dto.getPlaceholder());
        field.setMaxLength(dto.getMaxLength());
        field.setPredefinedType(dto.getPredefinedType());
        field.setIsPredefined(dto.getIsPredefined() != null ? dto.getIsPredefined() : false);
        field.setTriggerValue(dto.getTriggerValue());
        field.setRowIndex(dto.getRowIndex() != null ? dto.getRowIndex() : 0);
        field.setColPosition(dto.getColPosition() != null ? dto.getColPosition() : 0);
        field.setColWidth(dto.getColWidth() != null ? dto.getColWidth() : 12);

        if (dto.getSortOrder() != null) {
            field.setSortOrder(dto.getSortOrder());
        }

        // Handle options
        if (dto.getOptions() != null) {
            // Clear existing options
            field.getOptions().clear();

            // Add new options
            for (int i = 0; i < dto.getOptions().size(); i++) {
                FieldOptionDTO optionDTO = dto.getOptions().get(i);
                FieldOption option = new FieldOption();
                option.setValue(optionDTO.getValue());
                option.setLabel(optionDTO.getLabel());
                option.setSortOrder(i);
                field.addOption(option);
            }
        }
    }

    // Helper: Convert entity to DTO
    private FormFieldDTO toDTO(FormField field) {
        FormFieldDTO dto = new FormFieldDTO();
        dto.setId(field.getId());
        dto.setEventId(field.getEvent().getId());
        dto.setLabel(field.getLabel());
        dto.setFieldType(field.getFieldType());
        dto.setSortOrder(field.getSortOrder());
        dto.setRequired(field.getRequired());
        dto.setVisible(field.getVisible());
        dto.setValidationPattern(field.getValidationPattern());
        dto.setPlaceholder(field.getPlaceholder());
        dto.setMaxLength(field.getMaxLength());
        dto.setPredefinedType(field.getPredefinedType());
        dto.setIsPredefined(field.getIsPredefined());
        dto.setTriggerValue(field.getTriggerValue());
        dto.setRowIndex(field.getRowIndex());
        dto.setColPosition(field.getColPosition());
        dto.setColWidth(field.getColWidth());

        if (field.getParentField() != null) {
            dto.setParentFieldId(field.getParentField().getId());
        }

        // Map options
        List<FieldOptionDTO> optionDTOs = field.getOptions().stream()
                .map(opt -> {
                    FieldOptionDTO optDTO = new FieldOptionDTO();
                    optDTO.setId(opt.getId());
                    optDTO.setValue(opt.getValue());
                    optDTO.setLabel(opt.getLabel());
                    optDTO.setSortOrder(opt.getSortOrder());
                    return optDTO;
                })
                .collect(Collectors.toList());
        dto.setOptions(optionDTOs);

        return dto;
    }
}