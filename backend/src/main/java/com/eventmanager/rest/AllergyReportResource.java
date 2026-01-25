package com.eventmanager.rest;

import com.eventmanager.dto.AllergyReportDTO;
import com.eventmanager.service.AllergyReportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * REST resource for allergy report endpoints
 */
@Path("/events/{eventId}/allergy-report")
@Produces(MediaType.APPLICATION_JSON)
public class AllergyReportResource {

    @Inject
    private AllergyReportService allergyReportService;

    /**
     * Get allergy report as JSON
     */
    @GET
    public Response getAllergyReport(@PathParam("eventId") Long eventId) {
        try {
            AllergyReportDTO report = allergyReportService.generateReport(eventId);
            return Response.ok(report).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Event not found\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Export allergy report as Excel file (.xlsx)
     */
    @GET
    @Path("/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportExcel(@PathParam("eventId") Long eventId) {
        try {
            byte[] excelBytes = allergyReportService.generateExcel(eventId);
            
            // Get event name for filename
            AllergyReportDTO report = allergyReportService.generateReport(eventId);
            String filename = "allergirapport-" + sanitizeFilename(report.getEventName()) + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            
            return Response.ok(excelBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Event not found\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to generate Excel file\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    /**
     * Export allergy report as CSV file
     */
    @GET
    @Path("/csv")
    @Produces("text/csv; charset=UTF-8")
    public Response exportCSV(@PathParam("eventId") Long eventId) {
        try {
            String csvContent = allergyReportService.generateCSV(eventId);
            
            // Get event name for filename
            AllergyReportDTO report = allergyReportService.generateReport(eventId);
            String filename = "allergirapport-" + sanitizeFilename(report.getEventName()) + ".csv";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            
            return Response.ok(csvContent)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .header("Content-Type", "text/csv; charset=UTF-8")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Event not found\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    /**
     * Sanitize filename by removing/replacing invalid characters
     */
    private String sanitizeFilename(String name) {
        if (name == null) return "event";
        return name
            .toLowerCase()
            .replaceAll("[åä]", "a")
            .replaceAll("ö", "o")
            .replaceAll("[^a-z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
}
