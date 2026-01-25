package com.eventmanager.rest;

import com.eventmanager.dto.ParticipantDTO;
import com.eventmanager.service.ParticipantService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Set;

// REST resource for Participant endpoints
@Path("/participants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParticipantResource {

    @Inject
    private ParticipantService participantService;

    // GET /api/participants - List all participants
    @GET
    public List<ParticipantDTO> getAllParticipants() {
        return participantService.findAll();
    }

    // GET /api/participants/{id} - Get participant by ID
    @GET
    @Path("/{id}")
    public ParticipantDTO getParticipantById(@PathParam("id") Long id) {
        return participantService.findById(id);
    }

    // GET /api/participants/patrol/{patrolId} - Get participants by patrol
    @GET
    @Path("/patrol/{patrolId}")
    public List<ParticipantDTO> getParticipantsByPatrol(@PathParam("patrolId") Long patrolId) {
        return participantService.findByPatrolId(patrolId);
    }

    // GET /api/participants/event/{eventId} - Get participants by event
    @GET
    @Path("/event/{eventId}")
    public List<ParticipantDTO> getParticipantsByEvent(@PathParam("eventId") Long eventId) {
        return participantService.findByEventId(eventId);
    }

    // GET /api/participants/with-allergens - Get participants with allergens
    @GET
    @Path("/with-allergens")
    public List<ParticipantDTO> getParticipantsWithAllergens() {
        return participantService.findWithAllergens();
    }

    // GET /api/participants/search - Search participants by name
    @GET
    @Path("/search")
    public List<ParticipantDTO> searchParticipants(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return participantService.findAll();
        }
        return participantService.searchByName(name);
    }

    // POST /api/participants - Create new participant
    @POST
    public Response createParticipant(@Valid ParticipantDTO participantDTO) {
        ParticipantDTO created = participantService.create(participantDTO);
        return Response.created(URI.create("/api/participants/" + created.getId()))
            .entity(created)
            .build();
    }

    // PUT /api/participants/{id} - Update participant
    @PUT
    @Path("/{id}")
    public ParticipantDTO updateParticipant(@PathParam("id") Long id, @Valid ParticipantDTO participantDTO) {
        return participantService.update(id, participantDTO);
    }

    // DELETE /api/participants/{id} - Delete participant
    @DELETE
    @Path("/{id}")
    public Response deleteParticipant(@PathParam("id") Long id) {
        participantService.delete(id);
        return Response.noContent().build();
    }

    // POST /api/participants/{id}/allergens/{allergenId} - Add allergen to participant
    @POST
    @Path("/{id}/allergens/{allergenId}")
    public ParticipantDTO addAllergen(
            @PathParam("id") Long participantId,
            @PathParam("allergenId") Long allergenId) {
        return participantService.addAllergen(participantId, allergenId);
    }

    // DELETE /api/participants/{id}/allergens/{allergenId} - Remove allergen from participant
    @DELETE
    @Path("/{id}/allergens/{allergenId}")
    public ParticipantDTO removeAllergen(
            @PathParam("id") Long participantId,
            @PathParam("allergenId") Long allergenId) {
        return participantService.removeAllergen(participantId, allergenId);
    }

    // PUT /api/participants/{id}/allergens - Set all allergens for participant
    @PUT
    @Path("/{id}/allergens")
    public ParticipantDTO setAllergens(
            @PathParam("id") Long participantId,
            Set<Long> allergenIds) {
        return participantService.setAllergens(participantId, allergenIds);
    }
}
