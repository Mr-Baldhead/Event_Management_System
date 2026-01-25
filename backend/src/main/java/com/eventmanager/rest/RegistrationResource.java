package com.eventmanager.rest;

import com.eventmanager.dto.RegistrationDTO;
import com.eventmanager.service.RegistrationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST resource for Registration endpoints
 */
@Path("/events/{eventId}/registrations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationResource {

    @Inject
    private RegistrationService registrationService;

    // GET /api/events/{eventId}/registrations - Get all registrations for an event
    @GET
    public List<RegistrationDTO> getRegistrations(@PathParam("eventId") Long eventId) {
        return registrationService.getRegistrationsForEvent(eventId);
    }

    // GET /api/events/{eventId}/registrations/count - Get registration count
    @GET
    @Path("/count")
    public String getRegistrationCount(@PathParam("eventId") Long eventId) {
        int total = registrationService.countRegistrationsForEvent(eventId);
        int confirmed = registrationService.countConfirmedRegistrations(eventId);
        return "{\"total\": " + total + ", \"confirmed\": " + confirmed + "}";
    }

    // DELETE /api/events/{eventId}/registrations/{registrationId} - Delete a registration
    @DELETE
    @Path("/{registrationId}")
    public Response deleteRegistration(
            @PathParam("eventId") Long eventId,
            @PathParam("registrationId") Long registrationId) {

        boolean deleted = registrationService.deleteRegistration(eventId, registrationId);

        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Registration not found\"}")
                    .build();
        }
    }

    // GET /api/events/{eventId}/registrations/excel - Export registrations to Excel
    @GET
    @Path("/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportExcel(@PathParam("eventId") Long eventId) {
        try {
            byte[] excelData = registrationService.generateExcel(eventId);
            String filename = "deltagare-event-" + eventId + ".xlsx";

            return Response.ok(excelData)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to generate Excel file\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}