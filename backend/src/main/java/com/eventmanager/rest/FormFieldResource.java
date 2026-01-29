package com.eventmanager.rest;

import com.eventmanager.dto.FormFieldDTO;
import com.eventmanager.service.FormFieldService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

// REST resource for form field operations
@Path("/events/{eventId}/form/fields")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FormFieldResource {

    @Inject
    private FormFieldService formFieldService;

    // Get all fields for an event
    @GET
    public Response getFields(@PathParam("eventId") Long eventId) {
        List<FormFieldDTO> fields = formFieldService.getFieldsByEventId(eventId);
        return Response.ok(fields).build();
    }

    // Get a single field
    @GET
    @Path("/{fieldId}")
    public Response getField(
            @PathParam("eventId") Long eventId,
            @PathParam("fieldId") Long fieldId) {
        FormFieldDTO field = formFieldService.getFieldById(fieldId);
        return Response.ok(field).build();
    }

    // Create a new field
    @POST
    public Response createField(
            @PathParam("eventId") Long eventId,
            @Valid FormFieldDTO dto) {
        FormFieldDTO created = formFieldService.createField(eventId, dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // Update a field
    @PUT
    @Path("/{fieldId}")
    public Response updateField(
            @PathParam("eventId") Long eventId,
            @PathParam("fieldId") Long fieldId,
            @Valid FormFieldDTO dto) {
        FormFieldDTO updated = formFieldService.updateField(fieldId, dto);
        return Response.ok(updated).build();
    }

    // Delete a field
    @DELETE
    @Path("/{fieldId}")
    public Response deleteField(
            @PathParam("eventId") Long eventId,
            @PathParam("fieldId") Long fieldId) {
        formFieldService.deleteField(fieldId);
        return Response.noContent().build();
    }

    // Bulk save all fields (replace all)
    @PUT
    public Response saveAllFields(
            @PathParam("eventId") Long eventId,
            List<FormFieldDTO> fields) {
        List<FormFieldDTO> saved = formFieldService.saveAllFields(eventId, fields);
        return Response.ok(saved).build();
    }

    // Reorder fields
    @POST
    @Path("/reorder")
    public Response reorderFields(
            @PathParam("eventId") Long eventId,
            List<Long> fieldIds) {
        formFieldService.reorderFields(eventId, fieldIds);
        return Response.ok().build();
    }
}