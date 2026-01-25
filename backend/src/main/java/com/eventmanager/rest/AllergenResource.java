package com.eventmanager.rest;

import com.eventmanager.dto.AllergenDTO;
import com.eventmanager.entity.AllergenSeverity;
import com.eventmanager.service.AllergenService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

// REST resource for Allergen endpoints
@Path("/allergens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AllergenResource {

    @Inject
    private AllergenService allergenService;

    // GET /api/allergens - List all allergens
    @GET
    public List<AllergenDTO> getAllAllergens() {
        return allergenService.findAll();
    }

    // GET /api/allergens/{id} - Get allergen by ID
    @GET
    @Path("/{id}")
    public AllergenDTO getAllergenById(@PathParam("id") Long id) {
        return allergenService.findById(id);
    }

    // GET /api/allergens/critical - Get critical allergens (HIGH/CRITICAL severity)
    @GET
    @Path("/critical")
    public List<AllergenDTO> getCriticalAllergens() {
        return allergenService.findCritical();
    }

    // GET /api/allergens/global - Get global allergens (not tied to any event)
    @GET
    @Path("/global")
    public List<AllergenDTO> getGlobalAllergens() {
        return allergenService.findGlobal();
    }

    // GET /api/allergens/event/{eventId} - Get allergens by event
    @GET
    @Path("/event/{eventId}")
    public List<AllergenDTO> getAllergensByEvent(@PathParam("eventId") Long eventId) {
        return allergenService.findByEventId(eventId);
    }

    // GET /api/allergens/severity/{severity} - Get allergens by severity
    @GET
    @Path("/severity/{severity}")
    public List<AllergenDTO> getAllergensBySeverity(@PathParam("severity") String severity) {
        AllergenSeverity severityEnum = AllergenSeverity.valueOf(severity.toUpperCase());
        return allergenService.findBySeverity(severityEnum);
    }

    // GET /api/allergens/search - Search allergens by name
    @GET
    @Path("/search")
    public List<AllergenDTO> searchAllergens(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return allergenService.findAll();
        }
        return allergenService.searchByName(name);
    }

    // POST /api/allergens - Create new allergen
    @POST
    public Response createAllergen(@Valid AllergenDTO allergenDTO) {
        AllergenDTO created = allergenService.create(allergenDTO);
        return Response.created(URI.create("/api/allergens/" + created.getId()))
            .entity(created)
            .build();
    }

    // PUT /api/allergens/{id} - Update allergen
    @PUT
    @Path("/{id}")
    public AllergenDTO updateAllergen(@PathParam("id") Long id, @Valid AllergenDTO allergenDTO) {
        return allergenService.update(id, allergenDTO);
    }

    // DELETE /api/allergens/{id} - Delete allergen
    @DELETE
    @Path("/{id}")
    public Response deleteAllergen(@PathParam("id") Long id) {
        allergenService.delete(id);
        return Response.noContent().build();
    }
}
